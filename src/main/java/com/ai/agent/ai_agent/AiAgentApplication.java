package com.ai.agent.ai_agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication

public class AiAgentApplication {

	public static void main(String[] args) {
		System.out.println("ENV: " + System.getenv("OPENAI_API_KEY"));
		SpringApplication.run(AiAgentApplication.class, args);
	}

}
