package eu.isygoit.openai;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
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
 * Integration tests for Ollama endpoints using Testcontainers.
 * Updated for Spring Boot 3.5.x + modern Testcontainers best practices.
 */
@SpringBootTest(
        classes = OpenAiApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
@Testcontainers
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class,
        CassandraAutoConfiguration.class
})
@ImportAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        CassandraAutoConfiguration.class
})
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration," +
                "org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration"
})
public class OllamaIntegrationTest {

    @Container
    static final GenericContainer<?> ollama = new GenericContainer<>("ollama/ollama:latest")
            .withExposedPorts(11434)
            .withReuse(true)
            .withCommand("serve")
            // Persistent volume for models (better cross-platform handling)
            .withFileSystemBind(getOllamaVolumePath(), "/root/.ollama")
            .waitingFor(Wait.forHttp("/api/v1/tags")
                    .forStatusCode(200)
                    .withStartupTimeout(Duration.ofMinutes(3)));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${ollama.api.url}")
    private String ollamaApiUrl;

    @Value("${ollama.model}")
    private String model;

    /**
     * Returns a consistent path for Ollama model storage.
     * This helps reuse models between test runs and speeds up CI.
     */
    private static String getOllamaVolumePath() {
        // You can also use System.getProperty("java.io.tmpdir") + "/ollama-data" for temp-based storage
        return "./ollama-data";
    }

    /**
     * Configure dynamic properties for the test (Ollama URL and model).
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("ollama.api.url", () ->
                "http://localhost:" + ollama.getFirstMappedPort() + "/api/v1/generate");
        registry.add("ollama.model", () -> "qwen2.5:3b");
    }

    /**
     * Pull the required model before all tests (runs once per test class).
     */
    @BeforeAll
    static void setUp() throws IOException, InterruptedException {
        pullModelIfNeeded("qwen2.5:3b");
    }

    private static void pullModelIfNeeded(String modelName) throws IOException, InterruptedException {
        // Check if model already exists
        var listResult = ollama.execInContainer("ollama", "list");
        String output = listResult.getStdout() + listResult.getStderr();

        if (output.contains(modelName)) {
            System.out.println("Model " + modelName + " already available.");
            return;
        }

        System.out.println("Pulling model: " + modelName + " ... This may take a few minutes.");

        var pullResult = ollama.execInContainer("ollama", "pull", modelName);
        if (pullResult.getExitCode() != 0) {
            throw new RuntimeException("Failed to pull model " + modelName + ": " + pullResult.getStderr());
        }

        // Wait until model appears in the list
        long start = System.currentTimeMillis();
        long timeout = Duration.ofMinutes(6).toMillis();

        while (System.currentTimeMillis() - start < timeout) {
            var checkResult = ollama.execInContainer("ollama", "list");
            String checkOutput = checkResult.getStdout() + checkResult.getStderr();

            if (checkOutput.contains(modelName)) {
                System.out.println("Model " + modelName + " successfully pulled and ready.");
                return;
            }
            Thread.sleep(3000);
        }

        throw new RuntimeException("Timeout: Model " + modelName + " was not available after pulling.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Generate Endpoint Tests
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void testOllamaGenerateEndpointWithSimplePrompt() throws Exception {
        mockMvc.perform(get("/api/v1/chat/ai/ollama/generate")
                        .param("message", "Translate to French: Hello, how can I help you today?")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.generatedText").exists())
                .andExpect(jsonPath("$.errorMessage").doesNotExist());
    }

    @Test
    void testOllamaGenerateEndpointWithTemperatureAndMaxTokens() throws Exception {
        mockMvc.perform(get("/api/v1/chat/ai/ollama/generate")
                        .param("message", "Write a short inspirational quote about AI.")
                        .param("temperature", "0.85")
                        .param("maxTokens", "300")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.generatedText").exists())
                .andExpect(jsonPath("$.errorMessage").doesNotExist());
    }

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

    @Test
    void testOllamaGenerateEndpointWithLongMessage() throws Exception {
        String longMessage = "a".repeat(15000);

        mockMvc.perform(get("/api/v1/chat/ai/ollama/generate")
                        .param("message", longMessage)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())   // or BadRequest depending on your exception handling
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bill Analysis Tests
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void testOllamaAnalyzeBillEndpointWithValidPDF() throws Exception {
        Resource pdfResource = resourceLoader.getResource("classpath:Facture-981-06-2025.pdf");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "Facture-981-06-2025.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                pdfResource.getInputStream().readAllBytes()
        );

        mockMvc.perform(multipart("/api/v1/chat/ai/ollama/analyze-bill")
                        .file(file)
                        .param("temperature", "0.7")
                        .param("maxTokens", "600")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.generatedText").exists())
                .andExpect(jsonPath("$.errorMessage").doesNotExist());
    }

    @Test
    void testOllamaAnalyzeBillEndpointWithEmptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.pdf", MediaType.APPLICATION_PDF_VALUE, new byte[0]);

        mockMvc.perform(multipart("/api/v1/chat/ai/ollama/analyze-bill")
                        .file(emptyFile)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    @Test
    void testOllamaAnalyzeBillEndpointWithInvalidFileType() throws Exception {
        MockMultipartFile txtFile = new MockMultipartFile(
                "file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "This is not a PDF".getBytes());

        mockMvc.perform(multipart("/api/v1/chat/ai/ollama/analyze-bill")
                        .file(txtFile)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CV Analysis Tests
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void testOllamaAnalyzeCVEndpointWithValidPDF() throws Exception {
        Resource pdfResource = resourceLoader.getResource("classpath:Sample-CV-2025.pdf");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "Sample-CV-2025.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                pdfResource.getInputStream().readAllBytes()
        );

        mockMvc.perform(multipart("/api/v1/chat/ai/ollama/analyze-cv")
                        .file(file)
                        .param("temperature", "0.6")
                        .param("maxTokens", "1200")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.generatedText").exists())
                .andExpect(jsonPath("$.errorMessage").doesNotExist());
    }

    @Test
    void testOllamaAnalyzeCVEndpointWithEmptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.pdf", MediaType.APPLICATION_PDF_VALUE, new byte[0]);

        mockMvc.perform(multipart("/api/v1/chat/ai/ollama/analyze-cv")
                        .file(emptyFile)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    @Test
    void testOllamaAnalyzeCVEndpointWithInvalidFileType() throws Exception {
        MockMultipartFile txtFile = new MockMultipartFile(
                "file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Not a PDF file".getBytes());

        mockMvc.perform(multipart("/api/v1/chat/ai/ollama/analyze-cv")
                        .file(txtFile)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").exists());
    }
}