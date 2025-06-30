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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the TutorialController with DATABASE multi-tenancy strategy.
 * This test suite verifies tenant isolation at the database level by simulating HTTP calls using MockMvc.
 */
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=update",
        "multi-tenancy.mode=SCHEMA"
})
@ActiveProfiles("postgres")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
class MultiTenancySchemaPostgresTests {

    private static final String TENANT_1 = "tenant1";
    private static final String TENANT_2 = "tenant2";
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("postgres") // initial database
            .withUsername("postgres")
            .withPassword("root")
            .withInitScript("db/pg_init-multi-db.sql"); // creates tenant1 and tenant2
    private static Long tenant1TutorialId;
    private final String BASE_URL = "/api/tutorials";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${multi-tenancy.mode}")
    private String multiTenancyProperty;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        String baseUrl = postgres.getJdbcUrl();
        registry.add("spring.datasource.url", () -> baseUrl);

        String tenant1Url = baseUrl.replace("/postgres", "/tenants")
                .replace("public", "tenant1");
        String tenant2Url = baseUrl.replace("/postgres", "/tenants")
                .replace("public", "tenant2");

        registry.add("multi-tenancy.tenants[0].id", () -> "tenant1");
        registry.add("multi-tenancy.tenants[0].url", () -> tenant1Url);
        registry.add("multi-tenancy.tenants[0].username", postgres::getUsername);
        registry.add("multi-tenancy.tenants[0].password", postgres::getPassword);

        registry.add("multi-tenancy.tenants[1].id", () -> "tenant2");
        registry.add("multi-tenancy.tenants[1].url", () -> tenant2Url);
        registry.add("multi-tenancy.tenants[1].username", postgres::getUsername);
        registry.add("multi-tenancy.tenants[1].password", postgres::getPassword);
    }

    /**
     * Initialize database schema for tenant1 and tenant2 before all tests.
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
        Assertions.assertEquals("SCHEMA", multiTenancyProperty,
                "Expected multi-tenancy mode to be DATABASE");
    }

    /**
     * Create a tutorial for tenant1 and store its ID.
     */
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

    /**
     * Ensure that tenant2 cannot access tenant1's data.
     */
    @Test
    @Order(2)
    void shouldNotFindTenant1TutorialFromTenant2() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + tenant1TutorialId)
                        .header("X-Tenant-ID", TENANT_2))
                .andExpect(status().isNotFound());
    }

    /**
     * Ensure that tenant1 can retrieve its own tutorial.
     */
    @Test
    @Order(3)
    void shouldGetTutorialByIdForTenant1() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + tenant1TutorialId)
                        .header("X-Tenant-ID", TENANT_1))
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
                        .header("X-Tenant-ID", TENANT_2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
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
                        .header("X-Tenant-ID", TENANT_1))
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
                        .header("X-Tenant-ID", TENANT_2))
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
                        .header("X-Tenant-ID", TENANT_1))
                .andExpect(status().isNoContent());

        // Confirm deletion
        mockMvc.perform(get(BASE_URL + "/" + tenant1TutorialId)
                        .header("X-Tenant-ID", TENANT_1))
                .andExpect(status().isNotFound());
    }
}