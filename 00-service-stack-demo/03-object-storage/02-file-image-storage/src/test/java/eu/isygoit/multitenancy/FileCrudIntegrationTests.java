package eu.isygoit.multitenancy;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.multitenancy.dto.ContractDto;
import eu.isygoit.multitenancy.utils.ITenantService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=update",
        "multitenancy.mode=GDM",
        "spring.jpa.hibernate.ddl-auto=create"
})
@ActiveProfiles("postgres")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
class FileCrudIntegrationTests {

    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final String TENANT_ID = "tenants";
    private static final String BASE_URL = "/api/v1/contract";
    private static final String FILE_URL = BASE_URL + "/file";

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

    @BeforeEach
    void cleanUp() throws Exception {
        MvcResult result = mockMvc.perform(get(BASE_URL)
                        .header(TENANT_HEADER, TENANT_ID))
                .andDo(print())
                .andExpect(status().is(anyOf(is(HttpStatus.OK.value()), is(HttpStatus.NO_CONTENT.value()))))
                .andReturn();

        if (result.getResponse().getStatus() == HttpStatus.OK.value()) {
            List<ContractDto> contracts = objectMapper.readValue(result.getResponse().getContentAsString(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ContractDto.class));

            for (ContractDto contract : contracts) {
                mockMvc.perform(delete(BASE_URL + "/{id}", contract.getId())
                                .header(TENANT_HEADER, TENANT_ID))
                        .andDo(print())
                        .andExpect(status().isNoContent());
            }
        }
    }

    @Test
    @Order(1)
    void testCreateContract() throws Exception {
        ContractDto contractDto = ContractDto.builder()
                .tenant(TENANT_ID)
                .code("CON001")
                .title("Test Contract")
                .description("Test contract description")
                .startDate(LocalDate.now().toString())
                .endDate(LocalDate.now().plusDays(30).toString())
                .active(true)
                .build();

        mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contractDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.code").value("CON001"))
                .andExpect(jsonPath("$.title").value("Test Contract"))
                .andExpect(jsonPath("$.description").value("Test contract description"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @Order(2)
    void testCreateContractWithFile() throws Exception {
        ContractDto contractDto = ContractDto.builder()
                .tenant(TENANT_ID)
                .code("CON002")
                .title("Contract with File")
                .description("Contract with file upload")
                .startDate(LocalDate.now().toString())
                .endDate(LocalDate.now().plusDays(30).toString())
                .active(true)
                .build();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Test file content".getBytes(StandardCharsets.UTF_8));

        MockMultipartFile dtoPart = new MockMultipartFile(
                "dto",
                "contract.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(contractDto).getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart(FILE_URL)
                        .file(file)
                        .file(dtoPart)
                        .header(TENANT_HEADER, TENANT_ID))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.code").value("CON002"))
                .andExpect(jsonPath("$.title").value("Contract with File"))
                .andExpect(jsonPath("$.originalFileName").value("test.pdf"));
    }

    @Test
    @Order(3)
    void testUpdateContract() throws Exception {
        ContractDto contractDto = ContractDto.builder()
                .tenant(TENANT_ID)
                .code("CON003")
                .title("Original Contract")
                .description("Original description")
                .startDate(LocalDate.now().toString())
                .endDate(LocalDate.now().plusDays(30).toString())
                .active(true)
                .build();

        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contractDto)))
                .andExpect(status().isCreated())
                .andReturn();

        ContractDto createdContract = objectMapper.readValue(
                result.getResponse().getContentAsString(), ContractDto.class);

        ContractDto updatedContractDto = ContractDto.builder()
                .tenant(TENANT_ID)
                .code("CON003")
                .title("Updated Contract")
                .description("Updated description")
                .startDate(LocalDate.now().toString())
                .endDate(LocalDate.now().plusDays(60).toString())
                .active(false)
                .build();

        mockMvc.perform(put(BASE_URL + "/" + createdContract.getId())
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedContractDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Contract"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @Order(4)
    void testUpdateContractWithFile() throws Exception {
        ContractDto contractDto = ContractDto.builder()
                .tenant(TENANT_ID)
                .code("CON004")
                .title("Contract for Update")
                .description("Contract for file update")
                .startDate(LocalDate.now().toString())
                .endDate(LocalDate.now().plusDays(30).toString())
                .active(true)
                .build();

        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contractDto)))
                .andExpect(status().isCreated())
                .andReturn();

        ContractDto createdContract = objectMapper.readValue(
                result.getResponse().getContentAsString(), ContractDto.class);

        ContractDto updatedContractDto = ContractDto.builder()
                .tenant(TENANT_ID)
                .code("CON004")
                .title("Updated Contract with File")
                .description("Updated with file")
                .startDate(LocalDate.now().toString())
                .endDate(LocalDate.now().plusDays(60).toString())
                .active(true)
                .build();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "updated.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Updated file content".getBytes(StandardCharsets.UTF_8));

        MockMultipartFile dtoPart = new MockMultipartFile(
                "dto",
                "contract.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(updatedContractDto).getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart(FILE_URL + "/" + createdContract.getId())
                        .file(file)
                        .file(dtoPart)
                        .header(TENANT_HEADER, TENANT_ID)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Contract with File"))
                .andExpect(jsonPath("$.originalFileName").value("updated.pdf"));
    }

    @Test
    @Order(5)
    void testFindContractById() throws Exception {
        ContractDto contractDto = ContractDto.builder()
                .tenant(TENANT_ID)
                .code("CON005")
                .title("Contract to Find")
                .description("Contract for find by ID")
                .startDate(LocalDate.now().toString())
                .endDate(LocalDate.now().plusDays(30).toString())
                .active(true)
                .build();

        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contractDto)))
                .andExpect(status().isCreated())
                .andReturn();

        ContractDto createdContract = objectMapper.readValue(
                result.getResponse().getContentAsString(), ContractDto.class);

        mockMvc.perform(get(BASE_URL + "/" + createdContract.getId())
                        .header(TENANT_HEADER, TENANT_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdContract.getId()))
                .andExpect(jsonPath("$.code").value("CON005"))
                .andExpect(jsonPath("$.title").value("Contract to Find"));
    }

    @Test
    @Order(6)
    void testFindAllContracts() throws Exception {
        ContractDto contract1 = ContractDto.builder()
                .tenant(TENANT_ID)
                .code("CON006")
                .title("Contract 1")
                .description("First contract")
                .startDate(LocalDate.now().toString())
                .endDate(LocalDate.now().plusDays(30).toString())
                .active(true)
                .build();

        ContractDto contract2 = ContractDto.builder()
                .tenant(TENANT_ID)
                .code("CON007")
                .title("Contract 2")
                .description("Second contract")
                .startDate(LocalDate.now().toString())
                .endDate(LocalDate.now().plusDays(30).toString())
                .active(true)
                .build();

        mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contract1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contract2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL)
                        .header(TENANT_HEADER, TENANT_ID)
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].code", containsInAnyOrder("CON006", "CON007")));
    }

    @Test
    @Order(7)
    void testDeleteContract() throws Exception {
        ContractDto contractDto = ContractDto.builder()
                .tenant(TENANT_ID)
                .code("CON008")
                .title("Contract to Delete")
                .description("Contract for deletion")
                .startDate(LocalDate.now().toString())
                .endDate(LocalDate.now().plusDays(30).toString())
                .active(true)
                .build();

        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contractDto)))
                .andExpect(status().isCreated())
                .andReturn();

        ContractDto createdContract = objectMapper.readValue(
                result.getResponse().getContentAsString(), ContractDto.class);

        mockMvc.perform(delete(BASE_URL + "/" + createdContract.getId())
                        .header(TENANT_HEADER, TENANT_ID))
                .andDo(print())
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL + "/" + createdContract.getId())
                        .header(TENANT_HEADER, TENANT_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(8)
    void testUploadFileToExistingContract() throws Exception {
        ContractDto contractDto = ContractDto.builder()
                .tenant(TENANT_ID)
                .code("CON009")
                .title("Contract for File Upload")
                .description("Contract for file upload test")
                .startDate(LocalDate.now().toString())
                .endDate(LocalDate.now().plusDays(30).toString())
                .active(true)
                .build();

        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contractDto)))
                .andExpect(status().isCreated())
                .andReturn();

        ContractDto createdContract = objectMapper.readValue(
                result.getResponse().getContentAsString(), ContractDto.class);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "contract.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Contract file content".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart(FILE_URL + "/upload/" + createdContract.getId())
                        .file(file)
                        .header(TENANT_HEADER, TENANT_ID)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalFileName").value("contract.pdf"));
    }

    @Test
    @Order(9)
    void testDownloadFile() throws Exception {
        ContractDto contractDto = ContractDto.builder()
                .tenant(TENANT_ID)
                .code("CON010")
                .title("Contract for File Download")
                .description(" договор для теста скачивания файла")
                .startDate(LocalDate.now().toString())
                .endDate(LocalDate.now().plusDays(30).toString())
                .active(true)
                .build();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "download.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Downloadable file content".getBytes(StandardCharsets.UTF_8));

        MockMultipartFile dtoPart = new MockMultipartFile(
                "dto",
                "contract.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(contractDto).getBytes(StandardCharsets.UTF_8));

        MvcResult result = mockMvc.perform(multipart(FILE_URL)
                        .file(file)
                        .file(dtoPart)
                        .header(TENANT_HEADER, TENANT_ID))
                .andExpect(status().isCreated())
                .andReturn();

        ContractDto createdContract = objectMapper.readValue(
                result.getResponse().getContentAsString(), ContractDto.class);

        long uploadedFileSize = file.getSize();

        MvcResult downloadResult = mockMvc.perform(get(FILE_URL + "/download/" + createdContract.getId())
                        .header(TENANT_HEADER, TENANT_ID)
                        .param("version", "0"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment; filename=\"download.pdf\"")))
                .andExpect(content().contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andReturn();

        byte[] downloadedContent = downloadResult.getResponse().getContentAsByteArray();
        assertThat("Downloaded file size should match uploaded file size", Long.valueOf(downloadedContent.length), is(uploadedFileSize));
    }
}