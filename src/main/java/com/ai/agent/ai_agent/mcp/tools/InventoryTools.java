package com.ai.agent.ai_agent.mcp.tools;

import com.ai.agent.ai_agent.dto.QueryParameters;
import com.ai.agent.ai_agent.entity.ItemEntity;
import com.ai.agent.ai_agent.mcp.tools.utils.ItemSummaryHelper;
import com.ai.agent.ai_agent.repository.ItemRepository;
import com.ai.agent.ai_agent.service.DynamicQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class InventoryTools {

    private static final Logger logger = LoggerFactory.getLogger(InventoryTools.class);
    private final ItemRepository itemRepository;
    private final DynamicQueryService queryService;

    public InventoryTools(ItemRepository itemRepository, DynamicQueryService queryService) {
        this.itemRepository = itemRepository;
        this.queryService = queryService;
    }

    private List<ItemEntity> getFilteredItems(Predicate<ItemEntity> filter) {
        return itemRepository.findAll().stream().filter(filter).collect(Collectors.toList());
    }

    @Tool(name = "getItemsWithLowStockAndHighSales", description = "Get items with low stock but high sales")
    public List<ItemEntity> getItemsWithLowStockAndHighSales(
            @ToolParam(description = "Maximum quantity in stock") int maxStock,
            @ToolParam(description = "Minimum number of units sold") int minUnitsSold
    ) {
        logger.info("Fetching items with low stock (maxStock: {}) and high sales (minUnitsSold: {})", maxStock, minUnitsSold);
        try {
            return getFilteredItems(item -> item.getQuantityInStock() <= maxStock && item.getUnitsSold() >= minUnitsSold);
        } catch (Exception ex) {
            logger.error("Error fetching items with low stock and high sales", ex);
            throw new RuntimeException("Failed to fetch items with low stock and high sales", ex);
        }
    }

    @Tool(name = "getUnderperformingItems", description = "Get items with low sales and low reviews")
    public List<ItemEntity> getUnderperformingItems(
            @ToolParam(description = "Maximum units sold") int maxUnitsSold,
            @ToolParam(description = "Maximum average rating") double maxAverageRating
    ) {
        logger.info("Fetching underperforming items (maxUnitsSold: {}, maxAverageRating: {})", maxUnitsSold, maxAverageRating);
        try {
            return getFilteredItems(item -> item.getUnitsSold() <= maxUnitsSold && item.getAverageRating() <= maxAverageRating);
        } catch (Exception ex) {
            logger.error("Error fetching underperforming items", ex);
            throw new RuntimeException("Failed to fetch underperforming items", ex);
        }
    }

    @Tool(name = "summarizeItems", description = "Summarize items by custom filters")
    public String summarizeItems(
            @ToolParam(description = "Minimum units sold") Integer minUnitsSold,
            @ToolParam(description = "Maximum units sold") Integer maxUnitsSold,
            @ToolParam(description = "Minimum average rating") Double minAvgRating,
            @ToolParam(description = "Maximum average rating") Double maxAvgRating,
            @ToolParam(description = "Maximum stock quantity") Integer maxStock,
            @ToolParam(description = "Only items available online") Boolean onlineOnly,
            @ToolParam(description = "Only items available in store") Boolean storeOnly,
            @ToolParam(description = "Maximum number of items") Integer limit
    ) {
        logger.info("Summarizing items with filters: minUnitsSold={}, maxUnitsSold={}, minAvgRating={}, maxAvgRating={}, maxStock={}, onlineOnly={}, storeOnly={}, limit={}",
                minUnitsSold, maxUnitsSold, minAvgRating, maxAvgRating, maxStock, onlineOnly, storeOnly, limit);
        try {
            QueryParameters params = QueryParameters.builder()
                    .minUnitsSold(minUnitsSold)
                    .maxUnitsSold(maxUnitsSold)
                    .minAverageRating(minAvgRating)
                    .maxAverageRating(maxAvgRating)
                    .maxStock(maxStock)
                    .onlineAvailable(Boolean.TRUE.equals(onlineOnly))
                    .storeAvailable(Boolean.TRUE.equals(storeOnly))
                    .limit(limit)
                    .build();

            List<ItemEntity> items = queryService.runDynamicQuery("queryItems", params);
            return ItemSummaryHelper.summarizeItems("Filtered Items", items);
        } catch (Exception ex) {
            logger.error("Error summarizing items with custom filters", ex);
            throw new RuntimeException("Failed to summarize items", ex);
        }
    }

    @Tool(name = "summarizeItemsByField", description = "Summarize items grouped by any field (e.g. brand, category, promotion)")
    public String summarizeItemsByField(
            @ToolParam(description = "Field to group by (e.g. brand, category, promotion)") String groupBy,
            @ToolParam(description = "Minimum units sold") Integer minUnitsSold,
            @ToolParam(description = "Minimum average rating") Double minAverageRating,
            @ToolParam(description = "Maximum number of items to include in summary") Integer limit
    ) {
        int pageSize = (limit != null && limit > 0) ? limit : 10;
        logger.info("Summarizing items by field: {}, minUnitsSold: {}, minAverageRating: {}, limit: {}", groupBy, minUnitsSold, minAverageRating, pageSize);
        try {
            List<ItemEntity> items = itemRepository.findTopPerformingItems(
                    minUnitsSold != null ? minUnitsSold : 0,
                    minAverageRating != null ? minAverageRating : 0.0,
                    PageRequest.of(0, pageSize)
            );
            return ItemSummaryHelper.summarizeGroupedByDynamic(items, groupBy);
        } catch (Exception ex) {
            logger.error("Error summarizing items by field: {}", groupBy, ex);
            throw new RuntimeException("Failed to summarize items by field", ex);
        }
    }

    @Tool(name = "recommendStockReplenishment", description = "Recommend items to restock and suggested quantities")
    public String recommendStockReplenishment(
            @ToolParam(description = "Minimum days of stock to maintain") int minDaysOfStock,
            @ToolParam(description = "Sales lookback period in days") int salesLookbackDays
    ) {
        logger.info("Recommending stock replenishment for minDaysOfStock={}, salesLookbackDays={}", minDaysOfStock, salesLookbackDays);
        if (minDaysOfStock <= 0 || salesLookbackDays <= 0) {
            return "Both minDaysOfStock and salesLookbackDays must be greater than zero.";
        }
        try {
            List<ItemEntity> items = itemRepository.findAll();
            StringJoiner joiner = new StringJoiner("\n");
            int count = 0;

            for (ItemEntity item : items) {
                int recentUnitsSold = item.getUnitsSold(); // Ideally, use sales in lookback period
                double dailySales = (double) recentUnitsSold / salesLookbackDays;
                if (dailySales <= 0) continue;

                int currentStock = item.getQuantityInStock();
                int daysOfStockLeft = (int) Math.floor(currentStock / dailySales);

                if (daysOfStockLeft < minDaysOfStock) {
                    int recommendedQty = (int) Math.ceil((minDaysOfStock * dailySales) - currentStock);
                    if (recommendedQty > 0) {
                        joiner.add(String.format(
                                "%s (Current stock: %d, Sales/day: %.2f): Recommend ordering %d units to cover %d days of stock.",
                                item.getItemName(), currentStock, dailySales, recommendedQty, minDaysOfStock
                        ));
                        count++;
                    }
                }
            }
            if (count == 0) return "All items have sufficient stock.";
            return "Stock Replenishment Recommendations (" + count + " items):\n" + joiner;
        } catch (Exception ex) {
            logger.error("Error recommending stock replenishment", ex);
            throw new RuntimeException("Failed to recommend stock replenishment", ex);
        }
    }

    @Tool(name = "inventoryAgingReport", description = "List items in stock for too long (slow movers)")
    public String inventoryAgingReport(
            @ToolParam(description = "Minimum days in stock to consider as slow moving") int minDaysInStock
    ) {
        logger.info("Generating inventory aging report for items in stock over {} days", minDaysInStock);
        try {
            List<ItemEntity> items = itemRepository.findAll();
            StringBuilder report = new StringBuilder("Inventory Aging Report (Items in stock >= " + minDaysInStock + " days):\n");
            int count = 0;
            var now = java.time.ZonedDateTime.now();

            for (ItemEntity item : items) {
                if (item.getQuantityInStock() <= 0) continue;
                java.time.ZonedDateTime lastActivity = item.getLastPurchasedAt() != null ? item.getLastPurchasedAt() : item.getCreatedAt();
                if (lastActivity == null) continue;
                long daysInStock = java.time.temporal.ChronoUnit.DAYS.between(lastActivity, now);
                if (daysInStock >= minDaysInStock) {
                    report.append(String.format(
                            "%s (Stock: %d, Days in stock: %d, Last purchased: %s)\n",
                            item.getItemName(), item.getQuantityInStock(), daysInStock,
                            lastActivity.toLocalDate()
                    ));
                    count++;
                }
            }
            if (count == 0) return "No slow-moving items found.";
            return report.toString();
        } catch (Exception ex) {
            logger.error("Error generating inventory aging report", ex);
            throw new RuntimeException("Failed to generate inventory aging report", ex);
        }
    }

    @Tool(name = "demandForecast", description = "Predict future sales for items using historical data")
    public String demandForecast(
            @ToolParam(description = "Item ID or category") String itemOrCategory,
            @ToolParam(description = "Forecast period in days") int forecastDays
    ) {
        logger.info("Forecasting demand for: {}, over next {} days", itemOrCategory, forecastDays);
        final int lookbackDays = 30; // Use last 30 days as lookback period
        try {
            List<ItemEntity> items;
            // Try to find by itemId first
            ItemEntity item = itemRepository.findById(itemOrCategory).orElse(null);
            if (item != null) {
                items = List.of(item);
            } else {
                // Otherwise, treat as category
                items = itemRepository.findByCategoryIgnoreCase(itemOrCategory);
                if (items.isEmpty()) {
                    return "No items found for item ID or category: " + itemOrCategory;
                }
            }

            StringBuilder sb = new StringBuilder("Demand Forecast for '" + itemOrCategory + "' (" + forecastDays + " days):\n");
            for (ItemEntity i : items) {
                double avgDailySales = (double) i.getUnitsSold() / lookbackDays;
                int forecast = (int) Math.round(avgDailySales * forecastDays);
                sb.append(String.format(
                        "%s: Avg sales/day: %.2f, Forecasted sales: %d units\n",
                        i.getItemName(), avgDailySales, forecast
                ));
            }
            return sb.toString();
        } catch (Exception ex) {
            logger.error("Error forecasting demand", ex);
            throw new RuntimeException("Failed to forecast demand", ex);
        }
    }

    @Tool(name = "outOfStockAlert", description = "Alert when items are out of stock or below a threshold")
    public String outOfStockAlert(
            @ToolParam(description = "Stock threshold") int threshold
    ) {
        logger.info("Checking for items with stock below threshold: {}", threshold);
        try {
            List<ItemEntity> items = getFilteredItems(item -> item.getQuantityInStock() <= threshold);
            if (items.isEmpty()) return "All items are above the stock threshold.";
            StringBuilder alert = new StringBuilder("Out-of-Stock Alert (Threshold: " + threshold + "):\n");
            items.forEach(item -> alert.append(String.format(
                    "%s (Current stock: %d)\n", item.getItemName(), item.getQuantityInStock())));
            return alert.toString();
        } catch (Exception ex) {
            logger.error("Error generating out-of-stock alert", ex);
            throw new RuntimeException("Failed to generate out-of-stock alert", ex);
        }
    }

    @Tool(name = "categoryBrandPerformanceSummary", description = "Summarize sales, revenue, and stock by category or brand")
    public String categoryBrandPerformanceSummary(
            @ToolParam(description = "Group by: category or brand") String groupBy
    ) {
        logger.info("Summarizing performance by: {}", groupBy);
        if (!"category".equalsIgnoreCase(groupBy) && !"brand".equalsIgnoreCase(groupBy)) {
            return "Invalid groupBy value. Use 'category' or 'brand'.";
        }
        try {
            List<ItemEntity> items = itemRepository.findAll();
            java.util.Map<String, int[]> summary = new java.util.HashMap<>();
            java.util.Map<String, Double> revenueMap = new java.util.HashMap<>();

            for (ItemEntity item : items) {
                String key = "category".equalsIgnoreCase(groupBy) ? item.getCategory() : item.getBrand();
                int[] metrics = summary.getOrDefault(key, new int[2]); // [0]=unitsSold, [1]=stock
                metrics[0] += item.getUnitsSold();
                metrics[1] += item.getQuantityInStock();
                summary.put(key, metrics);

                double revenue = item.getUnitsSold() * item.getStorePrice();
                revenueMap.put(key, revenueMap.getOrDefault(key, 0.0) + revenue);
            }

            StringBuilder sb = new StringBuilder("Performance Summary by " + groupBy + ":\n");
            sb.append(String.format("%-25s %-12s %-12s %-12s\n", groupBy, "Units Sold", "Revenue", "Stock"));
            for (var entry : summary.entrySet()) {
                String key = entry.getKey();
                int[] metrics = entry.getValue();
                double revenue = revenueMap.getOrDefault(key, 0.0);
                sb.append(String.format("%-25s %-12d $%-11.2f %-12d\n", key, metrics[0], revenue, metrics[1]));
            }
            return sb.toString();
        } catch (Exception ex) {
            logger.error("Error summarizing performance by {}", groupBy, ex);
            throw new RuntimeException("Failed to summarize performance by " + groupBy, ex);
        }
    }
}
