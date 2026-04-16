package eu.isygoit.openai.controller;

import eu.isygoit.constants.RestApiConstants;
import eu.isygoit.openai.dto.GeminiResponse;
import eu.isygoit.openai.exception.GeminiApiException;
import eu.isygoit.openai.service.GeminiApiService;
import eu.isygoit.openai.service.OllamaApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Chat controller for Gemini and Ollama endpoints.
 * Compatible with Spring Boot 3.5.x.
 */
@RestController
@RequestMapping("/api/v1/chat")
@Slf4j
@Tag(name = "Chat Controller", description = "Endpoints for AI-powered chat and analysis using Gemini and Ollama")
@SecurityRequirement(name = "BearerAuth")
public class ChatController {

    private final GeminiApiService geminiApiService;
    private final OllamaApiService ollamaApiService;

    @Autowired
    public ChatController(GeminiApiService geminiApiService, OllamaApiService ollamaApiService) {
        this.geminiApiService = geminiApiService;
        this.ollamaApiService = ollamaApiService;
    }

    @Operation(summary = "Generate content using Gemini AI",
            description = "Sends a message to Gemini AI and returns the generated content")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully generated content",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GeminiResponse.class))}),
            @ApiResponse(responseCode = "400",
                    description = "Invalid input message",
                    content = @Content),
            @ApiResponse(responseCode = "429",
                    description = "Rate limit exceeded",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content)
    })
    @GetMapping(value = "/ai/gemini/generate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GeminiResponse> geminiGenerateContent(
            @Parameter(description = "Message to send to AI", required = true) @RequestParam String message,
            @Parameter(description = "Sampling temperature") @RequestParam(required = false) Double temperature,
            @Parameter(description = "Maximum number of tokens to generate") @RequestParam(required = false) Integer maxTokens) {

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

    @Operation(summary = "Generate content using Ollama AI",
            description = "Sends a message to Ollama AI and returns the generated content")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully generated content",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GeminiResponse.class))}),
            @ApiResponse(responseCode = "400",
                    description = "Invalid input message",
                    content = @Content),
            @ApiResponse(responseCode = "429",
                    description = "Rate limit exceeded",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content)
    })
    @GetMapping(value = "/ai/ollama/generate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GeminiResponse> ollamaGenerateContent(
            @Parameter(description = "Message to send to AI", required = true) @RequestParam String message,
            @Parameter(description = "Sampling temperature") @RequestParam(required = false) Double temperature,
            @Parameter(description = "Maximum number of tokens to generate") @RequestParam(required = false) Integer maxTokens) {

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
            } else {
                return ResponseEntity.internalServerError().body(errorResponse);
            }
        }
    }

    @Operation(summary = "Analyze a bill using Ollama AI",
            description = "Uploads a PDF bill and returns structured information extracted by Ollama AI")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully analyzed bill",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GeminiResponse.class))}),
            @ApiResponse(responseCode = "400",
                    description = "Invalid file or parameters",
                    content = @Content),
            @ApiResponse(responseCode = "429",
                    description = "Rate limit exceeded",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content)
    })
    @PostMapping(value = "/ai/ollama/analyze-bill", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GeminiResponse> analyzeBill(
            @Parameter(description = "PDF bill file to analyze", required = true) @RequestPart(name = RestApiConstants.FILE) MultipartFile file,
            @Parameter(description = "Sampling temperature") @RequestParam(required = false) Double temperature,
            @Parameter(description = "Maximum number of tokens to generate") @RequestParam(required = false) Integer maxTokens) {

        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File cannot be empty");
            }
            if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
                throw new IllegalArgumentException("File must be a PDF");
            }

            String generatedJson = ollamaApiService.analyzeBill(file, temperature, maxTokens != null ? maxTokens : 800);
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

    @Operation(summary = "Analyze a CV using Ollama AI",
            description = "Uploads a PDF CV and returns structured information extracted by Ollama AI")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully analyzed CV",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GeminiResponse.class))}),
            @ApiResponse(responseCode = "400",
                    description = "Invalid file or parameters",
                    content = @Content),
            @ApiResponse(responseCode = "429",
                    description = "Rate limit exceeded",
                    content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content)
    })
    @PostMapping(value = "/ai/ollama/analyze-cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GeminiResponse> analyzeCV(
            @Parameter(description = "PDF CV file to analyze", required = true) @RequestPart(name = RestApiConstants.FILE) MultipartFile file,
            @Parameter(description = "Sampling temperature") @RequestParam(required = false) Double temperature,
            @Parameter(description = "Maximum number of tokens to generate") @RequestParam(required = false) Integer maxTokens) {

        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File cannot be empty");
            }
            if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
                throw new IllegalArgumentException("File must be a PDF");
            }

            String generatedJson = ollamaApiService.analyzeCV(file, temperature, maxTokens != null ? maxTokens : 800);
            GeminiResponse response = GeminiResponse.success(generatedJson);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Ollama CV analysis error: {}", e.getMessage());
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