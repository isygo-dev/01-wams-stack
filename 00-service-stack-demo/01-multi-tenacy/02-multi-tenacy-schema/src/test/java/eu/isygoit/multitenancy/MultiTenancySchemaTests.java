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

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.jpa.properties.hibernate.multiTenancy=SCHEMA"
})
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MultiTenancySchemaTests {

    private static final String TENANT_1 = "TENANT1";
    private static final String TENANT_2 = "TENANT2";
    private static Long tenant1TutorialId;
    private final String BASE_URL = "/api/tutorials";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${spring.jpa.properties.hibernate.multiTenancy}")
    private String multiTenancyProperty;

    private TutorialDto buildDto() {
        return TutorialDto.builder()
                .title("Intro to Spring")
                .description("Learn Spring Boot with schema-based multi-tenancy")
                .published(true)
                .build();
    }

    @Test
    @Order(0)
    void shouldValidateMultiTenancyProperty() {
        Assertions.assertEquals("SCHEMA", multiTenancyProperty);
    }

    @Test
    @Order(1)
    void shouldCreateTutorialForTenant1() throws Exception {
        var dto = buildDto();

        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .header("X-Tenant-ID", TENANT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Intro to Spring"))
                .andExpect(jsonPath("$.tenant").value(TENANT_1)) // ✅ Check tenantId assigned
                .andReturn();

        var response = objectMapper.readValue(result.getResponse().getContentAsString(), TutorialDto.class);
        tenant1TutorialId = response.getId();
    }

    @Test
    @Order(2)
    void shouldNotFindTenant1TutorialFromTenant2() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + tenant1TutorialId)
                        .header("X-Tenant-ID", TENANT_2))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(3)
    void shouldGetTutorialByIdForTenant1() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + tenant1TutorialId)
                        .header("X-Tenant-ID", TENANT_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Intro to Spring"))
                .andExpect(jsonPath("$.tenant").value(TENANT_1)); // ✅ Check tenantId assigned
    }

    @Test
    @Order(4)
    void shouldCreateSeparateTutorialForTenant2() throws Exception {
        var dto = buildDto();
        dto.setTitle("Tenant2 Tutorial");

        mockMvc.perform(post(BASE_URL)
                        .header("X-Tenant-ID", TENANT_2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Tenant2 Tutorial"))
                .andExpect(jsonPath("$.tenant").value(TENANT_2)) // ✅ Check tenantId assigned
                .andReturn();
    }

    @Test
    @Order(5)
    void shouldReturnOnlyTenant1TutorialsForTenant1() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header("X-Tenant-ID", TENANT_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", hasItem("Intro to Spring")))
                .andExpect(jsonPath("$[*].title", not(hasItem("Tenant2 Tutorial"))))
                .andExpect(jsonPath("$[*].tenant", not(hasItem(TENANT_2)))); // ✅ Check tenantId assigned
    }

    @Test
    @Order(6)
    void shouldReturnOnlyTenant2TutorialsForTenant2() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header("X-Tenant-ID", TENANT_2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", hasItem("Tenant2 Tutorial")))
                .andExpect(jsonPath("$[*].title", not(hasItem("Intro to Spring"))))
                .andExpect(jsonPath("$[*].tenant", not(hasItem(TENANT_1)))); // ✅ Check tenantId assigned
    }

    @Test
    @Order(7)
    void shouldDeleteTutorialFromTenant1Only() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + tenant1TutorialId)
                        .header("X-Tenant-ID", TENANT_1))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL + "/" + tenant1TutorialId)
                        .header("X-Tenant-ID", TENANT_1))
                .andExpect(status().isNotFound());
    }
}
