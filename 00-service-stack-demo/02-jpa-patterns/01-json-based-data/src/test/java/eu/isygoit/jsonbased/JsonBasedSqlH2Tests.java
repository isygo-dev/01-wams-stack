package eu.isygoit.jsonbased;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.constants.TenantConstants;
import eu.isygoit.jsonbased.dto.UserLoginEventDto;
import eu.isygoit.jsonbased.utils.ITenantService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=update",
        "multi-tenancy.mode=GDM"
})
@ActiveProfiles("h2")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JsonBasedSqlH2Tests {

    private static final String TENANT_1 = "tenant1";
    private static final String TENANT_2 = "tenant2";
    private static final String INVALID_TENANT = "unknown";
    private static final String SUPER_TENANT = TenantConstants.SUPER_TENANT_NAME;

    private static final String BASE_URL = "/api/userlogin";

    private static UUID tenant1_userLoginId;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${multi-tenancy.mode}")
    private String multiTenancyProperty;

    @BeforeAll
    static void initSharedSchema(@Autowired ITenantService tenantService) {
        tenantService.initializeTenantSchema("public");
    }

    private UserLoginEventDto buildDto(String userId) {
        return UserLoginEventDto.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .ip("127.0.0.1")
                .device("Lenov0")
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
        var dto = buildDto("user_one");

        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .header("X-Tenant-ID", TENANT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenant").value(TENANT_1))
                .andReturn();

        tenant1_userLoginId = objectMapper.readValue(result.getResponse().getContentAsString(), UserLoginEventDto.class).getId();
        Assertions.assertNotNull(tenant1_userLoginId);
    }
}
