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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        //"spring.jpa.hibernate.ddl-auto=create",
        "app.tenancy.enabled=true",
        "app.tenancy.mode=SCHEMA"
})
@ActiveProfiles("h2")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MultiTenancySchemaH2IntegrationTests {

    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final String TENANT_1 = "tenant1";
    private static final String TENANT_2 = "tenant2";

    private static Long tenant1TutorialId;

    private final String BASE_URL = "/api/v1/tutorials";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.tenancy.mode}")
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

    @Test
    @Order(2)
    void shouldNotFindTenant1TutorialFromTenant2() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + tenant1TutorialId)
                        .header(TENANT_HEADER, TENANT_2))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(3)
    void shouldGetTutorialByIdForTenant1() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + tenant1TutorialId)
                        .header(TENANT_HEADER, TENANT_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Tenant1 Tutorial"))
                .andExpect(jsonPath("$.tenant").value(TENANT_1));
    }

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

    @Test
    @Order(5)
    void shouldReturnOnlyTenant1TutorialsForTenant1() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header(TENANT_HEADER, TENANT_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", hasItem("Tenant1 Tutorial")))
                .andExpect(jsonPath("$[*].title", not(hasItem("Tenant2 Tutorial"))));
    }

    @Test
    @Order(6)
    void shouldReturnOnlyTenant2TutorialsForTenant2() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header(TENANT_HEADER, TENANT_2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", hasItem("Tenant2 Tutorial")))
                .andExpect(jsonPath("$[*].title", not(hasItem("Tenant1 Tutorial"))));
    }

    @Test
    @Order(7)
    void shouldDeleteTutorialFromTenant1Only() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + tenant1TutorialId)
                        .header(TENANT_HEADER, TENANT_1))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL + "/" + tenant1TutorialId)
                        .header(TENANT_HEADER, TENANT_1))
                .andExpect(status().isNotFound());

        // Re-create it for subsequent tests
        var dto = buildDto("Tenant1 Tutorial");
        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonHelper.toJson(dto)))
                .andExpect(status().isCreated())
                .andReturn();
        tenant1TutorialId = objectMapper.readValue(result.getResponse().getContentAsString(), TutorialDto.class).getId();
    }

    @Test
    @Order(8)
    void shouldFilterByTitle() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("title", "Tutorial")
                        .header(TENANT_HEADER, TENANT_2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Tenant2 Tutorial"));
    }

    @Test
    @Order(9)
    void shouldGetOnlyPublishedTutorials() throws Exception {
        mockMvc.perform(get(BASE_URL + "/published")
                        .header(TENANT_HEADER, TENANT_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].published", everyItem(is(true))));
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
    void shouldDeleteAllTenantTutorials() throws Exception {
        mockMvc.perform(delete(BASE_URL)
                        .header(TENANT_HEADER, TENANT_1))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL)
                        .header(TENANT_HEADER, TENANT_1))
                .andExpect(status().isNoContent());
    }
}