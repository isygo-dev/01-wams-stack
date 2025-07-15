# OpenAI Integration Example

This document presents an example of integrating an AI service (specifically Google's Gemini API) into a Spring Boot application. The example demonstrates a RESTful API for generating AI content, handling requests, and managing responses with proper error handling and testing.

## Project Overview

The project is a Spring Boot application (`OpenAiApplication`) that provides a REST endpoint for generating AI content using the Gemini API. It includes data transfer objects (DTOs), a service layer for API communication, a controller for handling HTTP requests, and integration tests.

## Project Structure

The codebase is organized under the `eu.isygoit.openai` package with the following key components:

- **DTOs**: Data structures for API requests and responses (`GeminiRequest`, `GeminiResponse`, `ConversationRequest`).
- **Service**: `GeminiApiService` for interacting with the Gemini API.
- **Controller**: `ChatController` for handling HTTP requests.
- **Exception**: `GeminiApiException` for custom error handling.
- **Main Application**: `OpenAiApplication` to bootstrap the Spring Boot application.
- **Tests**: `ChatControllerIntegrationTest` for integration testing.

## Key Components

### 1. GeminiRequest DTO
The `GeminiRequest` class defines the structure for requests sent to the Gemini API, including content, safety settings, and generation configuration.

**File**: `GeminiRequest.java`

```java
package eu.isygoit.openai.dto;

import lombok.Data;

@Data
public class GeminiRequest {
    private Content[] contents;
    private SafetySetting[] safetySettings;
    private GenerationConfig generationConfig;

    @Data
    public static class Content {
        private Part[] parts;
    }

    @Data
    public static class Part {
        private String text;
    }

    @Data
    public static class SafetySetting {
        private String category;
        private String threshold;

        public SafetySetting(String category, String threshold) {
            this.category = category;
            this.threshold = threshold;
        }
    }

    @Data
    public static class GenerationConfig {
        private double temperature;
        private double topP;
        private int maxOutputTokens;

        public GenerationConfig(double temperature, double topP, int maxOutputTokens) {
            this.temperature = temperature;
            this.topP = topP;
            this.maxOutputTokens = maxOutputTokens;
        }
    }
}
```

### 2. GeminiResponse DTO
The `GeminiResponse` class handles API responses, including success status, generated text, and error messages.

**File**: `GeminiResponse.java`

```java
package eu.isygoit.openai.dto;

import lombok.Data;

@Data
public class GeminiResponse {
    private boolean success;
    private String generatedText;
    private String errorMessage;

    public static GeminiResponse success(String generatedText) {
        GeminiResponse response = new GeminiResponse();
        response.setSuccess(true);
        response.setGeneratedText(generatedText);
        return response;
    }

    public static GeminiResponse error(String errorMessage) {
        GeminiResponse response = new GeminiResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        return response;
    }
}
```

### 3. GeminiApiService
The `GeminiApiService` handles communication with the Gemini API, including request preparation, safety settings, and response parsing.

**Key Features**:
- Validates input messages (non-empty, max 8192 characters).
- Configures safety settings to block high-risk content.
- Uses `RestTemplate` for HTTP requests with configurable timeout and retries.
- Parses and validates API responses, handling errors appropriately.

**File**: `GeminiApiService.java`

```java
package eu.isygoit.openai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.openai.dto.GeminiRequest;
import eu.isygoit.openai.dto.GeminiResponse;
import eu.isygoit.openai.exception.GeminiApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
@Slf4j
public class GeminiApiService {
    // Service implementation (see full code in provided documents)
}
```

### 4. ChatController
The `ChatController` exposes a REST endpoint (`/api/v1/chat/ai/generate`) to accept user messages and optional parameters (temperature, max tokens) and returns AI-generated content.

**Key Features**:
- Handles HTTP GET requests with query parameters.
- Returns appropriate HTTP status codes (200, 400, 429, 500) based on API response or errors.
- Logs errors for debugging.

**File**: `ChatController.java`

```java
package eu.isygoit.openai.controller;

import eu.isygoit.openai.dto.GeminiResponse;
import eu.isygoit.openai.exception.GeminiApiException;
import eu.isygoit.openai.service.GeminiApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
@Slf4j
public class ChatController {
    // Controller implementation (see full code in provided documents)
}
```

### 5. Integration Test
The `ChatControllerIntegrationTest` verifies the functionality of the `/api/v1/chat/ai/generate` endpoint using Spring Boot's testing framework and MockMvc.

**Test Case**:
- Sends a sample prompt ("Translate to Arabic: Hello, how can I help you").
- Verifies HTTP 200 status, successful response, and presence of generated text.

**File**: `ChatControllerIntegrationTest.java`

```java
package eu.isygoit.openai;

import eu.isygoit.openai.dto.GeminiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ChatControllerIntegrationTest {
    // Test implementation (see full code in provided documents)
}
```

### 6. Main Application
The `OpenAiApplication` class is the entry point for the Spring Boot application, configuring the application context and excluding unnecessary auto-configurations.

**File**: `OpenAiApplication.java`

```java
package eu.isygoit.openai;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@Slf4j
@SpringBootApplication(scanBasePackages = {
        "eu.isygoit.openai",
        "eu.isygoit.app"
}, exclude = {
        // Excluded auto-configurations
})
@OpenAPIDefinition(info = @Info(title = "Poc Open AI", version = "1.0", description = "Poc Open AI"))
public class OpenAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpenAiApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        log.debug("Configuring RestTemplate bean");
        return new RestTemplate();
    }
}
```

## Usage

1. **Configuration**:
   - Set up the Gemini API credentials and endpoint in the application properties:
     ```properties
     google.gemini.api.url=https://api.gemini.google.com/v1
     google.gemini.api.key=your-api-key
     google.gemini.api.timeout=30000
     google.gemini.api.max-retries=3
     ```

2. **Running the Application**:
   - Run `OpenAiApplication` to start the Spring Boot server.
   - The API will be available at `http://localhost:8080/api/v1/chat/ai/generate`.

3. **Making Requests**:
   - Use a tool like `curl` or Postman to send a GET request:
     ```bash
     curl "http://localhost:8080/api/v1/chat/ai/generate?message=Translate%20to%20Arabic:%20Hello,%20how%20can%20I%20help%20you"
     ```
   - Expected response:
     ```json
     {
         "success": true,
         "generatedText": "مرحبا، كيف يمكنني مساعدتك",
         "errorMessage": null
     }
     ```

4. **Error Handling**:
   - The API handles various errors (e.g., empty input, rate limits, safety concerns) and returns appropriate HTTP status codes (400, 429, 500) with error messages.

## Testing

Run the integration tests using Maven or your IDE:
```bash
mvn test
```
The `ChatControllerIntegrationTest` will verify the endpoint's functionality.

## Notes

- The application uses Lombok to reduce boilerplate code (e.g., getters, setters).
- Safety settings are configured to block high-risk content (harassment, hate speech, etc.).
- The API supports a maximum input length of 8192 characters and configurable generation parameters (temperature, topP, max tokens).
- OpenAPI (Swagger) is integrated for API documentation.

This example demonstrates a robust integration of an AI API into a Spring Boot application, suitable for use cases like chatbots, content generation, or translation services.