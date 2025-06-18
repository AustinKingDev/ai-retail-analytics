package com.ai.agent.ai_agent.client.impl;


import com.ai.agent.ai_agent.client.AIClient;
import com.ai.agent.ai_agent.mcp.tools.InventoryTools;
import com.ai.agent.ai_agent.mcp.tools.PricingTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import static com.ai.agent.ai_agent.constants.Constants.SYSTEM_PROMPT;


@Service
@RequiredArgsConstructor
public class OpenAIClientImpl implements AIClient {

    private final InventoryTools inventoryTools;
    private final PricingTools pricingTools;
    private final ChatClient chatClient;

    @Override
    public String query(String userInput) {
        return chatClient
                .prompt()
                .system(SYSTEM_PROMPT)
                .user(userInput)
                .tools(inventoryTools, pricingTools)
                .call()
                .content();
    }
}
