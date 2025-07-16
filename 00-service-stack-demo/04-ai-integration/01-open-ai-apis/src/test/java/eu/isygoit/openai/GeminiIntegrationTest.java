package eu.isygoit.openai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The type Gemini integration test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class GeminiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Test generate endpoint with simple prompt.
     *
     * @throws Exception the exception
     */
    @Test
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
    }
}