package eu.isygoit.multitenancy;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.constants.TenantConstants;
import eu.isygoit.multitenancy.dto.UserLoginEventDto;
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

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for DISCRIMINATOR multitenancy strategy.
 * Verifies tenant isolation via tenant ID filtering on a shared table.
 */
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=update",
        "multitenancy.mode=GDM"
})
@ActiveProfiles("postgres")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
class JsonBasedSqlPostgresTests {

    private static final String TENANT_1 = "tenant1";
    private static final String TENANT_2 = "tenant2";
    private static final String INVALID_TENANT = "unknown";
    private static final String SUPER_TENANT = TenantConstants.SUPER_TENANT_NAME;

    private static final String BASE_URL = "/api/userlogin";

    private static UUID tenant1_userLoginId;
    private static UserLoginEventDto tenant1_userLogin;

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

    private UserLoginEventDto buildDto(String userId) {
        return UserLoginEventDto.builder()
                .userId(userId)
                .ip("127.0.0.1")
                .device("Lenovo")
                .build();
    }

    @Test
    @Order(0)
    void shouldValidateDiscriminatorMode() {
        Assertions.assertEquals("GDM", multiTenancyProperty);
    }

    @Test
    @Order(1)
    void shouldCreateLoginEventForTenant1() throws Exception {
        tenant1_userLogin = buildDto("user_one");

        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .header("X-Tenant-ID", TENANT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tenant1_userLogin)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(tenant1_userLogin.getUserId()))
                .andExpect(jsonPath("$.ip").value(tenant1_userLogin.getIp()))
                .andExpect(jsonPath("$.device").value(tenant1_userLogin.getDevice()))
                .andReturn();

        tenant1_userLoginId = objectMapper.readValue(result.getResponse().getContentAsString(), UserLoginEventDto.class).getId();
        Assertions.assertNotNull(tenant1_userLoginId);
    }

    @Test
    @Order(2)
    void shouldRetrieveOwnDataForTenant1() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + tenant1_userLoginId)
                        .header("X-Tenant-ID", TENANT_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(tenant1_userLogin.getUserId()))
                .andExpect(jsonPath("$.ip").value(tenant1_userLogin.getIp()))
                .andExpect(jsonPath("$.device").value(tenant1_userLogin.getDevice()));
    }
}
