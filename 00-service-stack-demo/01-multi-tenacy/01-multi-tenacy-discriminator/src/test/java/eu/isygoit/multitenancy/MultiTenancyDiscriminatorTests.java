package eu.isygoit.multitenancy;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.multitenancy.dto.TutorialDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.jpa.properties.hibernate.multiTenancy=DISCRIMINATOR"
})
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MultiTenancyDiscriminatorTests {

    private static final String TEST_TENANT_ID = "test-tenant";
    private static Long createdTutorialId;
    private final String BASE_URL = "/api/tutorials";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${spring.jpa.properties.hibernate.multiTenancy}")
    private String multiTenancyProperty;

    private TutorialDto buildDto() {
        return TutorialDto.builder()
                // tenantId will be assigned by backend using header
                .title("Intro to Spring")
                .description("Learn Spring Boot with tests")
                .published(true)
                .build();
    }

    @Test
    @Order(0)
    void shouldValidateMultiTenancyProperty() {
        Assertions.assertEquals("DISCRIMINATOR", multiTenancyProperty);
    }

    @Test
    @Order(1)
    void shouldCreateTutorial() throws Exception {
        var tutorialDto = buildDto();

        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .header("X-Tenant-ID", TEST_TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tutorialDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Intro to Spring"))
                .andExpect(jsonPath("$.tenant").value(TEST_TENANT_ID)) // ✅ Check tenantId assigned
                .andReturn();

        var response = objectMapper.readValue(result.getResponse().getContentAsString(), TutorialDto.class);
        createdTutorialId = response.getId();
    }

    @Test
    @Order(2)
    void shouldGetTutorialById() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + createdTutorialId)
                        .header("X-Tenant-ID", TEST_TENANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdTutorialId))
                .andExpect(jsonPath("$.title").value("Intro to Spring"));
    }

    @Test
    @Order(3)
    void shouldGetAllTutorials() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header("X-Tenant-ID", TEST_TENANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[*].title", hasItem("Intro to Spring")));
    }

    @Test
    @Order(4)
    void shouldUpdateTutorial() throws Exception {
        var updatedDto = buildDto();
        updatedDto.setTitle("Updated Spring Boot");
        updatedDto.setPublished(false);

        mockMvc.perform(put(BASE_URL + "/" + createdTutorialId)
                        .header("X-Tenant-ID", TEST_TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Spring Boot"))
                .andExpect(jsonPath("$.published").value(false));
    }

    @Test
    @Order(5)
    void shouldDeleteTutorial() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + createdTutorialId)
                        .header("X-Tenant-ID", TEST_TENANT_ID))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL + "/" + createdTutorialId)
                        .header("X-Tenant-ID", TEST_TENANT_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(6)
    void shouldHandleEmptyListWhenNoTutorialsExist() throws Exception {
        mockMvc.perform(delete(BASE_URL)
                        .header("X-Tenant-ID", TEST_TENANT_ID))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL)
                        .header("X-Tenant-ID", TEST_TENANT_ID))
                .andExpect(status().isNoContent());
    }
}
