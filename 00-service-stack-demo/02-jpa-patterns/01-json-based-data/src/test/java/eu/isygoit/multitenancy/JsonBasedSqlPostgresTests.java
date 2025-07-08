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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("postgres") // initial database
            .withUsername("postgres")
            .withPassword("root")
            .withInitScript("db/pg_init-multi-db.sql"); // creates tenant1 and tenant2

    // Test data storage
    private static UUID tenant1_userLoginId;
    private static UUID tenant2_userLoginId;
    private static UserLoginEventDto tenant1_userLogin;
    private static UserLoginEventDto tenant2_userLogin;
    private static List<UUID> tenant1_batchIds;
    private static List<UUID> tenant2_batchIds;

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

    private UserLoginEventDto buildDto(String userId, String ip, String device) {
        return UserLoginEventDto.builder()
                .userId(userId)
                .ip(ip)
                .device(device)
                .build();
    }

    // === CONFIGURATION TESTS ===

    @Test
    @Order(0)
    void shouldValidateDiscriminatorMode() {
        Assertions.assertEquals("GDM", multiTenancyProperty);
    }

    // === CREATE TESTS ===

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
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        tenant1_userLoginId = objectMapper.readValue(result.getResponse().getContentAsString(), UserLoginEventDto.class).getId();
        Assertions.assertNotNull(tenant1_userLoginId);
    }

    @Test
    @Order(2)
    void shouldCreateLoginEventForTenant2() throws Exception {
        tenant2_userLogin = buildDto("user_two");

        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .header("X-Tenant-ID", TENANT_2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tenant2_userLogin)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(tenant2_userLogin.getUserId()))
                .andExpect(jsonPath("$.ip").value(tenant2_userLogin.getIp()))
                .andExpect(jsonPath("$.device").value(tenant2_userLogin.getDevice()))
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        tenant2_userLoginId = objectMapper.readValue(result.getResponse().getContentAsString(), UserLoginEventDto.class).getId();
        Assertions.assertNotNull(tenant2_userLoginId);
        Assertions.assertNotEquals(tenant1_userLoginId, tenant2_userLoginId);
    }

    @Test
    @Order(3)
    void shouldCreateBatchLoginEventsForTenant1() throws Exception {
        List<UserLoginEventDto> batchEvents = Arrays.asList(
                buildDto("batch_user_1", "192.168.1.1", "Device1"),
                buildDto("batch_user_2", "192.168.1.2", "Device2"),
                buildDto("batch_user_3", "192.168.1.3", "Device3")
        );

        MvcResult result = mockMvc.perform(post(BASE_URL + "/batch")
                        .header("X-Tenant-ID", TENANT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batchEvents)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].userId").value("batch_user_1"))
                .andExpect(jsonPath("$[1].userId").value("batch_user_2"))
                .andExpect(jsonPath("$[2].userId").value("batch_user_3"))
                .andReturn();

        UserLoginEventDto[] createdEvents = objectMapper.readValue(result.getResponse().getContentAsString(), UserLoginEventDto[].class);
        tenant1_batchIds = Arrays.stream(createdEvents)
                .map(UserLoginEventDto::getId)
                .toList();

        Assertions.assertEquals(3, tenant1_batchIds.size());
        tenant1_batchIds.forEach(Assertions::assertNotNull);
    }

    @Test
    @Order(4)
    void shouldCreateBatchLoginEventsForTenant2() throws Exception {
        List<UserLoginEventDto> batchEvents = Arrays.asList(
                buildDto("batch_user_a", "10.0.0.1", "DeviceA"),
                buildDto("batch_user_b", "10.0.0.2", "DeviceB")
        );

        MvcResult result = mockMvc.perform(post(BASE_URL + "/batch")
                        .header("X-Tenant-ID", TENANT_2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batchEvents)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value("batch_user_a"))
                .andExpect(jsonPath("$[1].userId").value("batch_user_b"))
                .andReturn();

        UserLoginEventDto[] createdEvents = objectMapper.readValue(result.getResponse().getContentAsString(), UserLoginEventDto[].class);
        tenant2_batchIds = Arrays.stream(createdEvents)
                .map(UserLoginEventDto::getId)
                .toList();

        Assertions.assertEquals(2, tenant2_batchIds.size());
        tenant2_batchIds.forEach(Assertions::assertNotNull);
    }

    // === READ TESTS ===

    @Test
    @Order(5)
    void shouldRetrieveOwnDataForTenant1() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + tenant1_userLoginId)
                        .header("X-Tenant-ID", TENANT_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tenant1_userLoginId.toString()))
                .andExpect(jsonPath("$.userId").value(tenant1_userLogin.getUserId()))
                .andExpect(jsonPath("$.ip").value(tenant1_userLogin.getIp()))
                .andExpect(jsonPath("$.device").value(tenant1_userLogin.getDevice()));
    }

    @Test
    @Order(6)
    void shouldRetrieveOwnDataForTenant2() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + tenant2_userLoginId)
                        .header("X-Tenant-ID", TENANT_2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tenant2_userLoginId.toString()))
                .andExpect(jsonPath("$.userId").value(tenant2_userLogin.getUserId()))
                .andExpect(jsonPath("$.ip").value(tenant2_userLogin.getIp()))
                .andExpect(jsonPath("$.device").value(tenant2_userLogin.getDevice()));
    }

    @Test
    @Order(7)
    void shouldNotRetrieveTenant1DataFromTenant2() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + tenant1_userLoginId)
                        .header("X-Tenant-ID", TENANT_2))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(8)
    void shouldNotRetrieveTenant2DataFromTenant1() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + tenant2_userLoginId)
                        .header("X-Tenant-ID", TENANT_1))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(9)
    void shouldFindAllForTenant1() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header("X-Tenant-ID", TENANT_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4)) // 1 single + 3 batch
                .andExpect(jsonPath("$[?(@.userId == 'user_one')]").exists())
                .andExpect(jsonPath("$[?(@.userId == 'batch_user_1')]").exists())
                .andExpect(jsonPath("$[?(@.userId == 'batch_user_2')]").exists())
                .andExpect(jsonPath("$[?(@.userId == 'batch_user_3')]").exists());
    }

    @Test
    @Order(10)
    void shouldFindAllForTenant2() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header("X-Tenant-ID", TENANT_2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3)) // 1 single + 2 batch
                .andExpect(jsonPath("$[?(@.userId == 'user_two')]").exists())
                .andExpect(jsonPath("$[?(@.userId == 'batch_user_a')]").exists())
                .andExpect(jsonPath("$[?(@.userId == 'batch_user_b')]").exists());
    }

    @Test
    @Order(11)
    void shouldFindAllWithPagination() throws Exception {
        mockMvc.perform(get(BASE_URL + "/0/2")
                        .header("X-Tenant-ID", TENANT_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @Order(12)
    void shouldGetCountForTenant1() throws Exception {
        mockMvc.perform(get(BASE_URL + "/count")
                        .header("X-Tenant-ID", TENANT_1))
                .andExpect(status().isOk())
                .andExpect(content().string("4"));
    }

    @Test
    @Order(13)
    void shouldGetCountForTenant2() throws Exception {
        mockMvc.perform(get(BASE_URL + "/count")
                        .header("X-Tenant-ID", TENANT_2))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }

    @Test
    @Order(14)
    void shouldFindAllFullData() throws Exception {
        mockMvc.perform(get(BASE_URL + "/full")
                        .header("X-Tenant-ID", TENANT_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4));
    }

    // === UPDATE TESTS ===

    @Test
    @Order(15)
    void shouldUpdateLoginEventForTenant1() throws Exception {
        UserLoginEventDto updatedDto = buildDto("user_one_updated", "192.168.1.100", "Updated Device");
        updatedDto.setId(tenant1_userLoginId);

        mockMvc.perform(put(BASE_URL + "/" + tenant1_userLoginId)
                        .header("X-Tenant-ID", TENANT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tenant1_userLoginId.toString()))
                .andExpect(jsonPath("$.userId").value("user_one_updated"))
                .andExpect(jsonPath("$.ip").value("192.168.1.100"))
                .andExpect(jsonPath("$.device").value("Updated Device"));
    }

    @Test
    @Order(16)
    void shouldNotUpdateTenant1DataFromTenant2() throws Exception {
        UserLoginEventDto updatedDto = buildDto("malicious_update", "192.168.1.100", "Malicious Device");
        updatedDto.setId(tenant1_userLoginId);

        mockMvc.perform(put(BASE_URL + "/" + tenant1_userLoginId)
                        .header("X-Tenant-ID", TENANT_2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(17)
    void shouldUpdateLoginEventForTenant2() throws Exception {
        UserLoginEventDto updatedDto = buildDto("user_two_updated", "10.0.0.100", "Updated Device 2");
        updatedDto.setId(tenant2_userLoginId);

        mockMvc.perform(put(BASE_URL + "/" + tenant2_userLoginId)
                        .header("X-Tenant-ID", TENANT_2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tenant2_userLoginId.toString()))
                .andExpect(jsonPath("$.userId").value("user_two_updated"))
                .andExpect(jsonPath("$.ip").value("10.0.0.100"))
                .andExpect(jsonPath("$.device").value("Updated Device 2"));
    }

    // === DELETE TESTS ===

    @Test
    @Order(18)
    void shouldDeleteLoginEventForTenant1() throws Exception {
        // Delete one of the batch items
        UUID toDelete = tenant1_batchIds.get(0);

        mockMvc.perform(delete(BASE_URL + "/" + toDelete)
                        .header("X-Tenant-ID", TENANT_1))
                .andExpect(status().isNoContent());

        // Verify it's deleted
        mockMvc.perform(get(BASE_URL + "/" + toDelete)
                        .header("X-Tenant-ID", TENANT_1))
                .andExpect(status().isNotFound());

        // Verify count is reduced
        mockMvc.perform(get(BASE_URL + "/count")
                        .header("X-Tenant-ID", TENANT_1))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }

    @Test
    @Order(19)
    void shouldNotDeleteTenant1DataFromTenant2() throws Exception {
        UUID toDelete = tenant1_batchIds.get(1);

        mockMvc.perform(delete(BASE_URL + "/" + toDelete)
                        .header("X-Tenant-ID", TENANT_2))
                .andExpect(status().isNotFound());

        // Verify it still exists in tenant1
        mockMvc.perform(get(BASE_URL + "/" + toDelete)
                        .header("X-Tenant-ID", TENANT_1))
                .andExpect(status().isOk());
    }

    @Test
    @Order(20)
    void shouldDeleteLoginEventForTenant2() throws Exception {
        UUID toDelete = tenant2_batchIds.get(0);

        mockMvc.perform(delete(BASE_URL + "/" + toDelete)
                        .header("X-Tenant-ID", TENANT_2))
                .andExpect(status().isNoContent());

        // Verify it's deleted
        mockMvc.perform(get(BASE_URL + "/" + toDelete)
                        .header("X-Tenant-ID", TENANT_2))
                .andExpect(status().isNotFound());

        // Verify count is reduced
        mockMvc.perform(get(BASE_URL + "/count")
                        .header("X-Tenant-ID", TENANT_2))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
    }

    // === ERROR HANDLING TESTS ===

    @Test
    @Order(21)
    void shouldRejectRequestWithoutTenantHeader() throws Exception {
        UserLoginEventDto newDto = buildDto("no_tenant");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(22)
    void shouldRejectRequestWithInvalidTenant() throws Exception {
        UserLoginEventDto newDto = buildDto("invalid_tenant");

        mockMvc.perform(post(BASE_URL)
                        .header("X-Tenant-ID", INVALID_TENANT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(23)
    void shouldHandleNonExistentId() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get(BASE_URL + "/" + nonExistentId)
                        .header("X-Tenant-ID", TENANT_1))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(24)
    void shouldHandleInvalidIdFormat() throws Exception {
        mockMvc.perform(get(BASE_URL + "/invalid-uuid")
                        .header("X-Tenant-ID", TENANT_1))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(25)
    void shouldHandleEmptyBatchCreation() throws Exception {
        mockMvc.perform(post(BASE_URL + "/batch")
                        .header("X-Tenant-ID", TENANT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest());
    }

    // === FILTERING TESTS ===

    @Test
    @Order(26)
    void shouldFilterByUserId() throws Exception {
        mockMvc.perform(get(BASE_URL + "/filter")
                        .header("X-Tenant-ID", TENANT_1)
                        .param("criteria", "userId=user_one_updated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value("user_one_updated"));
    }

    @Test
    @Order(27)
    void shouldFilterByDevice() throws Exception {
        mockMvc.perform(get(BASE_URL + "/filter")
                        .header("X-Tenant-ID", TENANT_1)
                        .param("criteria", "device~Device"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2)); // batch_user_2 and batch_user_3
    }

    @Test
    @Order(28)
    void shouldGetFilterCriteria() throws Exception {
        mockMvc.perform(get(BASE_URL + "/filter/criteria")
                        .header("X-Tenant-ID", TENANT_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    // === CROSS-TENANT VALIDATION TESTS ===

    @Test
    @Order(29)
    void shouldEnsureCompleteDataIsolation() throws Exception {
        // Get all data from tenant1
        MvcResult tenant1Result = mockMvc.perform(get(BASE_URL)
                        .header("X-Tenant-ID", TENANT_1))
                .andExpect(status().isOk())
                .andReturn();

        UserLoginEventDto[] tenant1Data = objectMapper.readValue(
                tenant1Result.getResponse().getContentAsString(), UserLoginEventDto[].class);

        // Get all data from tenant2
        MvcResult tenant2Result = mockMvc.perform(get(BASE_URL)
                        .header("X-Tenant-ID", TENANT_2))
                .andExpect(status().isOk())
                .andReturn();

        UserLoginEventDto[] tenant2Data = objectMapper.readValue(
                tenant2Result.getResponse().getContentAsString(), UserLoginEventDto[].class);

        // Verify no data overlap
        List<UUID> tenant1Ids = Arrays.stream(tenant1Data)
                .map(UserLoginEventDto::getId)
                .toList();
        List<UUID> tenant2Ids = Arrays.stream(tenant2Data)
                .map(UserLoginEventDto::getId)
                .toList();

        // Assert no common IDs between tenants
        Assertions.assertTrue(tenant1Ids.stream().noneMatch(tenant2Ids::contains),
                "Tenant data should be completely isolated");
        Assertions.assertTrue(tenant2Ids.stream().noneMatch(tenant1Ids::contains),
                "Tenant data should be completely isolated");
    }

    @Test
    @Order(30)
    void shouldValidateTotalDataIntegrity() throws Exception {
        // Verify final counts
        mockMvc.perform(get(BASE_URL + "/count")
                        .header("X-Tenant-ID", TENANT_1))
                .andExpect(status().isOk())
                .andExpect(content().string("3")); // 1 updated + 2 remaining batch

        mockMvc.perform(get(BASE_URL + "/count")
                        .header("X-Tenant-ID", TENANT_2))
                .andExpect(status().isOk())
                .andExpect(content().string("2")); // 1 updated + 1 remaining batch
    }

    // === CLEANUP TESTS ===

    @Test
    @Order(31)
    void shouldCleanupTenant1Data() throws Exception {
        // Delete remaining data for tenant1
        mockMvc.perform(delete(BASE_URL + "/" + tenant1_userLoginId)
                        .header("X-Tenant-ID", TENANT_1))
                .andExpect(status().isOk());

        // Delete remaining batch items
        for (UUID id : tenant1_batchIds.subList(1, tenant1_batchIds.size())) {
            mockMvc.perform(delete(BASE_URL + "/" + id)
                            .header("X-Tenant-ID", TENANT_1))
                    .andExpect(status().isOk());
        }

        // Verify empty
        mockMvc.perform(get(BASE_URL + "/count")
                        .header("X-Tenant-ID", TENANT_1))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    @Test
    @Order(32)
    void shouldCleanupTenant2Data() throws Exception {
        // Delete remaining data for tenant2
        mockMvc.perform(delete(BASE_URL + "/" + tenant2_userLoginId)
                        .header("X-Tenant-ID", TENANT_2))
                .andExpect(status().isOk());

        // Delete remaining batch items
        for (UUID id : tenant2_batchIds.subList(1, tenant2_batchIds.size())) {
            mockMvc.perform(delete(BASE_URL + "/" + id)
                            .header("X-Tenant-ID", TENANT_2))
                    .andExpect(status().isOk());
        }

        // Verify empty
        mockMvc.perform(get(BASE_URL + "/count")
                        .header("X-Tenant-ID", TENANT_2))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }
}