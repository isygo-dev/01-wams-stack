package eu.isygoit.multitenancy;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.helper.JsonHelper;
import eu.isygoit.multitenancy.dto.ResumeDto;
import eu.isygoit.multitenancy.dto.ResumeLinkedFileDto;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create",
        "multitenancy.mode=GDM"
})
@ActiveProfiles("postgres")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
class MultiFileCrudIntegrationTests {

    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final String TENANT_ID = "tenants";
    private static final String BASE_URL = "/api/v1/resume";
    private static final String MULTI_FILES_URL = BASE_URL + "/multi-files";
    private static final String UPLOAD_URL = MULTI_FILES_URL + "/upload";
    private static final String UPLOAD_ONE_URL = MULTI_FILES_URL + "/upload/one";
    private static final String DOWNLOAD_URL = MULTI_FILES_URL + "/download";

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

    private Long testResumeId;

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
            List<ResumeDto> resumes = objectMapper.readValue(result.getResponse().getContentAsString(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ResumeDto.class));

            for (ResumeDto resume : resumes) {
                mockMvc.perform(delete(BASE_URL + "/{id}", resume.getId())
                                .header(TENANT_HEADER, TENANT_ID))
                        .andDo(print())
                        .andExpect(status().isNoContent());
            }
        }
    }

    @Test
    @Order(1)
    @DisplayName("Create Resume for Multi-File Testing")
    void createResumeForTesting() throws Exception {
        ResumeDto resumeDto = ResumeDto.builder()
                .title("Test Resume")
                .description("Test Description")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(30))
                .build();

        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonHelper.toJson(resumeDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Resume"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        ResumeDto createdResume = objectMapper.readValue(
                result.getResponse().getContentAsString(), ResumeDto.class);
        testResumeId = createdResume.getId();

        assertThat(testResumeId, is(notNullValue()));
    }

    @Test
    @Order(2)
    @DisplayName("Upload Multiple Files - Success")
    void uploadMultipleFiles_Success() throws Exception {
        // Create test resume first
        createResumeForTesting();

        // Create test files
        MockMultipartFile file1 = new MockMultipartFile(
                "files", "test1.txt", MediaType.TEXT_PLAIN_VALUE,
                "Test file content 1".getBytes(StandardCharsets.UTF_8));

        MockMultipartFile file2 = new MockMultipartFile(
                "files", "test2.pdf", MediaType.APPLICATION_PDF_VALUE,
                "Test PDF content 2".getBytes(StandardCharsets.UTF_8));

        MockMultipartFile file3 = new MockMultipartFile(
                "files", "test3.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "Test DOCX content 3".getBytes(StandardCharsets.UTF_8));

        MvcResult result = mockMvc.perform(multipart(UPLOAD_URL)
                        .file(file1)
                        .file(file2)
                        .file(file3)
                        .param("parentId", testResumeId.toString())
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].originalFileName").exists())
                .andExpect(jsonPath("$[0].size").exists())
                .andExpect(jsonPath("$[0].version").exists())
                .andExpect(jsonPath("$[0].code").exists())
                .andReturn();

        List<ResumeLinkedFileDto> uploadedFiles = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, ResumeLinkedFileDto.class));

        assertThat(uploadedFiles, hasSize(3));
        assertThat(uploadedFiles.get(0).getOriginalFileName(), is(oneOf("test1.txt", "test2.pdf", "test3.docx")));
        assertThat(uploadedFiles.get(0).getSize(), is(greaterThan(0L)));
        assertThat(uploadedFiles.get(0).getVersion(), is(greaterThanOrEqualTo(1L)));
    }

    @Test
    @Order(3)
    @DisplayName("Upload Single File - Success")
    void uploadSingleFile_Success() throws Exception {
        // Create test resume first
        createResumeForTesting();

        MockMultipartFile file = new MockMultipartFile(
                "file", "single-test.txt", MediaType.TEXT_PLAIN_VALUE,
                "Single test file content".getBytes(StandardCharsets.UTF_8));

        MvcResult result = mockMvc.perform(multipart(UPLOAD_ONE_URL)
                        .file(file)
                        .param("parentId", testResumeId.toString())
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].originalFileName").value("single-test.txt"))
                .andExpect(jsonPath("$[0].size").value(greaterThan(0)))
                .andExpect(jsonPath("$[0].version").exists())
                .andExpect(jsonPath("$[0].code").exists())
                .andReturn();

        List<ResumeLinkedFileDto> uploadedFiles = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, ResumeLinkedFileDto.class));

        assertThat(uploadedFiles, hasSize(1));
        assertThat(uploadedFiles.get(0).getOriginalFileName(), is("single-test.txt"));
        assertThat(uploadedFiles.get(0).getSize(), is(greaterThan(0L)));
    }

    @Test
    @Order(4)
    @DisplayName("Upload Files with Invalid Parent ID - Not Found")
    void uploadFiles_InvalidParentId_NotFound() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", MediaType.TEXT_PLAIN_VALUE,
                "Test content".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart(UPLOAD_ONE_URL)
                        .file(file)
                        .param("parentId", "99999")
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(5)
    @DisplayName("Upload Empty File - Bad Request")
    void uploadEmptyFile_BadRequest() throws Exception {
        // Create test resume first
        createResumeForTesting();

        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.txt", MediaType.TEXT_PLAIN_VALUE, new byte[0]);

        mockMvc.perform(multipart(UPLOAD_ONE_URL)
                        .file(emptyFile)
                        .param("parentId", testResumeId.toString())
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(6)
    @DisplayName("Download File - Success")
    void downloadFile_Success() throws Exception {
        // Create test resume and upload file first
        createResumeForTesting();

        MockMultipartFile file = new MockMultipartFile(
                "file", "download-test.txt", MediaType.TEXT_PLAIN_VALUE,
                "Download test content".getBytes(StandardCharsets.UTF_8));

        MvcResult uploadResult = mockMvc.perform(multipart(UPLOAD_ONE_URL)
                        .file(file)
                        .param("parentId", testResumeId.toString())
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andReturn();

        List<ResumeLinkedFileDto> uploadedFiles = objectMapper.readValue(
                uploadResult.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, ResumeLinkedFileDto.class));

        ResumeLinkedFileDto uploadedFile = uploadedFiles.get(0);

        // Now download the file
        MvcResult downloadResult = mockMvc.perform(get(DOWNLOAD_URL)
                        .param("parentId", testResumeId.toString())
                        .param("fileId", uploadedFile.getId().toString())
                        .param("version", uploadedFile.getVersion().toString())
                        .header(TENANT_HEADER, TENANT_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("attachment; filename=\"download-test.txt\"")))
                .andReturn();

        byte[] downloadedContent = downloadResult.getResponse().getContentAsByteArray();
        String downloadedText = new String(downloadedContent, StandardCharsets.UTF_8);

        assertThat(downloadedText, is("Download test content"));
    }

    @Test
    @Order(7)
    @DisplayName("Download File with Invalid File ID - Not Found")
    void downloadFile_InvalidFileId_NotFound() throws Exception {
        // Create test resume first
        createResumeForTesting();

        mockMvc.perform(get(DOWNLOAD_URL)
                        .param("parentId", testResumeId.toString())
                        .param("fileId", "99999")
                        .param("version", "1")
                        .header(TENANT_HEADER, TENANT_ID))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(8)
    @DisplayName("Download File with Invalid Parent ID - Not Found")
    void downloadFile_InvalidParentId_NotFound() throws Exception {
        mockMvc.perform(get(DOWNLOAD_URL)
                        .param("parentId", "99999")
                        .param("fileId", "1")
                        .param("version", "1")
                        .header(TENANT_HEADER, TENANT_ID))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(9)
    @DisplayName("Delete File - Success")
    void deleteFile_Success() throws Exception {
        // Create test resume and upload file first
        createResumeForTesting();

        MockMultipartFile file = new MockMultipartFile(
                "file", "delete-test.txt", MediaType.TEXT_PLAIN_VALUE,
                "Delete test content".getBytes(StandardCharsets.UTF_8));

        MvcResult uploadResult = mockMvc.perform(multipart(UPLOAD_ONE_URL)
                        .file(file)
                        .param("parentId", testResumeId.toString())
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andReturn();

        List<ResumeLinkedFileDto> uploadedFiles = objectMapper.readValue(
                uploadResult.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, ResumeLinkedFileDto.class));

        ResumeLinkedFileDto uploadedFile = uploadedFiles.get(0);

        // Delete the file
        mockMvc.perform(delete(MULTI_FILES_URL)
                        .param("parentId", testResumeId.toString())
                        .param("fileId", uploadedFile.getId().toString())
                        .header(TENANT_HEADER, TENANT_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // Verify file is deleted by trying to download it
        mockMvc.perform(get(DOWNLOAD_URL)
                        .param("parentId", testResumeId.toString())
                        .param("fileId", uploadedFile.getId().toString())
                        .param("version", uploadedFile.getVersion().toString())
                        .header(TENANT_HEADER, TENANT_ID))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(10)
    @DisplayName("Delete File with Invalid File ID - Not Found")
    void deleteFile_InvalidFileId_NotFound() throws Exception {
        // Create test resume first
        createResumeForTesting();

        mockMvc.perform(delete(MULTI_FILES_URL)
                        .param("parentId", testResumeId.toString())
                        .param("fileId", "99999")
                        .header(TENANT_HEADER, TENANT_ID))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(11)
    @DisplayName("Delete File with Invalid Parent ID - Not Found")
    void deleteFile_InvalidParentId_NotFound() throws Exception {
        mockMvc.perform(delete(MULTI_FILES_URL)
                        .param("parentId", "99999")
                        .param("fileId", "1")
                        .header(TENANT_HEADER, TENANT_ID))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(12)
    @DisplayName("Upload Multiple Files with Different Types")
    void uploadMultipleFilesWithDifferentTypes_Success() throws Exception {
        // Create test resume first
        createResumeForTesting();

        // Create files with different MIME types
        MockMultipartFile textFile = new MockMultipartFile(
                "files", "document.txt", MediaType.TEXT_PLAIN_VALUE,
                "Plain text content".getBytes(StandardCharsets.UTF_8));

        MockMultipartFile jsonFile = new MockMultipartFile(
                "files", "data.json", MediaType.APPLICATION_JSON_VALUE,
                "{\"key\": \"value\"}".getBytes(StandardCharsets.UTF_8));

        MockMultipartFile xmlFile = new MockMultipartFile(
                "files", "config.xml", MediaType.APPLICATION_XML_VALUE,
                "<?xml version=\"1.0\"?><root></root>".getBytes(StandardCharsets.UTF_8));

        MvcResult result = mockMvc.perform(multipart(UPLOAD_URL)
                        .file(textFile)
                        .file(jsonFile)
                        .file(xmlFile)
                        .param("parentId", testResumeId.toString())
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andReturn();

        List<ResumeLinkedFileDto> uploadedFiles = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, ResumeLinkedFileDto.class));

        assertThat(uploadedFiles, hasSize(3));

        // Verify all files have different names and proper metadata
        List<String> fileNames = uploadedFiles.stream()
                .map(ResumeLinkedFileDto::getOriginalFileName)
                .toList();

        assertThat(fileNames, containsInAnyOrder("document.txt", "data.json", "config.xml"));
    }

    @Test
    @Order(13)
    @DisplayName("Upload and Download Large File")
    void uploadAndDownloadLargeFile_Success() throws Exception {
        // Create test resume first
        createResumeForTesting();

        // Create a larger file (1KB)
        byte[] largeContent = new byte[1024];
        for (int i = 0; i < largeContent.length; i++) {
            largeContent[i] = (byte) (i % 256);
        }

        MockMultipartFile largeFile = new MockMultipartFile(
                "file", "large-file.bin", MediaType.APPLICATION_OCTET_STREAM_VALUE, largeContent);

        // Upload large file
        MvcResult uploadResult = mockMvc.perform(multipart(UPLOAD_ONE_URL)
                        .file(largeFile)
                        .param("parentId", testResumeId.toString())
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].size").value(1024))
                .andReturn();

        List<ResumeLinkedFileDto> uploadedFiles = objectMapper.readValue(
                uploadResult.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, ResumeLinkedFileDto.class));

        ResumeLinkedFileDto uploadedFile = uploadedFiles.get(0);

        // Download and verify content
        MvcResult downloadResult = mockMvc.perform(get(DOWNLOAD_URL)
                        .param("parentId", testResumeId.toString())
                        .param("fileId", uploadedFile.getId().toString())
                        .param("version", uploadedFile.getVersion().toString())
                        .header(TENANT_HEADER, TENANT_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        byte[] downloadedContent = downloadResult.getResponse().getContentAsByteArray();

        assertThat(downloadedContent.length, is(1024));
        assertThat(downloadedContent, is(largeContent));
    }

    @Test
    @Order(14)
    @DisplayName("Test File Operations Without Tenant Header - Bad Request")
    void testFileOperationsWithoutTenantHeader_BadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", MediaType.TEXT_PLAIN_VALUE,
                "Test content".getBytes(StandardCharsets.UTF_8));

        // Upload without tenant header
        mockMvc.perform(multipart(UPLOAD_ONE_URL)
                        .file(file)
                        .param("parentId", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andDo(print())
                .andExpect(status().isBadRequest());

        // Download without tenant header
        mockMvc.perform(get(DOWNLOAD_URL)
                        .param("parentId", "1")
                        .param("fileId", "1")
                        .param("version", "1"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        // Delete without tenant header
        mockMvc.perform(delete(MULTI_FILES_URL)
                        .param("parentId", "1")
                        .param("fileId", "1"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}