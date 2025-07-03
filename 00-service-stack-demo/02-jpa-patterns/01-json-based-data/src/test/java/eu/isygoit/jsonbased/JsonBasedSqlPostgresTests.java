package eu.isygoit.jsonbased;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.constants.TenantConstants;
import eu.isygoit.jsonbased.utils.ITenantService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration tests for DISCRIMINATOR multi-tenancy strategy.
 * Verifies tenant isolation via tenant ID filtering on a shared table.
 */
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=update",
        "multi-tenancy.mode=GDM"
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
    @Value("${multi-tenancy.mode}")
    private String multiTenancyProperty;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        String baseUrl = postgres.getJdbcUrl();
        registry.add("spring.datasource.url", () -> baseUrl);

        String tenants = baseUrl.replace("/postgres", "/tenants");

        registry.add("multi-tenancy.tenants[0].id", () -> "tenants");
        registry.add("multi-tenancy.tenants[0].url", () -> tenants);
        registry.add("multi-tenancy.tenants[0].username", postgres::getUsername);
        registry.add("multi-tenancy.tenants[0].password", postgres::getPassword);
    }

    /**
     * Initialize database schema for tenant1 and tenant2 before all tests.
     */
    @BeforeAll
    static void initSharedSchema(@Autowired ITenantService tenantService) {
        tenantService.initializeTenantSchema("public");
    }
}
