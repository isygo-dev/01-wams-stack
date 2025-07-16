package eu.isygoit.openai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class OllamaApiService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String ollamaApiUrl;
    private final String model;

    public OllamaApiService(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${ollama.api.url}") String ollamaApiUrl,
            @Value("${ollama.model}") String model) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.ollamaApiUrl = ollamaApiUrl;
        this.model = model;
    }

    public String generateContent(String message) throws RuntimeException {
        return generateContent(message, null, null);
    }

    public String generateContent(String message, Double temperature, Integer maxTokens) throws RuntimeException {
        try {
            // Validate input
            if (message == null || message.trim().isEmpty()) {
                throw new RuntimeException("Message cannot be empty");
            }

            if (message.length() > 4096) {
                throw new RuntimeException("Message exceeds maximum length of 4096 characters");
            }

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            // Prepare request body for Ollama
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("prompt", message);
            requestBody.put("stream", false);

            // Add optional parameters
            Map<String, Object> options = new HashMap<>();
            if (temperature != null) {
                options.put("temperature", temperature);
            }
            if (maxTokens != null) {
                options.put("num_predict", maxTokens);
            }

            if (!options.isEmpty()) {
                requestBody.put("options", options);
            }

            String requestJson = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

            // Make API call
            ResponseEntity<String> response = restTemplate.exchange(
                    ollamaApiUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            // Parse and validate response
            return parseAndValidateResponse(response.getBody());

        } catch (HttpClientErrorException e) {
            String errorMsg = "Client error calling Ollama API: " + e.getStatusCode() + " - " + e.getResponseBodyAsString();
            log.error(errorMsg);
            throw new RuntimeException(errorMsg, e);
        } catch (Exception e) {
            log.error("Unexpected error calling Ollama API: {}", e.getMessage());
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }
    }

    private String parseAndValidateResponse(String responseBody) throws RuntimeException {
        try {
            var jsonNode = objectMapper.readTree(responseBody);

            // Check for errors
            if (jsonNode.has("error")) {
                String errorMsg = jsonNode.path("error").asText();
                throw new RuntimeException("Ollama API error: " + errorMsg);
            }

            // Extract generated text
            String generatedText = jsonNode.path("response").asText();

            if (generatedText == null || generatedText.trim().isEmpty()) {
                throw new RuntimeException("Empty response from Ollama API");
            }

            return generatedText;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Ollama API response: " + e.getMessage(), e);
        }
    }
}