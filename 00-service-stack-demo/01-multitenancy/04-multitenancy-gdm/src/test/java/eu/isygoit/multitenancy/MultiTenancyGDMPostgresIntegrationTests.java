package eu.isygoit.multitenancy;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.helper.JsonHelper;
import eu.isygoit.constants.TenantConstants;
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

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for DISCRIMINATOR multitenancy strategy.
 * Verifies tenant isolation via tenant ID filtering on a shared table.
 */
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create",
        "multitenancy.mode=GDM"
})
@ActiveProfiles("postgres")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
class MultiTenancyGDMPostgresIntegrationTests {

    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final String TENANT_1 = "tenant1";
    private static final String TENANT_2 = "tenant2";
    private static final String INVALID_TENANT = "unknown";
    private static final String SUPER_TENANT = TenantConstants.SUPER_TENANT_NAME;

    private static final String BASE_URL = "/api/tutorials";
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("postgres") // initial database
            .withUsername("postgres")
            .withPassword("root")
            .withInitScript("db/pg_init-multi-db.sql"); // creates tenant1 and tenant2
    private static Long tenant1TutorialId;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${multitenancy.mode}")
    private String multiTenancyProperty;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

        String baseUrl = postgres.getJdbcUrl();
        registry.add("spring.datasource.url", () -> baseUrl);

        String tenants = baseUrl.replace("/postgres", "/tenants");

        registry.add("multitenancy.tenants[0].id", () -> "tenants");
        registry.add("multitenancy.tenants[0].url", () -> tenants);
        registry.add("multitenancy.tenants[0].username", postgres::getUsername);
        registry.add("multitenancy.tenants[0].password", postgres::getPassword);
    }

    /**
     * Initialize database schema for tenant1 and tenant2 before all tests.
     */
    @BeforeAll
    static void initSharedSchema(@Autowired ITenantService tenantService) {
        tenantService.initializeTenantSchema("public");
    }

    private TutorialDto buildDto(String title) {
        return TutorialDto.builder()
                .title(title)
                .description("Learn Spring Boot with Discriminator strategy")
                .published(true)
                .build();
    }

    @Test
    @Order(0)
    void shouldValidateDiscriminatorMode() {
        Assertions.assertEquals("GDM", multiTenancyProperty);
    }

    @Test
    @Order(1)
    void shouldCreateTutorialForTenant1() throws Exception {
        var dto = buildDto("Tenant1 Tutorial");

        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonHelper.toJson(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenant").value(TENANT_1))
                .andReturn();

        tenant1TutorialId = objectMapper.readValue(result.getResponse().getContentAsString(), TutorialDto.class).getId();
        Assertions.assertNotNull(tenant1TutorialId);
    }

    @Test
    @Order(2)
    void shouldRejectAccessToOtherTenantData() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + tenant1TutorialId)
                        .header(TENANT_HEADER, TENANT_2))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(3)
    void shouldRetrieveOwnDataForTenant1() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + tenant1TutorialId)
                        .header(TENANT_HEADER, TENANT_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Tenant1 Tutorial"))
                .andExpect(jsonPath("$.tenant").value(TENANT_1));
    }

    @Test
    @Order(4)
    void shouldCreateTutorialForTenant2() throws Exception {
        var dto = buildDto("Tenant2 Tutorial");

        mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonHelper.toJson(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenant").value(TENANT_2));
    }

    @Test
    @Order(5)
    void shouldNotLeakDataToOtherTenants() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header(TENANT_HEADER, TENANT_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].tenant", everyItem(is(TENANT_1))))
                .andExpect(jsonPath("$[*].title", not(hasItem("Tenant2 Tutorial"))));

        mockMvc.perform(get(BASE_URL)
                        .header(TENANT_HEADER, TENANT_2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].tenant", everyItem(is(TENANT_2))))
                .andExpect(jsonPath("$[*].title", not(hasItem("Tenant1 Tutorial"))));
    }

    @Test
    @Order(6)
    void shouldRejectUpdateByOtherTenant() throws Exception {
        var updated = buildDto("Hacked Title");
        updated.setId(tenant1TutorialId);

        mockMvc.perform(put(BASE_URL + "/" + tenant1TutorialId)
                        .header(TENANT_HEADER, TENANT_2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonHelper.toJson(updated)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @Order(7)
    void shouldHandlePaginationForTenant1() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header(TENANT_HEADER, TENANT_1)
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].tenant", everyItem(is(TENANT_1))));
    }

    @Test
    @Order(8)
    void shouldRejectMissingTenantHeader() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(9)
    void shouldRejectUnknownTenant() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header(TENANT_HEADER, INVALID_TENANT))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(10)
    void shouldCreateMultipleTutorialsForTenant1() throws Exception {
        List<TutorialDto> tutorials = List.of(
                buildDto("Bulk 1"),
                buildDto("Bulk 2"),
                buildDto("Bulk 3")
        );

        mockMvc.perform(post(BASE_URL + "/batch")
                        .header(TENANT_HEADER, TENANT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonHelper.toJson(tutorials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @Order(11)
    void shouldSupportFilteringByCriteria() throws Exception {
        //cr1 = val1, OR cr2 != val2, AND cr3 > val3, OR cr4 >= val4, AND cr5 ~ val5
        String criteria = "title = 'Bulk 1'";

        mockMvc.perform(get(BASE_URL + "/filter?criteria=" + criteria)
                        .header(TENANT_HEADER, TENANT_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", everyItem(containsString("Bulk"))));
    }

    @Test
    @Order(12)
    void superTenantShouldAccessAllData() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header(TENANT_HEADER, SUPER_TENANT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].tenant", hasItems(TENANT_1, TENANT_2)));
    }
}
