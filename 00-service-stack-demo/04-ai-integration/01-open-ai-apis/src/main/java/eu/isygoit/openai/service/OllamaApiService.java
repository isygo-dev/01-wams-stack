package eu.isygoit.openai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.helper.JsonHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for interacting with Ollama.
 * Updated for Spring Boot 3.5+: modern RestTemplateBuilder API + improved error handling.
 */
@Slf4j
@Service
public class OllamaApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final String ollamaApiUrl;
    private final String model;

    public OllamaApiService(
            RestTemplateBuilder restTemplateBuilder,
            ObjectMapper objectMapper,
            ResourceLoader resourceLoader,
            @Value("${ollama.api.url}") String ollamaApiUrl,
            @Value("${ollama.model}") String model) {

        // Updated for Spring Boot 3.5+: use fluent methods instead of deprecated setters
        this.restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(60))   // Suitable for smaller models like qwen2.5:3b
                .build();

        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.ollamaApiUrl = ollamaApiUrl;
        this.model = model;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Plain text generation
    // ─────────────────────────────────────────────────────────────────────────

    public String generateContent(String message) {
        return generateContent(message, null, null);
    }

    public String generateContent(String message, Double temperature, Integer maxTokens) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
        if (message.length() > 12000) {
            throw new IllegalArgumentException("Message exceeds maximum allowed length (12000 chars)");
        }

        try {
            log.debug("Calling Ollama generate with prompt length: {} characters", message.length());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("prompt", message);
            requestBody.put("stream", false);

            Map<String, Object> options = new HashMap<>();
            if (temperature != null) options.put("temperature", temperature);
            if (maxTokens != null) options.put("num_predict", Math.min(maxTokens, 2000));
            if (!options.isEmpty()) requestBody.put("options", options);

            HttpEntity<String> request = new HttpEntity<>(JsonHelper.toJson(requestBody), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    ollamaApiUrl, HttpMethod.POST, request, String.class);

            return parseAndValidateResponse(response.getBody());

        } catch (HttpClientErrorException ex) {
            log.error("Ollama HTTP error: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new RuntimeException("Ollama API error: " + ex.getResponseBodyAsString(), ex);
        } catch (Exception ex) {
            log.error("Error calling Ollama API", ex);
            throw new RuntimeException("Failed to call Ollama: " + ex.getMessage(), ex);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PDF Analysis - Bill & CV
    // ─────────────────────────────────────────────────────────────────────────

    public String analyzeBill(MultipartFile file, Double temperature, Integer maxTokens) {
        try {
            String extractedText = extractTextFromPDF(file);
            log.info("Bill PDF extracted text length: {} characters", extractedText.length());

            Resource resource = resourceLoader.getResource("classpath:bill-structure.json");
            String jsonStructure = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            String prompt = """
                    You are an expert bill parser. Extract all key information from the bill text below.
                    Return ONLY a valid JSON object matching this exact structure (no extra text, no markdown, no explanations):
                    
                    """ + jsonStructure + """
                    
                    Bill text:
                    """ + extractedText.substring(0, Math.min(extractedText.length(), 8000));

            String jsonResponse = generateContent(prompt, temperature, maxTokens != null ? maxTokens : 800);
            String cleanedJson = cleanJsonResponse(jsonResponse);

            // Validate JSON
            objectMapper.readTree(cleanedJson);
            return cleanedJson;

        } catch (IOException ex) {
            log.error("PDF processing failed", ex);
            throw new RuntimeException("Failed to process bill PDF: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            log.error("Bill analysis failed", ex);
            throw new RuntimeException("Failed to analyze bill: " + ex.getMessage(), ex);
        }
    }

    public String analyzeCV(MultipartFile file, Double temperature, Integer maxTokens) {
        try {
            String extractedText = extractTextFromPDF(file);
            log.info("CV PDF extracted text length: {} characters", extractedText.length());

            Resource resource = resourceLoader.getResource("classpath:cv-structure.json");
            String jsonStructure = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            String prompt = """
                    You are an expert resume parser. Extract all information from the CV text below.
                    Return ONLY a valid JSON object matching this exact structure (no extra text, no markdown):
                    
                    """ + jsonStructure + """
                    
                    CV text:
                    """ + extractedText.substring(0, Math.min(extractedText.length(), 10000));

            String jsonResponse = generateContent(prompt, temperature, maxTokens != null ? maxTokens : 1200);
            String cleanedJson = cleanJsonResponse(jsonResponse);

            objectMapper.readTree(cleanedJson);
            return cleanedJson;

        } catch (IOException ex) {
            log.error("CV PDF processing failed", ex);
            throw new RuntimeException("Failed to process CV PDF: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            log.error("CV analysis failed", ex);
            throw new RuntimeException("Failed to analyze CV: " + ex.getMessage(), ex);
        }
    }

    private String extractTextFromPDF(MultipartFile file) throws IOException {
        byte[] pdfBytes = file.getBytes();

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            if (document.isEncrypted()) {
                throw new IOException("Encrypted PDFs are not supported");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document).trim();
        }
    }

    private String cleanJsonResponse(String response) throws IOException {
        if (response == null || response.trim().isEmpty()) {
            throw new IOException("Empty response from Ollama");
        }

        String cleaned = response.trim();

        // Remove common markdown code fences
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7).trim();
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3).trim();
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
        }

        // Basic validation
        objectMapper.readTree(cleaned);
        return cleaned;
    }

    private String parseAndValidateResponse(String responseBody) {
        try {
            var jsonNode = objectMapper.readTree(responseBody);
            if (jsonNode.has("error")) {
                throw new RuntimeException("Ollama error: " + jsonNode.path("error").asText());
            }
            String text = jsonNode.path("response").asText().trim();
            if (text.isEmpty()) {
                throw new RuntimeException("Empty response from Ollama");
            }
            return text;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to parse Ollama response: " + ex.getMessage(), ex);
        }
    }
}