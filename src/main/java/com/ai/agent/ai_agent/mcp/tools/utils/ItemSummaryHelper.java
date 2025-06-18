package com.ai.agent.ai_agent.mcp.tools.utils;

import com.ai.agent.ai_agent.entity.ItemEntity;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemSummaryHelper {

    public static String summarizeItems(String title, List<ItemEntity> items) {
        if (items == null || items.isEmpty()) {
            return "No items found for: " + title;
        }

        StringBuilder summary = new StringBuilder(title + ":\n");

        for (int i = 0; i < items.size(); i++) {
            ItemEntity item = items.get(i);
            summary.append(String.format(
                    "%d. Item Name: \"%s\", Item ID: \"%s\", SKU: \"%s\", Category: \"%s\", Units Sold: %d, Average Rating: %.1f\n",
                    i + 1,
                    item.getItemName(),
                    item.getItemId(),
                    item.getSku(),
                    item.getCategory(),
                    item.getUnitsSold(),
                    item.getAverageRating()
            ));
        }

        return summary.toString();
    }

    public static String summarizeGroupedByDynamic(List<ItemEntity> items, String groupByField) {
        if (items == null || items.isEmpty()) {
            return "No items found to group.";
        }

        // Normalize the field name
        String normalizedField = groupByField.trim();

        // Group items dynamically by field using reflection
        Map<String, List<ItemEntity>> grouped = items.stream()
                .collect(Collectors.groupingBy(item -> {
                    try {
                        Field field = ItemEntity.class.getDeclaredField(normalizedField);
                        field.setAccessible(true);
                        Object value = field.get(item);
                        return value != null ? value.toString() : "Unknown";
                    } catch (Exception e) {
                        return "Invalid field: " + normalizedField;
                    }
                }));

        StringBuilder result = new StringBuilder("Grouped by '" + normalizedField + "':\n");

        grouped.forEach((key, group) -> {
            result.append("\n== ").append(key).append(" ==\n");
            for (int i = 0; i < group.size(); i++) {
                ItemEntity item = group.get(i);
                result.append(String.format(
                        "%d. \"%s\" (ID: %s, Sold: %d, Rating: %.1f, Price: $%.2f)\n",
                        i + 1,
                        item.getItemName(),
                        item.getItemId(),
                        item.getUnitsSold(),
                        item.getAverageRating(),
                        item.getStorePrice()
                ));
            }
        });

        return result.toString();
    }
}