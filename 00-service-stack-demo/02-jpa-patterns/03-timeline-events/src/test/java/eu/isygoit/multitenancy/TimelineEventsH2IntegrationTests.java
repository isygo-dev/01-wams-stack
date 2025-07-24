package eu.isygoit.multitenancy;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create",
        "multitenancy.mode=GDM"
})
@ActiveProfiles("h2")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TimelineEventsH2IntegrationTests {

    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final String TENANT_1 = "tenant1";
    private static final String TENANT_2 = "tenant2";
    private static final String INVALID_TENANT = "unknown";
    private static final String SUPER_TENANT = TenantConstants.SUPER_TENANT_NAME;

    private static final String BASE_URL = "/api/tutorials";

    private static Long tenant1TutorialId;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${multitenancy.mode}")
    private String multiTenancyProperty;

    @BeforeAll
    static void initSharedSchema(@Autowired ITenantService tenantService) {
        tenantService.initializeTenantSchema("public");
    }
}
