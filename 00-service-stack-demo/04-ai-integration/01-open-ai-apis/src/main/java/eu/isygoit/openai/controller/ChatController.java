package eu.isygoit.openai.controller;

import eu.isygoit.openai.dto.GeminiResponse;
import eu.isygoit.openai.exception.GeminiApiException;
import eu.isygoit.openai.service.GeminiApiService;
import eu.isygoit.openai.service.OllamaApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/chat")
@Slf4j
public class ChatController {

    private final GeminiApiService geminiApiService;
    private final OllamaApiService ollamaApiService;

    @Autowired
    public ChatController(GeminiApiService geminiApiService, OllamaApiService ollamaApiService) {
        this.geminiApiService = geminiApiService;
        this.ollamaApiService = ollamaApiService;
    }

    @GetMapping(value = "/ai/gemini/generate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GeminiResponse> geminiGenerateContent(
            @RequestParam String message,
            @RequestParam(required = false) Double temperature,
            @RequestParam(required = false) Integer maxTokens) {

        try {
            GeminiResponse response = geminiApiService.generateContent(message);
            return ResponseEntity.ok(response);
        } catch (GeminiApiException e) {
            log.error("Gemini API error: {}", e.getMessage());

            GeminiResponse errorResponse = GeminiResponse.error(e.getMessage());

            if (e.getMessage().contains("Rate limit")) {
                return ResponseEntity.status(429).body(errorResponse);
            } else if (e.getMessage().contains("Invalid input") ||
                    e.getMessage().contains("Message cannot be empty")) {
                return ResponseEntity.badRequest().body(errorResponse);
            } else {
                return ResponseEntity.internalServerError().body(errorResponse);
            }
        }
    }

    @GetMapping(value = "/ai/ollama/generate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GeminiResponse> ollamaGenerateContent(
            @RequestParam String message,
            @RequestParam(required = false) Double temperature,
            @RequestParam(required = false) Integer maxTokens) {

        try {
            String generatedText = ollamaApiService.generateContent(message, temperature, maxTokens);
            GeminiResponse response = GeminiResponse.success(generatedText);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Ollama API error: {}", e.getMessage());

            GeminiResponse errorResponse = GeminiResponse.error(e.getMessage());

            if (e.getMessage().contains("Rate limit")) {
                return ResponseEntity.status(429).body(errorResponse);
            } else if (e.getMessage().contains("Invalid input") ||
                    e.getMessage().contains("Message cannot be empty")) {
                return ResponseEntity.badRequest().body(errorResponse);
            } else if (e.getMessage().contains("Client error")) {
                return ResponseEntity.badRequest().body(errorResponse);
            } else {
                return ResponseEntity.internalServerError().body(errorResponse);
            }
        }
    }

    @PostMapping(value = "/ai/ollama/analyze-bill", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GeminiResponse> analyzeBill(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) Double temperature,
            @RequestParam(required = false) Integer maxTokens) {

        try {
            if (file == null || file.isEmpty()) {
                throw new RuntimeException("File cannot be empty");
            }
            if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
                throw new RuntimeException("File must be a PDF");
            }

            String generatedJson = ollamaApiService.analyzeBill(file, temperature, maxTokens);
            GeminiResponse response = GeminiResponse.success(generatedJson);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Ollama bill analysis error: {}", e.getMessage());
            GeminiResponse errorResponse = GeminiResponse.error(e.getMessage());

            if (e.getMessage().contains("Rate limit")) {
                return ResponseEntity.status(429).body(errorResponse);
            } else if (e.getMessage().contains("Invalid input") ||
                    e.getMessage().contains("File cannot be empty") ||
                    e.getMessage().contains("File must be a PDF")) {
                return ResponseEntity.badRequest().body(errorResponse);
            } else {
                return ResponseEntity.internalServerError().body(errorResponse);
            }
        }
    }
}