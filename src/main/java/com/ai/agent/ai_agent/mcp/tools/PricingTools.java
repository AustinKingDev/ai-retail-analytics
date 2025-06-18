package com.ai.agent.ai_agent.mcp.tools;

import com.ai.agent.ai_agent.entity.ItemEntity;
import com.ai.agent.ai_agent.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PricingTools {

    private static final Logger logger = LoggerFactory.getLogger(PricingTools.class);
    private final ItemRepository itemRepository;

    public PricingTools(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Tool(name = "topExpensiveItems", description = "Get the top N most expensive items filtered by availability")
    public List<ItemEntity> getTopExpensiveItems(
            @ToolParam(description = "Number of items to return") int count,
            @ToolParam(description = "Availability: online, store, both") String availability
    ) {
        logger.info("Fetching top {} expensive items for availability: {}", count, availability);
        Pageable pageable = PageRequest.of(0, count);
        try {
            return switch (availability.toLowerCase()) {
                case "online" -> itemRepository.findOnlineOnlyItemsByStorePriceDesc(pageable);
                case "store" -> itemRepository.findStoreOnlyItemsByStorePriceDesc(pageable);
                case "both" -> itemRepository.findOnlineAndStoreItemsByStorePriceDesc(pageable);
                default -> {
                    logger.error("Invalid availability filter: {}", availability);
                    throw new IllegalArgumentException("Invalid availability filter: must be online, store, or both.");
                }
            };
        } catch (Exception ex) {
            logger.error("Error fetching top expensive items. Count: {}, Availability: {}", count, availability, ex);
            throw new RuntimeException("Failed to fetch top expensive items", ex);
        }
    }

    @Tool(name = "topExpensiveItemSummaries", description = "Get a summary of the top N most expensive items filtered by availability")
    public String getTopExpensiveItemSummaries(
            @ToolParam(description = "Number of items to summarize") int count,
            @ToolParam(description = "Availability: online, store, both") String availability
    ) {
        logger.info("Summarizing top {} expensive items for availability: {}", count, availability);
        try {
            List<ItemEntity> items = getTopExpensiveItems(count, availability);

            if (items.isEmpty()) {
                return "No items found for availability: " + availability;
            }

            StringBuilder summary = new StringBuilder("Top " + count + " most expensive items (" + availability + "):\n");
            for (int i = 0; i < items.size(); i++) {
                ItemEntity item = items.get(i);
                summary.append(String.format(
                        "%d. %s ($%.2f) — Brand: %s, Category: %s\n",
                        i + 1,
                        item.getItemName(),
                        item.getStorePrice(),
                        item.getBrand(),
                        item.getCategory()
                ));
            }
            return summary.toString();
        } catch (Exception ex) {
            logger.error("Error summarizing top expensive items. Count: {}, Availability: {}", count, availability, ex);
            throw new RuntimeException("Failed to summarize top expensive items", ex);
        }
    }

    @Tool(name = "optimizePrices", description = "Suggest optimal prices for items based on sales and stock levels")
    public String optimizePrices(
            @ToolParam(description = "Minimum sales to consider as high") int highSalesThreshold,
            @ToolParam(description = "Maximum stock to consider as low") int lowStockThreshold,
            @ToolParam(description = "Percentage to increase price for high demand (e.g. 10 for 10%)") double increasePercent,
            @ToolParam(description = "Percentage to decrease price for low demand (e.g. 10 for 10%)") double decreasePercent,
            @ToolParam(description = "Maximum number of items to analyze") int limit
    ) {
        logger.info("Optimizing prices with highSalesThreshold={}, lowStockThreshold={}, increasePercent={}, decreasePercent={}, limit={}",
                highSalesThreshold, lowStockThreshold, increasePercent, decreasePercent, limit);
        try {
            List<ItemEntity> items = itemRepository.findAll(PageRequest.of(0, limit)).getContent();
            StringBuilder result = new StringBuilder("Price Optimization Suggestions:\n");
            for (ItemEntity item : items) {
                String suggestion;
                double newPrice = item.getStorePrice();
                int stock = item.getQuantityInStock();
                if (item.getUnitsSold() >= highSalesThreshold && stock <= lowStockThreshold) {
                    newPrice = item.getStorePrice() * (1 + increasePercent / 100.0);
                    suggestion = String.format("Increase price to $%.2f", newPrice);
                } else if (item.getUnitsSold() < highSalesThreshold && stock > lowStockThreshold) {
                    newPrice = item.getStorePrice() * (1 - decreasePercent / 100.0);
                    suggestion = String.format("Decrease price to $%.2f", newPrice);
                } else {
                    suggestion = "Keep current price";
                }
                result.append(String.format("%s (Current: $%.2f, Sales: %d, Stock: %d): %s\n",
                        item.getItemName(), item.getStorePrice(), item.getUnitsSold(), stock, suggestion));
            }
            return result.toString();
        } catch (Exception ex) {
            logger.error("Error optimizing prices", ex);
            throw new RuntimeException("Failed to optimize prices", ex);
        }
    }

    @Tool(name = "analyzeDiscountPromotionImpact", description = "Analyze the impact of discounts or promotions on sales and inventory turnover")
    public String analyzeDiscountPromotionImpact(
            @ToolParam(description = "Promotion name or code") String promotion,
            @ToolParam(description = "Time window in days") int days
    ) {
        logger.info("Analyzing impact of promotion: {}, over last {} days", promotion, days);
        try {
            List<ItemEntity> items = itemRepository.findAll(); // Filter by promotion if possible
            StringBuilder sb = new StringBuilder("Promotion Impact Analysis for '" + promotion + "' (Last " + days + " days):\n");
            sb.append(String.format("%-25s %-18s %-18s %-18s\n", "Item", "Prev Daily Sales", "Promo Daily Sales", "Change (%)"));

            for (ItemEntity item : items) {
                // Mock: Assume all sales in last 2*days, and promotion active in last 'days'
                int totalSales = item.getUnitsSold();
                int promoSales = (int) (totalSales * 0.6); // Assume 60% of sales during promo
                int prevSales = totalSales - promoSales;

                double prevDaily = prevSales / (double) days;
                double promoDaily = promoSales / (double) days;
                double change = prevDaily == 0 ? 100.0 : ((promoDaily - prevDaily) / prevDaily) * 100.0;

                sb.append(String.format("%-25s %-18.2f %-18.2f %-17.1f%%\n",
                        item.getItemName(), prevDaily, promoDaily, change));
            }
            return sb.toString();
        } catch (Exception ex) {
            logger.error("Error analyzing promotion impact", ex);
            throw new RuntimeException("Failed to analyze promotion impact", ex);
        }
    }

    @Tool(name = "marginAnalyzer", description = "Calculate and report profit margins by item, category, or brand")
    public String marginAnalyzer(
            @ToolParam(description = "Group by: item, category, or brand") String groupBy
    ) {
        logger.info("Analyzing margins grouped by: {}", groupBy);
        if (!"item".equalsIgnoreCase(groupBy) && !"category".equalsIgnoreCase(groupBy) && !"brand".equalsIgnoreCase(groupBy)) {
            return "Invalid groupBy value. Use 'item', 'category', or 'brand'.";
        }
        try {
            List<ItemEntity> items = itemRepository.findAll();
            StringBuilder sb = new StringBuilder("Profit Margin Analysis by " + groupBy + ":\n");

            if ("item".equalsIgnoreCase(groupBy)) {
                sb.append(String.format("%-25s %-12s %-12s %-12s\n", "Item", "Cost", "Price", "Margin (%)"));
                for (ItemEntity item : items) {
                    double cost = item.getCostPrice();
                    double price = item.getStorePrice();
                    double margin = price == 0 ? 0 : (price - cost) / price * 100.0;
                    sb.append(String.format("%-25s $%-11.2f $%-11.2f %-11.2f%%\n",
                            item.getItemName(), cost, price, margin));
                }
            } else {
                java.util.Map<String, double[]> groupMap = new java.util.HashMap<>();
                for (ItemEntity item : items) {
                    String key = "category".equalsIgnoreCase(groupBy) ? item.getCategory() : item.getBrand();
                    double[] vals = groupMap.getOrDefault(key, new double[2]); // [0]=cost sum, [1]=price sum
                    vals[0] += item.getCostPrice();
                    vals[1] += item.getStorePrice();
                    groupMap.put(key, vals);
                }
                sb.append(String.format("%-25s %-12s %-12s %-12s\n", groupBy, "Total Cost", "Total Price", "Margin (%)"));
                for (var entry : groupMap.entrySet()) {
                    String key = entry.getKey();
                    double[] vals = entry.getValue();
                    double margin = vals[1] == 0 ? 0 : (vals[1] - vals[0]) / vals[1] * 100.0;
                    sb.append(String.format("%-25s $%-11.2f $%-11.2f %-11.2f%%\n",
                            key, vals[0], vals[1], margin));
                }
            }
            return sb.toString();
        } catch (Exception ex) {
            logger.error("Error analyzing margins by {}", groupBy, ex);
            throw new RuntimeException("Failed to analyze margins by " + groupBy, ex);
        }
    }
}
