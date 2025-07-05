package eu.isygoit.multitenancy;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.multitenancy.dto.TutorialDto;
import eu.isygoit.multitenancy.utils.ITenantService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=update",
        "multitenancy.mode=SCHEMA"
})
@ActiveProfiles("h2")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MultiTenancySchemaH2Tests {

    private static final String TENANT_1 = "tenant1";
    private static final String TENANT_2 = "tenant2";

    private static Long tenant1TutorialId;

    private final String BASE_URL = "/api/tutorials";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${multitenancy.mode}")
    private String multiTenancyProperty;

    @BeforeAll
    static void initTenants(@Autowired ITenantService h2TenantService) {
        h2TenantService.initializeTenantSchema(TENANT_1);
        h2TenantService.initializeTenantSchema(TENANT_2);
    }

    private TutorialDto buildDto(String title) {
        return TutorialDto.builder()
                .title(title)
                .description("Learn Spring Boot with DB-per-tenant")
                .published(true)
                .build();
    }

    @Test
    @Order(0)
    void shouldValidateMultiTenancyProperty() {
        Assertions.assertEquals("SCHEMA", multiTenancyProperty,
                "Expected multitenancy mode to be SCHEMA");
    }

    @Test
    @Order(1)
    void shouldCreateTutorialForTenant1() throws Exception {
        var dto = buildDto("Tenant1 Tutorial");

        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .header("X-Tenant-ID", TENANT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value(dto.getTitle()))
                .andExpect(jsonPath("$.tenant").value(TENANT_1))
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
                .andExpect(jsonPath("$.title").value("Tenant1 Tutorial"))
                .andExpect(jsonPath("$.tenant").value(TENANT_1));
    }

    @Test
    @Order(4)
    void shouldCreateSeparateTutorialForTenant2() throws Exception {
        var dto = buildDto("Tenant2 Tutorial");

        mockMvc.perform(post(BASE_URL)
                        .header("X-Tenant-ID", TENANT_2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value(dto.getTitle()))
                .andExpect(jsonPath("$.tenant").value(TENANT_2));
    }

    @Test
    @Order(5)
    void shouldReturnOnlyTenant1TutorialsForTenant1() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header("X-Tenant-ID", TENANT_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", hasItem("Tenant1 Tutorial")))
                .andExpect(jsonPath("$[*].title", not(hasItem("Tenant2 Tutorial"))));
    }

    @Test
    @Order(6)
    void shouldReturnOnlyTenant2TutorialsForTenant2() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header("X-Tenant-ID", TENANT_2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", hasItem("Tenant2 Tutorial")))
                .andExpect(jsonPath("$[*].title", not(hasItem("Tenant1 Tutorial"))));
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