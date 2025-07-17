package eu.isygoit.openai;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.time.Duration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The type Ollama integration test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class OllamaIntegrationTest {

    /**
     * The constant ollama.
     */
    @Container
    static GenericContainer<?> ollama = new GenericContainer<>("ollama/ollama:latest")
            .withExposedPorts(11434)
            .withReuse(true)// Enable container reuse
            .waitingFor(Wait.forHttp("/api/tags")
                    .forStatusCode(200)
                    .withStartupTimeout(Duration.ofMinutes(3)))
            .withCommand("serve")
            // Mount a persistent volume for model storage
            .withFileSystemBind(getOllamaVolumePath(), "/root/.ollama");
    @Value("${ollama.api.url}")
    private String ollamaApiUrl;

    @Autowired
    private MockMvc mockMvc;
    @Value("${ollama.model}")
    private String model;
    @Autowired
    private ResourceLoader resourceLoader;

    /**
     * Get the volume path for Ollama models.
     * Use a consistent path to persist data across test runs.
     */
    private static String getOllamaVolumePath() {
        // Use a fixed path for the volume, e.g., in the project directory
        // Ensure this path is writable by the container
        return "./ollama-data";
    }

    /**
     * Configure properties.
     *
     * @param registry the registry
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) throws IOException, InterruptedException {
        registry.add("ollama.api.url", () ->
                "http://localhost:" + ollama.getFirstMappedPort() + "/api/generate");
        registry.add("ollama.model", () -> "qwen2.5:1.5b");
    }

    /**
     * Sets up.
     *
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    @BeforeAll
    static void setUp() throws IOException, InterruptedException {
        // Ensure the model is available without pulling if already present
        pullModelIfNeeded("qwen2.5:1.5b");
    }

    private static void pullModelIfNeeded(String model) throws IOException, InterruptedException {
        // Check if the model is already available
        org.testcontainers.containers.Container.ExecResult result = ollama.execInContainer("ollama", "list");
        String output = result.getStdout();
        if (output != null && output.contains(model)) {
            return;
        }

        // Pull the model if not found
        try {
            ollama.execInContainer("ollama", "pull", model);

            // Wait for the model to be available
            long startTime = System.currentTimeMillis();
            long timeout = Duration.ofMinutes(5).toMillis();
            boolean modelFound = false;

            while (System.currentTimeMillis() - startTime < timeout) {
                result = ollama.execInContainer("ollama", "list");
                output = result.getStdout();
                if (output.contains(model)) {
                    modelFound = true;
                    break;
                }
                Thread.sleep(2000);
            }

            if (!modelFound) {
                throw new RuntimeException("Model " + model + " not found after pulling");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error pulling model: " + e.getMessage(), e);
        }
    }

    /**
     * Test ollama generate endpoint with simple prompt.
     *
     * @throws Exception the exception
     */
    @Test
    void testOllamaGenerateEndpointWithSimplePrompt() throws Exception {
        // Wait a bit for model to be ready
        Thread.sleep(2000);

        MvcResult result = mockMvc.perform(get("/api/v1/chat/ai/ollama/generate")
                        .param("message", "Translate to french: Hello, how can I help you?")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.generatedText").exists())
                .andExpect(jsonPath("$.errorMessage").doesNotExist())
                .andReturn();

        System.out.println("Ollama Response: " + result.getResponse().getContentAsString());
    }

    /**
     * Test ollama generate endpoint with temperature.
     *
     * @throws Exception the exception
     */
    @Test
    void testOllamaGenerateEndpointWithTemperature() throws Exception {
        // Wait a bit for model to be ready
        Thread.sleep(2000);

        MvcResult result = mockMvc.perform(get("/api/v1/chat/ai/ollama/generate")
                        .param("message", "Write a short poem about spring")
                        .param("temperature", "0.8")
                        .param("maxTokens", "512")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.generatedText").exists())
                .andExpect(jsonPath("$.errorMessage").doesNotExist())
                .andReturn();

        System.out.println("Ollama Response with temperature: " + result.getResponse().getContentAsString());
    }

    /**
     * Test ollama generate endpoint with empty message.
     *
     * @throws Exception the exception
     */
    @Test
    void testOllamaGenerateEndpointWithEmptyMessage() throws Exception {
        mockMvc.perform(get("/api/v1/chat/ai/ollama/generate")
                        .param("message", "")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").exists())
                .andExpect(jsonPath("$.generatedText").doesNotExist());
    }

    /**
     * Test ollama generate endpoint with long message.
     *
     * @throws Exception the exception
     */
    @Test
    void testOllamaGenerateEndpointWithLongMessage() throws Exception {
        String longMessage = "a".repeat(5000); // Exceeds 4096 character limit

        mockMvc.perform(get("/api/v1/chat/ai/ollama/generate")
                        .param("message", longMessage)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").exists())
                .andExpect(jsonPath("$.generatedText").doesNotExist());
    }

    /**
     * Test ollama analyze bill endpoint with valid PDF.
     *
     * @throws Exception the exception
     */
    @Test
    void testOllamaAnalyzeBillEndpointWithValidPDF() throws Exception {
        // Load sample PDF from resources
        Resource pdfResource = resourceLoader.getResource("classpath:Facture-981-06-2025.pdf");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "Facture-981-06-2025.pdf",
                "application/pdf",
                pdfResource.getInputStream().readAllBytes()
        );

        // Wait a bit for modelorean to be ready
        Thread.sleep(2000);

        MvcResult result = mockMvc.perform(multipart("/api/v1/chat/ai/ollama/analyze-bill")
                        .file(file)
                        .param("temperature", "0.7")
                        .param("maxTokens", "512")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.generatedText").exists())
                .andExpect(jsonPath("$.errorMessage").doesNotExist())
                .andReturn();

        System.out.println("Ollama Bill Analysis Response: " + result.getResponse().getContentAsString());
    }

    /**
     * Test ollama analyze bill endpoint with empty file.
     *
     * @throws Exception the exception
     */
    @Test
    void testOllamaAnalyzeBillEndpointWithEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

        mockMvc.perform(multipart("/api/v1/chat/ai/ollama/analyze-bill")
                        .file(file)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").exists())
                .andExpect(jsonPath("$.generatedText").doesNotExist());
    }

    /**
     * Test ollama analyze bill endpoint with invalid file type.
     *
     * @throws Exception the exception
     */
    @Test
    void testOllamaAnalyzeBillEndpointWithInvalidFileType() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "Not a PDF".getBytes());

        mockMvc.perform(multipart("/api/v1/chat/ai/ollama/analyze-bill")
                        .file(file)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").exists())
                .andExpect(jsonPath("$.generatedText").doesNotExist());
    }
}