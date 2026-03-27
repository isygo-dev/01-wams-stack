package eu.isygoit.openai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.helper.JsonHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
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
 * Service for interacting with the Ollama local LLM API.
 * Supports plain text generation, PDF bill analysis, and PDF CV analysis.
 *
 * <p>PDFBox 3.x migration note:
 * <ul>
 *   <li>{@code PDDocument.load(InputStream)} was removed in 3.x</li>
 *   <li>Use {@code Loader.loadPDF(byte[])} instead — PDFBox 3.x no longer
 *       accepts an InputStream directly; the content must be fully read
 *       into a byte array first</li>
 * </ul>
 */
@Slf4j
@Service
public class OllamaApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final String ollamaApiUrl;
    private final String model;

    /**
     * Instantiates a new OllamaApiService.
     *
     * @param restTemplate   the rest template
     * @param objectMapper   the object mapper
     * @param resourceLoader the resource loader
     * @param ollamaApiUrl   the Ollama API base URL (e.g. http://localhost:11434/api/generate)
     * @param model          the Ollama model name (e.g. llama3, mistral)
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

    // ─────────────────────────────────────────────────────────────────────────
    // Content generation
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Generates content from a plain text prompt using default parameters.
     *
     * @param message the prompt message
     * @return the generated text response
     */
    public String generateContent(String message) {
        return generateContent(message, null, null);
    }

    /**
     * Generates content from a plain text prompt with optional tuning parameters.
     *
     * @param message     the prompt message (max ~12 000 characters)
     * @param temperature sampling temperature — higher values produce more creative output
     * @param maxTokens   maximum number of tokens to generate
     * @return the generated text response
     * @throws RuntimeException on API errors or validation failures
     */
    public String generateContent(String message, Double temperature, Integer maxTokens) {
        if (message == null || message.trim().isEmpty()) {
            throw new RuntimeException("Message cannot be empty");
        }
        if (message.length() > 4096 * 3) {
            throw new RuntimeException("Message exceeds maximum allowed length");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("prompt", message);
            requestBody.put("stream", false);

            Map<String, Object> options = new HashMap<>();
            if (temperature != null) options.put("temperature", temperature);
            if (maxTokens != null) options.put("num_predict", maxTokens);
            if (!options.isEmpty()) requestBody.put("options", options);

            HttpEntity<String> request = new HttpEntity<>(JsonHelper.toJson(requestBody), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    ollamaApiUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            return parseAndValidateResponse(response.getBody());

        } catch (HttpClientErrorException ex) {
            String msg = "Client error calling Ollama API: " + ex.getStatusCode()
                    + " — " + ex.getResponseBodyAsString();
            log.error(msg);
            throw new RuntimeException(msg, ex);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error calling Ollama API: {}", ex.getMessage());
            throw new RuntimeException("Unexpected error: " + ex.getMessage(), ex);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PDF analysis
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Extracts structured data from a bill PDF using the Ollama LLM.
     * The output JSON structure is loaded from {@code classpath:bill-structure.json}.
     *
     * @param file        the uploaded bill PDF file
     * @param temperature optional sampling temperature
     * @param maxTokens   optional max tokens (defaults to 1000)
     * @return cleaned JSON string matching the bill structure
     * @throws RuntimeException on PDF parsing or API errors
     */
    public String analyzeBill(MultipartFile file, Double temperature, Integer maxTokens) {
        try {
            String extractedText = extractTextFromPDF(file);

            Resource resource = resourceLoader.getResource("classpath:bill-structure.json");
            String jsonStructure = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            String prompt = "Extract the following information from the provided bill text and return a pure JSON object " +
                    "matching the structure below. Include all fields, using empty strings or 0 for missing values, " +
                    "and handle multiple items if present. " +
                    "Do NOT include any markdown, code blocks, or additional text — return only the JSON object:\n" +
                    jsonStructure + "\nBill text:\n" + extractedText;

            String jsonResponse = generateContent(prompt, temperature, maxTokens != null ? maxTokens : 1000);
            String cleanedJson = cleanJsonResponse(jsonResponse);
            objectMapper.readTree(cleanedJson); // validate JSON syntax
            return cleanedJson;

        } catch (IOException ex) {
            log.error("Error processing bill PDF or JSON structure: {}", ex.getMessage());
            throw new RuntimeException("Failed to process bill PDF or JSON structure: " + ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            log.error("Error analyzing bill: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Error analyzing bill: {}", ex.getMessage());
            throw new RuntimeException("Failed to analyze bill: " + ex.getMessage(), ex);
        }
    }

    /**
     * Extracts structured data from a CV/résumé PDF using the Ollama LLM.
     * The output JSON structure is loaded from {@code classpath:cv-structure.json}.
     *
     * @param file        the uploaded CV PDF file
     * @param temperature optional sampling temperature
     * @param maxTokens   optional max tokens (defaults to 1500)
     * @return cleaned JSON string matching the CV structure
     * @throws RuntimeException on PDF parsing or API errors
     */
    public String analyzeCV(MultipartFile file, Double temperature, Integer maxTokens) {
        try {
            String extractedText = extractTextFromPDF(file);

            Resource resource = resourceLoader.getResource("classpath:cv-structure.json");
            String jsonStructure = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            String prompt = "Extract the following information from the provided CV/resume text and return a pure JSON object " +
                    "matching the structure below. Include all fields, using empty strings or 0 for missing values, " +
                    "and handle multiple entries for education, work experience/achievements, skills, certifications, " +
                    "and languages if present. " +
                    "Do NOT include any markdown, code blocks, or additional text — return only the JSON object:\n" +
                    jsonStructure + "\nCV text:\n" + extractedText;

            String jsonResponse = generateContent(prompt, temperature, maxTokens != null ? maxTokens : 1500);
            String cleanedJson = cleanJsonResponse(jsonResponse);
            objectMapper.readTree(cleanedJson); // validate JSON syntax
            return cleanedJson;

        } catch (IOException ex) {
            log.error("Error processing CV PDF or JSON structure: {}", ex.getMessage());
            throw new RuntimeException("Failed to process CV PDF or JSON structure: " + ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            log.error("Error analyzing CV: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Error analyzing CV: {}", ex.getMessage());
            throw new RuntimeException("Failed to analyze CV: " + ex.getMessage(), ex);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Extracts plain text from a PDF multipart file.
     *
     * <p>PDFBox 3.x migration:
     * <ul>
     *   <li>{@code PDDocument.load(InputStream)} was removed — PDFBox 3.x no longer
     *       accepts a streaming source because it needs random access internally</li>
     *   <li>{@code Loader.loadPDF(byte[])} is the correct replacement — reads the
     *       full byte array and wraps it in an in-memory random-access buffer</li>
     * </ul>
     *
     * @param file the uploaded PDF file
     * @return extracted plain text content
     * @throws IOException if the file cannot be read or is encrypted
     */
    private String extractTextFromPDF(MultipartFile file) throws IOException {
        // PDFBox 3.x: read all bytes first, then pass to Loader.loadPDF(byte[])
        // PDDocument.load(InputStream) no longer exists in 3.x
        byte[] pdfBytes = file.getInputStream().readAllBytes();

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            if (document.isEncrypted()) {
                throw new IOException("Encrypted PDFs are not supported");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * Strips markdown code fences from an LLM response and validates JSON syntax.
     *
     * @param response raw LLM response string
     * @return clean JSON string
     * @throws IOException if the cleaned string is not valid JSON
     */
    private String cleanJsonResponse(String response) throws IOException {
        String cleaned = response.trim();
        if (cleaned.startsWith("```json") && cleaned.endsWith("```")) {
            cleaned = cleaned.substring(7, cleaned.length() - 3).trim();
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3, cleaned.endsWith("```") ? cleaned.length() - 3 : cleaned.length()).trim();
        }
        objectMapper.readTree(cleaned); // throws if invalid JSON
        return cleaned;
    }

    /**
     * Parses the Ollama API JSON response envelope and extracts the generated text.
     *
     * @param responseBody raw HTTP response body
     * @return the generated text from the {@code response} field
     * @throws RuntimeException if the response is malformed or contains an error
     */
    private String parseAndValidateResponse(String responseBody) {
        try {
            var jsonNode = objectMapper.readTree(responseBody);

            if (jsonNode.has("error")) {
                throw new RuntimeException("Ollama API error: " + jsonNode.path("error").asText());
            }

            String generatedText = jsonNode.path("response").asText();
            if (generatedText == null || generatedText.trim().isEmpty()) {
                throw new RuntimeException("Empty response from Ollama API");
            }

            return generatedText;

        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to parse Ollama API response: " + ex.getMessage(), ex);
        }
    }
}