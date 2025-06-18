package com.ai.agent.ai_agent.service;

import com.ai.agent.ai_agent.dto.QueryParameters;
import com.ai.agent.ai_agent.entity.ItemEntity;
import com.ai.agent.ai_agent.mcp.query.metadata.QueryMetadata;

import java.util.List;

public interface DynamicQueryService {
    List<ItemEntity> runDynamicQuery(String type, QueryParameters params);
    List<QueryMetadata> getSupportedQueries();
}
