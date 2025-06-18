package com.ai.agent.ai_agent.controller;


import com.ai.agent.ai_agent.client.AIClient;
import com.ai.agent.ai_agent.dto.QueryParameters;
import com.ai.agent.ai_agent.entity.ItemEntity;
import com.ai.agent.ai_agent.model.QueryRequest;
import com.ai.agent.ai_agent.model.QueryResponse;
import com.ai.agent.ai_agent.service.CustomQueryBuilderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ask")
public class AgentController {

    @Autowired
    private AIClient aiClient;

    @Autowired
    private CustomQueryBuilderService customQueryBuilderService;


    @PostMapping
    public QueryResponse askAgent(@RequestBody QueryRequest request) {
        String result = aiClient.query(request.getQuery());
        return QueryResponse.builder()
                .answer(result)
                .build();
    }

    @PostMapping("/query-items")
    public ResponseEntity<String> queryItems(@RequestBody QueryRequest request) {
        String response = aiClient.query(request.getQuery());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/custom-query")
    public List<ItemEntity> runCustomQuery(@RequestBody QueryParameters params) {
        return customQueryBuilderService.runCustomQuery(params);
    }
}