package com.ai.agent.ai_agent.service;

import com.ai.agent.ai_agent.dto.QueryParameters;
import com.ai.agent.ai_agent.entity.ItemEntity;

import java.util.List;

public interface CustomQueryBuilderService {
    List<ItemEntity> runCustomQuery(QueryParameters params);
}