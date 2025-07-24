package eu.isygoit.openai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.helper.JsonHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Ollama api service.
 */
@Service
@Slf4j
public class OllamaApiService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final String ollamaApiUrl;
    private final String model;

    /**
     * Instantiates a new Ollama api service.
     *
     * @param restTemplate   the rest template
     * @param objectMapper   the object mapper
     * @param resourceLoader the resource loader
     * @param ollamaApiUrl   the ollama api url
     * @param model          the model
     */
    public OllamaApiService(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            ResourceLoader resourceLoader,
            @Value("${ollama.api.url}") String ollamaApiUrl,
            @Value("${ollama.model}") String model) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.ollamaApiUrl = ollamaApiUrl;
        this.model = model;
    }

    /**
     * Generate content string.
     *
     * @param message the message
     * @return the string
     * @throws RuntimeException the runtime exception
     */
    public String generateContent(String message) throws RuntimeException {
        return generateContent(message, null, null);
    }

    /**
     * Generate content string.
     *
     * @param message     the message
     * @param temperature the temperature
     * @param maxTokens   the max tokens
     * @return the string
     * @throws RuntimeException the runtime exception
     */
    public String generateContent(String message, Double temperature, Integer maxTokens) throws RuntimeException {
        try {
            // Validate input
            if (message == null || message.trim().isEmpty()) {
                throw new RuntimeException("Message cannot be empty");
            }

            if (message.length() > 4096 * 3) {
                throw new RuntimeException("Message exceeds maximum length of 8192 characters");
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

            String requestJson = JsonHelper.toJson(requestBody);
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

    /**
     * Analyze bill string.
     *
     * @param file        the file
     * @param temperature the temperature
     * @param maxTokens   the max tokens
     * @return the string
     * @throws RuntimeException the runtime exception
     */
    public String analyzeBill(MultipartFile file, Double temperature, Integer maxTokens) throws RuntimeException {
        try {
            // Extract text from PDF
            String extractedText = extractTextFromPDF(file);

            // Load JSON structure from resources
            Resource resource = resourceLoader.getResource("classpath:bill-structure.json");
            String jsonStructure = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            // Create detailed prompt for Ollama
            String prompt = "Extract the following information from the provided bill text and return a pure JSON object matching the structure below. " +
                    "Include all fields, using empty strings or 0 for missing values, and handle multiple items if present. " +
                    "Do NOT include any markdown, code blocks, or additional text—return only the JSON object:\n" +
                    jsonStructure + "\n" +
                    "Bill text:\n" + extractedText;

            // Call generateContent with increased maxTokens if not specified
            String jsonResponse = generateContent(prompt, temperature, maxTokens != null ? maxTokens : 1000);

            // Clean and validate JSON response
            String cleanedJson = cleanJsonResponse(jsonResponse);

            objectMapper.readTree(cleanedJson);
            return cleanedJson;
        } catch (IOException e) {
            log.error("Error processing PDF file or reading JSON structure: {}", e.getMessage());
            throw new RuntimeException("Failed to process PDF file or JSON structure: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error analyzing bill: {}", e.getMessage());
            throw new RuntimeException("Failed to analyze bill: " + e.getMessage(), e);
        }
    }

    /**
     * Analyze cv string.
     *
     * @param file        the file
     * @param temperature the temperature
     * @param maxTokens   the max tokens
     * @return the string
     * @throws RuntimeException the runtime exception
     */
    public String analyzeCV(MultipartFile file, Double temperature, Integer maxTokens) throws RuntimeException {
        try {
            // Extract text from PDF
            String extractedText = extractTextFromPDF(file);

            // Load JSON structure from resources
            Resource resource = resourceLoader.getResource("classpath:cv-structure.json");
            String jsonStructure = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            // Create detailed prompt for Ollama
            String prompt = "Extract the following information from the provided CV/resume text and return a pure JSON object matching the structure below. " +
                    "Include all fields, using empty strings or 0 for missing values, and handle multiple entries for education, work experience/Achievements, skills, certifications, and languages if present. " +
                    "Do NOT include any markdown, code blocks, or additional text—return only the JSON object:\n" +
                    jsonStructure + "\n" +
                    "CV text:\n" + extractedText;

            // Call generateContent with increased maxTokens if not specified
            String jsonResponse = generateContent(prompt, temperature, maxTokens != null ? maxTokens : 1500);

            // Clean and validate JSON response
            String cleanedJson = cleanJsonResponse(jsonResponse);

            objectMapper.readTree(cleanedJson);
            return cleanedJson;
        } catch (IOException e) {
            log.error("Error processing PDF file or reading CV JSON structure: {}", e.getMessage());
            throw new RuntimeException("Failed to process PDF file or CV JSON structure: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error analyzing CV: {}", e.getMessage());
            throw new RuntimeException("Failed to analyze CV: " + e.getMessage(), e);
        }
    }

    private String extractTextFromPDF(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            if (document.isEncrypted()) {
                throw new IOException("Encrypted PDFs are not supported");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String cleanJsonResponse(String response) throws IOException {
        String cleaned = response.trim();
        // Remove markdown code blocks if present
        if (cleaned.startsWith("```json") && cleaned.endsWith("```")) {
            cleaned = cleaned.substring(7, cleaned.length() - 3).trim();
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3, cleaned.endsWith("```") ? cleaned.length() - 3 : cleaned.length()).trim();
        }
        // Validate JSON syntax
        objectMapper.readTree(cleaned);
        return cleaned;
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