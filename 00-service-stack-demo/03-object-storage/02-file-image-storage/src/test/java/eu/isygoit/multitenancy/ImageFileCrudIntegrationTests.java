package eu.isygoit.multitenancy;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.multitenancy.dto.ResumeDto;
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
        "spring.jpa.hibernate.ddl-auto=update",
        "multitenancy.mode=GDM",
        "spring.jpa.hibernate.ddl-auto=create"
})
@ActiveProfiles("postgres")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
class ImageFileCrudIntegrationTests {

    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final String TENANT_ID = "tenants";
    private static final String BASE_URL = "/api/v1/resume";
    private static final String FILE_URL = BASE_URL + "/file";
    private static final String IMAGE_URL = BASE_URL + "/image";

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
    void testCreateWithFile() throws Exception {
        ResumeDto resumeDto = ResumeDto.builder()
                .tenant(TENANT_ID)
                .code("RES001")
                .title("Test Resume")
                .description("Test Description")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(90))
                .active(true)
                .build();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Sample PDF content".getBytes(StandardCharsets.UTF_8));

        MockMultipartFile dtoPart = new MockMultipartFile(
                "dto",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(resumeDto).getBytes(StandardCharsets.UTF_8));

        MvcResult result = mockMvc.perform(multipart(FILE_URL)
                        .file(file)
                        .file(dtoPart)
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        ResumeDto createdResume = objectMapper.readValue(
                result.getResponse().getContentAsString(), ResumeDto.class);

        assertThat(createdResume.getId(), notNullValue());
        assertThat(createdResume.getOriginalFileName(), is("resume.pdf"));
        assertThat(createdResume.getTenant(), is(TENANT_ID));
        assertThat(createdResume.getCode(), is("RES001"));
    }

    @Test
    @Order(2)
    void testCreateWithImage() throws Exception {
        ResumeDto resumeDto = ResumeDto.builder()
                .tenant(TENANT_ID)
                .code("RES002")
                .title("Test Resume with Image")
                .description("Test Description with Image")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(30))
                .active(true)
                .build();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "Sample Image content".getBytes(StandardCharsets.UTF_8));

        MockMultipartFile dtoPart = new MockMultipartFile(
                "dto",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(resumeDto).getBytes(StandardCharsets.UTF_8));

        MvcResult result = mockMvc.perform(multipart(IMAGE_URL)
                        .file(file)
                        .file(dtoPart)
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        ResumeDto createdResume = objectMapper.readValue(
                result.getResponse().getContentAsString(), ResumeDto.class);

        assertThat(createdResume.getId(), notNullValue());
        assertThat(createdResume.getImagePath(), containsString("profile.jpg"));
        assertThat(createdResume.getTenant(), is(TENANT_ID));
        assertThat(createdResume.getCode(), is("RES002"));
    }

    @Test
    @Order(3)
    void testUpdateWithFile() throws Exception {
        // First, create a resume
        ResumeDto resumeDto = ResumeDto.builder()
                .tenant(TENANT_ID)
                .code("RES003")
                .title("Initial Resume")
                .description("Initial Description")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(90))
                .active(true)
                .build();

        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resumeDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        ResumeDto createdResume = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), ResumeDto.class);

        // Update with file
        ResumeDto updatedDto = ResumeDto.builder()
                .id(createdResume.getId())
                .tenant(TENANT_ID)
                .code("RES003")
                .title("Updated Resume")
                .description("Updated Description")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(90))
                .active(true)
                .build();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "updated_resume.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Updated PDF content".getBytes(StandardCharsets.UTF_8));

        MockMultipartFile dtoPart = new MockMultipartFile(
                "dto",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(updatedDto).getBytes(StandardCharsets.UTF_8));

        MvcResult result = mockMvc.perform(multipart(FILE_URL + "/{id}", createdResume.getId())
                        .file(file)
                        .file(dtoPart)
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ResumeDto updatedResume = objectMapper.readValue(
                result.getResponse().getContentAsString(), ResumeDto.class);

        assertThat(updatedResume.getId(), is(createdResume.getId()));
        assertThat(updatedResume.getTitle(), is("Updated Resume"));
        assertThat(updatedResume.getOriginalFileName(), is("updated_resume.pdf"));
    }

    @Test
    @Order(4)
    void testUpdateWithImage() throws Exception {
        // First, create a resume
        ResumeDto resumeDto = ResumeDto.builder()
                .tenant(TENANT_ID)
                .code("RES004")
                .title("Initial Resume")
                .description("Initial Description")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(90))
                .active(true)
                .build();

        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resumeDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        ResumeDto createdResume = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), ResumeDto.class);

        // Update with image
        ResumeDto updatedDto = ResumeDto.builder()
                .id(createdResume.getId())
                .tenant(TENANT_ID)
                .code("RES004")
                .title("Updated Resume with Image")
                .description("Updated Description")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(90))
                .active(true)
                .build();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "updated_profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "Updated Image content".getBytes(StandardCharsets.UTF_8));

        MockMultipartFile dtoPart = new MockMultipartFile(
                "dto",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(updatedDto).getBytes(StandardCharsets.UTF_8));

        MvcResult result = mockMvc.perform(multipart(IMAGE_URL + "/{id}", createdResume.getId())
                        .file(file)
                        .file(dtoPart)
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ResumeDto updatedResume = objectMapper.readValue(
                result.getResponse().getContentAsString(), ResumeDto.class);

        assertThat(updatedResume.getId(), is(createdResume.getId()));
        assertThat(updatedResume.getTitle(), is("Updated Resume with Image"));
        assertThat(updatedResume.getImagePath(), containsString("updated_profile.jpg"));
    }

    @Test
    @Order(5)
    void testUploadFile() throws Exception {
        // First, create a resume
        ResumeDto resumeDto = ResumeDto.builder()
                .tenant(TENANT_ID)
                .code("RES005")
                .title("Resume for File Upload")
                .description("Description")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(90))
                .active(true)
                .build();

        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resumeDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        ResumeDto createdResume = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), ResumeDto.class);

        // Upload file
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "uploaded_resume.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Uploaded PDF content".getBytes(StandardCharsets.UTF_8));

        MvcResult result = mockMvc.perform(multipart(FILE_URL + "/upload/{id}", createdResume.getId())
                        .file(file)
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ResumeDto updatedResume = objectMapper.readValue(
                result.getResponse().getContentAsString(), ResumeDto.class);

        assertThat(updatedResume.getId(), is(createdResume.getId()));
        assertThat(updatedResume.getOriginalFileName(), is("uploaded_resume.pdf"));
    }

    @Test
    @Order(6)
    void testUploadImage() throws Exception {
        // First, create a resume
        ResumeDto resumeDto = ResumeDto.builder()
                .tenant(TENANT_ID)
                .code("RES006")
                .title("Resume for Image Upload")
                .description("Description")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(90))
                .active(true)
                .build();

        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resumeDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        ResumeDto createdResume = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), ResumeDto.class);

        // Upload image
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "uploaded_profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "Uploaded Image content".getBytes(StandardCharsets.UTF_8));

        MvcResult result = mockMvc.perform(multipart(IMAGE_URL + "/upload/{id}", createdResume.getId())
                        .file(file)
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ResumeDto updatedResume = objectMapper.readValue(
                result.getResponse().getContentAsString(), ResumeDto.class);

        assertThat(updatedResume.getId(), is(createdResume.getId()));
        assertThat(updatedResume.getImagePath(), containsString("uploaded_profile.jpg"));
    }

    @Test
    @Order(7)
    void testDownloadFile() throws Exception {
        // First, create a resume with a file
        ResumeDto resumeDto = ResumeDto.builder()
                .tenant(TENANT_ID)
                .code("RES007")
                .title("Resume for File Download")
                .description("Description")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(90))
                .active(true)
                .build();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "download_resume.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Downloadable PDF content".getBytes(StandardCharsets.UTF_8));

        MockMultipartFile dtoPart = new MockMultipartFile(
                "dto",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(resumeDto).getBytes(StandardCharsets.UTF_8));

        MvcResult createResult = mockMvc.perform(multipart(FILE_URL)
                        .file(file)
                        .file(dtoPart)
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        ResumeDto createdResume = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), ResumeDto.class);

        // Download file
        mockMvc.perform(get(FILE_URL + "/download/{id}", createdResume.getId())
                        .header(TENANT_HEADER, TENANT_ID)
                        .param("version", "0"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment; filename=\"download_resume.pdf\"")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF));
    }

    @Test
    @Order(8)
    void testDownloadImage() throws Exception {
        // First, create a resume with an image
        ResumeDto resumeDto = ResumeDto.builder()
                .tenant(TENANT_ID)
                .code("RES008")
                .title("Resume for Image Download")
                .description("Description")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(90))
                .active(true)
                .build();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "download_profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "Downloadable Image content".getBytes(StandardCharsets.UTF_8));

        MockMultipartFile dtoPart = new MockMultipartFile(
                "dto",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(resumeDto).getBytes(StandardCharsets.UTF_8));

        MvcResult createResult = mockMvc.perform(multipart(IMAGE_URL)
                        .file(file)
                        .file(dtoPart)
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        ResumeDto createdResume = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), ResumeDto.class);

        // Download image
        mockMvc.perform(get(IMAGE_URL + "/download/{id}", createdResume.getId())
                        .header(TENANT_HEADER, TENANT_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment; filename=\"download_profile.jpg_RES008.jpg\"")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_JPEG));
    }
}