package com.ai.agent.ai_agent.service.impl;

import com.ai.agent.ai_agent.dto.QueryParameters;
import com.ai.agent.ai_agent.entity.ItemEntity;
import com.ai.agent.ai_agent.mcp.query.metadata.QueryMetadata;
import com.ai.agent.ai_agent.repository.ItemRepository;
import com.ai.agent.ai_agent.service.DynamicQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DynamicQueryServiceImpl implements DynamicQueryService {

    private static final int DEFAULT_LIMIT = 10;
    private static final Logger logger = LoggerFactory.getLogger(DynamicQueryServiceImpl.class);
    private final ItemRepository repository;

    public DynamicQueryServiceImpl(ItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ItemEntity> runDynamicQuery(String type, QueryParameters params) {
        logger.info("Running dynamic query. Type: {}, Params: {}", type, params);
        int limit = params.getLimit() != null ? params.getLimit() : DEFAULT_LIMIT;
        try {
            return switch (type) {
                case "topPerformingItems" -> repository.findTopPerformingItems(
                        params.getMinUnitsSold(),
                        params.getMinAverageRating(),
                        PageRequest.of(0, limit)
                );
                case "underperformingItems" -> repository.findUnderperformingItems(
                        params.getMaxUnitsSold(), params.getMaxAverageRating()
                );
                case "lowStockHighSales" -> repository.findItemsWithLowStockAndHighSales(
                        params.getMaxStock(), params.getMinUnitsSold()
                );
                case "onlineOnly" -> repository.findOnlineOnlyItemsByStorePriceDesc(PageRequest.of(0, limit));
                case "storeOnly" -> repository.findStoreOnlyItemsByStorePriceDesc(PageRequest.of(0, limit));
                case "onlineAndStore" -> repository.findOnlineAndStoreItemsByStorePriceDesc(PageRequest.of(0, limit));
                default -> {
                    logger.error("Unknown query type: {}", type);
                    throw new IllegalArgumentException("Unknown query type: " + type);
                }
            };
        } catch (Exception ex) {
            logger.error("Error running dynamic query. Type: {}, Params: {}", type, params, ex);
            throw new RuntimeException("Failed to run dynamic query: " + type, ex);
        }
    }

    @Override
    public List<QueryMetadata> getSupportedQueries() {
        logger.debug("Fetching supported queries metadata");
        return List.of(
                new QueryMetadata("topPerformingItems", "Top Performing Items", "Items with high sales and ratings"),
                new QueryMetadata("underperformingItems", "Underperforming Items", "Items with low sales and low ratings"),
                new QueryMetadata("lowStockHighSales", "Low Stock High Sales", "Items that are low in stock but selling well"),
                new QueryMetadata("onlineOnly", "Online Only Items", "Items only available online"),
                new QueryMetadata("storeOnly", "Store Only Items", "Items only available in store"),
                new QueryMetadata("onlineAndStore", "Online and Store Items", "Items available both online and in store")
        );
    }
}
