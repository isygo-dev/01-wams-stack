package eu.isygoit;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.dto.TutorialDto;
import eu.isygoit.helper.JsonHelper;
import eu.isygoit.utils.ITenantService;
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

/**
 * Integration tests for the TutorialController with DATABASE multitenancy strategy.
 * This test suite verifies tenant isolation at the database level by simulating HTTP calls using MockMvc.
 */
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create",
        "app.tenancy.enabled=true",
        "app.tenancy.mode=DATABASE"
})
@ActiveProfiles("h2")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MultiTenancyDatabaseH2IntegrationTests {

    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final String TENANT_1 = "tenant1";
    private static final String TENANT_2 = "tenant2";

    private static Long tenant1TutorialId;

    private final String BASE_URL = "/api/tutorials";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.tenancy.mode}")
    private String multiTenancyProperty;

    /**
     * Initialize database model for tenant1 and tenant2 before all tests.
     */
    @BeforeAll
    static void initTenants(@Autowired ITenantService PGTenantService) {
        PGTenantService.initializeTenantSchema(TENANT_1);
        PGTenantService.initializeTenantSchema(TENANT_2);
    }

    /**
     * Utility method to build a standard TutorialDto object.
     */
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
        Assertions.assertEquals("DATABASE", multiTenancyProperty,
                "Expected multitenancy mode to be DATABASE");
    }

    /**
     * Create a tutorial for tenant1 and store its ID.
     */
    @Test
    @Order(1)
    void shouldCreateTutorialForTenant1() throws Exception {
        var dto = buildDto("Tenant1 Tutorial");

        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonHelper.toJson(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value(dto.getTitle()))
                .andExpect(jsonPath("$.tenant").value(TENANT_1))
                .andReturn();

        var response = objectMapper.readValue(result.getResponse().getContentAsString(), TutorialDto.class);
        tenant1TutorialId = response.getId();
    }

    /**
     * Ensure that tenant2 cannot access tenant1's data.
     */
    @Test
    @Order(2)
    void shouldNotFindTenant1TutorialFromTenant2() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + tenant1TutorialId)
                        .header(TENANT_HEADER, TENANT_2))
                .andExpect(status().isNotFound());
    }

    /**
     * Ensure that tenant1 can retrieve its own tutorial.
     */
    @Test
    @Order(3)
    void shouldGetTutorialByIdForTenant1() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + tenant1TutorialId)
                        .header(TENANT_HEADER, TENANT_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Tenant1 Tutorial"))
                .andExpect(jsonPath("$.tenant").value(TENANT_1));
    }

    /**
     * Create a separate tutorial for tenant2.
     */
    @Test
    @Order(4)
    void shouldCreateSeparateTutorialForTenant2() throws Exception {
        var dto = buildDto("Tenant2 Tutorial");

        mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonHelper.toJson(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value(dto.getTitle()))
                .andExpect(jsonPath("$.tenant").value(TENANT_2));
    }

    /**
     * Verify tenant1 only sees its own tutorial(s).
     */
    @Test
    @Order(5)
    void shouldReturnOnlyTenant1TutorialsForTenant1() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header(TENANT_HEADER, TENANT_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", hasItem("Tenant1 Tutorial")))
                .andExpect(jsonPath("$[*].title", not(hasItem("Tenant2 Tutorial"))));
    }

    /**
     * Verify tenant2 only sees its own tutorial(s).
     */
    @Test
    @Order(6)
    void shouldReturnOnlyTenant2TutorialsForTenant2() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header(TENANT_HEADER, TENANT_2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", hasItem("Tenant2 Tutorial")))
                .andExpect(jsonPath("$[*].title", not(hasItem("Tenant1 Tutorial"))));
    }

    /**
     * Delete tenant1's tutorial and verify it no longer exists.
     */
    @Test
    @Order(7)
    void shouldDeleteTutorialFromTenant1Only() throws Exception {
        // Delete tutorial
        mockMvc.perform(delete(BASE_URL + "/" + tenant1TutorialId)
                        .header(TENANT_HEADER, TENANT_1))
                .andExpect(status().isNoContent());

        // Confirm deletion
        mockMvc.perform(get(BASE_URL + "/" + tenant1TutorialId)
                        .header(TENANT_HEADER, TENANT_1))
                .andExpect(status().isNotFound());
    }
}