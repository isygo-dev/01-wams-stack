package eu.isygoit.multitenancy;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.multitenancy.dto.AccountDto;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for validating JSON-embedded AccountEntity operations.
 * Tests focus on CRUD operations, edge cases, and data integrity in a multitenant environment.
 */
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=update",
        "multitenancy.mode=GDM"
})
@ActiveProfiles("postgres")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
class BasicCrudIntegrationTests {

    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final String TENANT_ID = "tenants";
    private static final String BASE_URL = "/api/v1/account";
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("postgres")
            .withUsername("postgres")
            .withPassword("root")
            .withInitScript("db/pg_init-multi-db.sql");
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

    @BeforeAll
    static void initSharedSchema(@Autowired ITenantService tenantService) {
        tenantService.initializeTenantSchema("public");
    }

    @Test
    @Order(1)
    void testCreateAccount() throws Exception {
        AccountDto accountDto = AccountDto.builder()
                .tenant(TENANT_ID)
                .login("testuser")
                .email("testuser@example.com")
                .passkey("securepass123")
                .build();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TENANT_HEADER, TENANT_ID)
                        .content(objectMapper.writeValueAsString(accountDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.tenant").value(TENANT_ID))
                .andExpect(jsonPath("$.login").value("testuser"))
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.passkey").value("securepass123"));
    }

    @Test
    @Order(2)
    void testCreateAccountWithInvalidEmail() throws Exception {
        AccountDto accountDto = AccountDto.builder()
                .tenant(TENANT_ID)
                .login("invaliduser")
                .email("invalid-email")
                .passkey("securepass123")
                .build();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TENANT_HEADER, TENANT_ID)
                        .content(objectMapper.writeValueAsString(accountDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    void testCreateAccountWithMissingTenant() throws Exception {
        AccountDto accountDto = AccountDto.builder()
                .login("notenantuser")
                .email("notenant@example.com")
                .passkey("securepass123")
                .build();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    void testCreateDuplicateLogin() throws Exception {
        AccountDto accountDto = AccountDto.builder()
                .tenant(TENANT_ID)
                .login("testuser") // Same as in testCreateAccount
                .email("different@example.com")
                .passkey("securepass123")
                .build();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TENANT_HEADER, TENANT_ID)
                        .content(objectMapper.writeValueAsString(accountDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    void testCreateBatchAccounts() throws Exception {
        List<AccountDto> accountDtos = List.of(
                AccountDto.builder()
                        .tenant(TENANT_ID)
                        .login("batchuser1")
                        .email("batchuser1@example.com")
                        .passkey("pass123")
                        .build(),
                AccountDto.builder()
                        .tenant(TENANT_ID)
                        .login("batchuser2")
                        .email("batchuser2@example.com")
                        .passkey("pass456")
                        .build()
        );

        mockMvc.perform(post(BASE_URL + "/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TENANT_HEADER, TENANT_ID)
                        .content(objectMapper.writeValueAsString(accountDtos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].login").value("batchuser1"))
                .andExpect(jsonPath("$[1].login").value("batchuser2"));
    }

    @Test
    @Order(6)
    void testConcurrentCreateAccounts() throws Exception {
        int threadCount = 5;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    AccountDto accountDto = AccountDto.builder()
                            .tenant(TENANT_ID)
                            .login("concurrentuser" + index)
                            .email("concurrent" + index + "@example.com")
                            .passkey("pass" + index)
                            .build();

                    mockMvc.perform(post(BASE_URL)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .header(TENANT_HEADER, TENANT_ID)
                                    .content(objectMapper.writeValueAsString(accountDto)))
                            .andExpect(status().isCreated());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        mockMvc.perform(get(BASE_URL + "/count")
                        .header(TENANT_HEADER, TENANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(8)); // 1 + 2 + 5
    }

    @Test
    @Order(7)
    void testFindAllAccounts() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header(TENANT_HEADER, TENANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(8));
    }

    @Test
    @Order(8)
    void testFindAllAccountsPaged() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header(TENANT_HEADER, TENANT_ID)
                        .param("page", "0")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(10)
    void testFindAllFullAccounts() throws Exception {
        mockMvc.perform(get(BASE_URL + "/full")
                        .header(TENANT_HEADER, TENANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(8));
    }

    @Test
    @Order(11)
    void testFindAccountById() throws Exception {
        AccountDto accountDto = AccountDto.builder()
                .tenant(TENANT_ID)
                .login("finduser")
                .email("finduser@example.com")
                .passkey("findpass")
                .build();

        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TENANT_HEADER, TENANT_ID)
                        .content(objectMapper.writeValueAsString(accountDto)))
                .andExpect(status().isCreated())
                .andReturn();

        AccountDto createdAccount = objectMapper.readValue(
                result.getResponse().getContentAsString(), AccountDto.class);

        mockMvc.perform(get(BASE_URL + "/" + createdAccount.getId())
                        .header(TENANT_HEADER, TENANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdAccount.getId()))
                .andExpect(jsonPath("$.login").value("finduser"));
    }

    @Test
    @Order(12)
    void testFindAccountByInvalidId() throws Exception {
        mockMvc.perform(get(BASE_URL + "/999999")
                        .header(TENANT_HEADER, TENANT_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(13)
    void testUpdateAccount() throws Exception {
        AccountDto accountDto = AccountDto.builder()
                .tenant(TENANT_ID)
                .login("updateuser")
                .email("updateuser@example.com")
                .passkey("updatepass")
                .build();

        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TENANT_HEADER, TENANT_ID)
                        .content(objectMapper.writeValueAsString(accountDto)))
                .andExpect(status().isCreated())
                .andReturn();

        AccountDto createdAccount = objectMapper.readValue(
                result.getResponse().getContentAsString(), AccountDto.class);

        AccountDto updatedDto = AccountDto.builder()
                .tenant(TENANT_ID)
                .login("updateuser")
                .email("updateduser@example.com")
                .passkey("newpass")
                .build();

        mockMvc.perform(put(BASE_URL + "/" + createdAccount.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TENANT_HEADER, TENANT_ID)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updateduser@example.com"))
                .andExpect(jsonPath("$.passkey").value("newpass"));
    }

    @Test
    @Order(15)
    void testGetCount() throws Exception {
        mockMvc.perform(get(BASE_URL + "/count")
                        .header(TENANT_HEADER, TENANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(10)); // Accounts from previous tests
    }

    @Test
    @Order(16)
    void testFindAllFilteredByMultipleCriteria() throws Exception {
        mockMvc.perform(get(BASE_URL + "/filter")
                        .header(TENANT_HEADER, TENANT_ID)
                        .param("criteria", "login='testuser' | login='batchuser1'"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.login=='testuser')]").exists())
                .andExpect(jsonPath("$[?(@.login=='batchuser1')]").exists());
    }

    @Test
    @Order(17)
    void testFindAllFilteredByCriteriaPaged() throws Exception {
        mockMvc.perform(get(BASE_URL + "/filter")
                        .header(TENANT_HEADER, TENANT_ID)
                        .param("page", "0")
                        .param("size", "1")
                        .param("criteria", "login='testuser' | login='batchuser1'"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(20)
    void testDeleteAccount() throws Exception {
        AccountDto accountDto = AccountDto.builder()
                .tenant(TENANT_ID)
                .login("deleteuser")
                .email("deleteuser@example.com")
                .passkey("deletepass")
                .build();

        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TENANT_HEADER, TENANT_ID)
                        .content(objectMapper.writeValueAsString(accountDto)))
                .andExpect(status().isCreated())
                .andReturn();

        AccountDto createdAccount = objectMapper.readValue(
                result.getResponse().getContentAsString(), AccountDto.class);

        mockMvc.perform(delete(BASE_URL + "/" + createdAccount.getId())
                        .header(TENANT_HEADER, TENANT_ID))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL + "/" + createdAccount.getId())
                        .header(TENANT_HEADER, TENANT_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(21)
    void testDeleteNonExistentAccount() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/999999")
                        .header(TENANT_HEADER, TENANT_ID))
                .andExpect(status().isNotFound());
    }
}