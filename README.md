# AI Retail Agent

A Spring Boot application that provides real-time retail analytics using AI-powered tools. It integrates with OpenAI to generate insights and reports on inventory, pricing, and product performance.

## Features

- Inventory aging and performance reports
- Pricing analysis and recommendations
- Product performance queries (top/underperforming items)
- Integration with OpenAI for natural language queries
- RESTful API endpoints for data access

## Technologies

- Java 17+
- Spring Boot
- Spring Data JPA (Hibernate)
- Maven
- OpenAI API

## Getting Started

### Prerequisites

- Java 17 or higher installed
- Maven 3.8+ installed
- An OpenAI API key (you must provide your own)

### Setup Steps

1. **Clone the repository:**
   ```sh
   git clone https://github.com/your-org/ai-retail-agent.git
   cd ai-retail-agent

2. **Configure application properties:**

    By default, the application uses an H2 in-memory database with seeded sample data—no SQL database setup is required for local development.

- The H2 database runs in-memory and is available only while the application is running.
- You can access the H2 web console at [http://localhost:8080/h2-console](http://localhost:8080/h2-console).

   Set your OpenAI API key in `src/main/resources/application.properties`: openai.api.key=YOUR_OPENAI_API_KEY

> **Note:** You must provide your own valid OpenAI API key.

3. **Build the project:**

   ```sh
   mvn clean install

4. **Run the application:**

    ```sh
    mvn spring-boot:run
The API will be available at http://localhost:8080.