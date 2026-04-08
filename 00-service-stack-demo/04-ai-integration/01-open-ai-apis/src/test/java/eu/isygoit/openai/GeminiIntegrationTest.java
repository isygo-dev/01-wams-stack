package eu.isygoit.openai;

import eu.isygoit.openai.dto.GeminiResponse;
import eu.isygoit.openai.service.GeminiApiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The type Gemini integration test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // optional but good
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        CassandraAutoConfiguration.class,
        CassandraDataAutoConfiguration.class
})
public class GeminiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private GeminiApiService geminiApiService;   // Mock the service

    /**
     * Test generate endpoint with simple prompt.
     *
     * @throws Exception the exception
     */
    /*@Test
    void testGenerateEndpointWithSimplePrompt() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/chat/ai/gemini/generate")
                        .param("message", "Translate to arabic: Hello, how can I help you")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.generatedText").exists())
                .andExpect(jsonPath("$.errorMessage").doesNotExist())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
    }*/
    @Test
    void testGenerateEndpointWithSimplePrompt() throws Exception {
        when(geminiApiService.generateContent(anyString()))
                .thenReturn(GeminiResponse.success("مرحبا، كيف يمكنني مساعدتك؟"));   // Arabic translation

        mockMvc.perform(get("/api/v1/chat/ai/gemini/generate")
                        .param("message", "Translate to arabic: Hello, how can I help you")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.generatedText").value("مرحبا، كيف يمكنني مساعدتك؟"));
    }
}