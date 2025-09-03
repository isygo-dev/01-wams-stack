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

/**
 * The type Gemini api service.
 */
@Service
@Slf4j
public class GeminiApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String geminiApiUrl;
    private final String apiKey;
    private final long timeout;
    private final int maxRetries;

    /**
     * Instantiates a new Gemini api service.
     *
     * @param restTemplate the rest template
     * @param objectMapper the object mapper
     * @param geminiApiUrl the gemini api url
     * @param apiKey       the api key
     * @param timeout      the timeout
     * @param maxRetries   the max retries
     */
    public GeminiApiService(RestTemplate restTemplate,
                            ObjectMapper objectMapper,
                            @Value("${google.gemini.api.url}") String geminiApiUrl,
                            @Value("${google.gemini.api.key}") String apiKey,
                            @Value("${google.gemini.api.timeout}") long timeout,
                            @Value("${google.gemini.api.max-retries}") int maxRetries) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.geminiApiUrl = geminiApiUrl;
        this.apiKey = apiKey;
        this.timeout = timeout;
        this.maxRetries = maxRetries;
    }

    /**
     * Generate content gemini response.
     *
     * @param message the message
     * @return the gemini response
     * @throws GeminiApiException the gemini api exception
     */
    public GeminiResponse generateContent(String message) throws GeminiApiException {
        try {
            // Validate input
            if (message == null || message.trim().isEmpty()) {
                throw new GeminiApiException("Message cannot be empty");
            }

            if (message.length() > 8192) {
                throw new GeminiApiException("Message exceeds maximum length of 8192 characters");
            }

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("x-goog-api-key", apiKey);

            // Prepare request with safety settings
            GeminiRequest requestBody = new GeminiRequest();
            GeminiRequest.Content content = new GeminiRequest.Content();
            GeminiRequest.Part part = new GeminiRequest.Part();
            part.setText(message);
            content.setParts(new GeminiRequest.Part[]{part});

            // Configure safety settings
            GeminiRequest.SafetySetting[] safetySettings = {
                    new GeminiRequest.SafetySetting("HARM_CATEGORY_HARASSMENT", "BLOCK_ONLY_HIGH"),
                    new GeminiRequest.SafetySetting("HARM_CATEGORY_HATE_SPEECH", "BLOCK_ONLY_HIGH"),
                    new GeminiRequest.SafetySetting("HARM_CATEGORY_SEXUALLY_EXPLICIT", "BLOCK_ONLY_HIGH"),
                    new GeminiRequest.SafetySetting("HARM_CATEGORY_DANGEROUS_CONTENT", "BLOCK_ONLY_HIGH")
            };

            requestBody.setContents(new GeminiRequest.Content[]{content});
            requestBody.setSafetySettings(safetySettings);
            requestBody.setGenerationConfig(new GeminiRequest.GenerationConfig(0.7, 0.9, 2048));

            // Make API call with timeout
            HttpEntity<GeminiRequest> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    geminiApiUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            // Parse and validate response
            return parseAndValidateResponse(response.getBody());

        } catch (HttpClientErrorException e) {
            String errorMsg = "Client error calling Gemini API: " + e.getStatusCode() + " - " + e.getResponseBodyAsString();
            log.error(errorMsg);
            throw new GeminiApiException(errorMsg, e);
        } catch (Exception e) {
            log.error("Unexpected error calling Gemini API: {}", e.getMessage());
            throw new GeminiApiException("Unexpected error: " + e.getMessage(), e);
        }
    }

    private GeminiResponse parseAndValidateResponse(String responseBody) throws GeminiApiException {
        try {
            var jsonNode = objectMapper.readTree(responseBody);

            // Check for errors first
            if (jsonNode.has("error")) {
                String errorMsg = jsonNode.path("error").path("message").asText();
                throw new GeminiApiException("Gemini API error: " + errorMsg);
            }

            // Check if candidates exist
            if (!jsonNode.has("candidates") || jsonNode.path("candidates").size() == 0) {
                throw new GeminiApiException("No candidates returned from Gemini API");
            }

            // Check safety ratings
            var firstCandidate = jsonNode.path("candidates").get(0);
            if (firstCandidate.has("safetyRatings")) {
                for (var rating : firstCandidate.path("safetyRatings")) {
                    String category = rating.path("category").asText();
                    String probability = rating.path("probability").asText();
                    if ("HIGH" .equals(probability) || "MEDIUM" .equals(probability)) {
                        throw new GeminiApiException("Content blocked due to safety concerns: " +
                                category + " (" + probability + ")");
                    }
                }
            }

            // Extract generated text
            String generatedText = firstCandidate
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            if (generatedText == null || generatedText.trim().isEmpty()) {
                throw new GeminiApiException("Empty response from Gemini API");
            }

            return GeminiResponse.success(generatedText);
        } catch (Exception e) {
            throw new GeminiApiException("Failed to parse Gemini API response: " + e.getMessage(), e);
        }
    }
}