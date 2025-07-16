package eu.isygoit.openai;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The type Ollama integration test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class OllamaIntegrationTest {

    @Value("${ollama.api.url}") 
    private String ollamaApiUrl;
    @Value("${ollama.model}") 
    private String model;
            
    @Autowired
    private MockMvc mockMvc;

    /**
     * The constant ollama.
     */
    @Container
    static GenericContainer<?> ollama = new GenericContainer<>("ollama/ollama:latest")
            .withExposedPorts(11434)
            .waitingFor(Wait.forHttp("/api/tags")
                    .forStatusCode(200)
                    .withStartupTimeout(Duration.ofMinutes(3)))
            .withCommand("serve");

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
        // Pull the model during container setup
        pullModel("qwen2.5:1.5b");
    }

    private static void pullModel(String model) throws IOException, InterruptedException {
        ollama.execInContainer("ollama", "pull", model);

        // Wait for the model to be available
        try {
            long startTime = System.currentTimeMillis();
            long timeout = Duration.ofMinutes(5).toMillis();
            boolean modelFound = false;

            while (System.currentTimeMillis() - startTime < timeout) {
                org.testcontainers.containers.Container.ExecResult result = ollama.execInContainer("ollama", "list");
                String output = result.getStdout();
                if (output.contains(model)) {
                    modelFound = true;
                    break;
                }
                Thread.sleep(2000);
            }

            if (!modelFound) {
                throw new RuntimeException("Model qwen2.5:1.5b not found after pulling");
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
                        .param("maxTokens", "100")
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
}