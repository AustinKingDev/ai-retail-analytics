package com.ai.agent.ai_agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryParameters {
    private Boolean onlineAvailable;
    private Boolean storeAvailable;
    private Integer minUnitsSold;
    private Integer maxUnitsSold;
    private Double minAverageRating;
    private Double maxAverageRating;
    private Integer maxStock;
    private String sortBy;
    private String sortOrder;
    private Integer limit;
}
