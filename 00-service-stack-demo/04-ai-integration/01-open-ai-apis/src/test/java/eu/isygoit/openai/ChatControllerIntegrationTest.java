package eu.isygoit.openai;

import eu.isygoit.openai.dto.GeminiResponse;
import eu.isygoit.openai.service.GeminiApiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class ChatControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGenerateEndpointWithSimplePrompt() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/chat/ai/generate")
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