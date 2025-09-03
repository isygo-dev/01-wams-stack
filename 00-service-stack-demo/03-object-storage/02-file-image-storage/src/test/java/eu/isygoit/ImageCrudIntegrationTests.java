package eu.isygoit;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.dto.UserDto;
import eu.isygoit.helper.JsonHelper;
import eu.isygoit.utils.ITenantService;
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
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create",
        "app.tenancy.enabled=true",
        "app.tenancy.mode=GDM"
})
@ActiveProfiles("postgres")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
class ImageCrudIntegrationTests {

    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final String TENANT_ID = "tenants";
    private static final String BASE_URL = "/api/v1/user";
    private static final String IMAGE_URL = BASE_URL + "/image";
    private static final int MAX_FIELD_LENGTH = 255;

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

    @Value("${app.tenancy.mode}")
    private String multiTenancyProperty;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.datasource.url", () -> postgres.getJdbcUrl());
        String tenants = postgres.getJdbcUrl().replace("/postgres", "/tenants");
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
            List<UserDto> users = objectMapper.readValue(result.getResponse().getContentAsString(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, UserDto.class));

            for (UserDto user : users) {
                mockMvc.perform(delete(BASE_URL + "/{id}", user.getId())
                                .header(TENANT_HEADER, TENANT_ID))
                        .andDo(print())
                        .andExpect(status().isNoContent());
            }
        }
    }

    private MockMultipartFile createMockImage() {
        byte[] imageContent = "mock image content" .getBytes();
        return new MockMultipartFile("file", "test.png", MediaType.IMAGE_PNG_VALUE, imageContent);
    }

    private MockMultipartFile createLargeImage() {
        byte[] largeContent = new byte[1024 * 1024 * 15];
        return new MockMultipartFile("file", "large.png", MediaType.IMAGE_PNG_VALUE, largeContent);
    }

    private MockMultipartFile createInvalidImage() {
        byte[] invalidContent = "invalid content" .getBytes();
        return new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, invalidContent);
    }

    private UserDto createUser(String firstName, String lastName, boolean withImage) throws Exception {
        UserDto userDto = UserDto.builder()
                .tenant(TENANT_ID)
                .firstName(firstName)
                .lastName(lastName)
                .active(true)
                .imagePath("")
                .build();

        if (withImage) {
            MockMultipartFile userPart = new MockMultipartFile(
                    "dto", "", "application/json", JsonHelper.toJson(userDto).getBytes());
            MockMultipartFile imagePart = createMockImage();
            MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart(IMAGE_URL);
            builder.file(userPart).file(imagePart);

            MvcResult result = mockMvc.perform(builder
                            .header(TENANT_HEADER, TENANT_ID))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andReturn();
            return objectMapper.readValue(result.getResponse().getContentAsString(), UserDto.class);
        } else {
            MvcResult result = mockMvc.perform(post(BASE_URL)
                            .header(TENANT_HEADER, TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JsonHelper.toJson(userDto)))
                    .andExpect(status().isCreated())
                    .andReturn();
            return objectMapper.readValue(result.getResponse().getContentAsString(), UserDto.class);
        }
    }

    private MockMultipartHttpServletRequestBuilder buildMultipartRequest(String url, String method, MockMultipartFile... files) {
        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart(url);
        if ("PUT" .equalsIgnoreCase(method)) {
            builder.with(req -> {
                req.setMethod("PUT");
                return req;
            });
        }
        for (MockMultipartFile file : files) {
            builder.file(file);
        }
        return builder;
    }

    @Test
    @Order(1)
    @DisplayName("Create user without image - should succeed")
    void testCreateUserWithoutImage() throws Exception {
        UserDto userDto = UserDto.builder()
                .tenant(TENANT_ID)
                .firstName("John")
                .lastName("Doe")
                .active(true)
                .imagePath("")
                .build();

        mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonHelper.toJson(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.tenant").value(TENANT_ID))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.imagePath").isEmpty());
    }

    @Test
    @Order(2)
    @DisplayName("Create user with image - should succeed")
    void testCreateUserWithImage() throws Exception {
        UserDto userDto = UserDto.builder()
                .tenant(TENANT_ID)
                .firstName("Jane")
                .lastName("Smith")
                .active(true)
                .imagePath("")
                .build();

        MockMultipartFile userPart = new MockMultipartFile(
                "dto", "", "application/json", JsonHelper.toJson(userDto).getBytes());
        MockMultipartFile imagePart = createMockImage();

        mockMvc.perform(buildMultipartRequest(IMAGE_URL, "POST", userPart, imagePart)
                        .header(TENANT_HEADER, TENANT_ID))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.tenant").value(TENANT_ID))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.imagePath").isNotEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("Update user with image - should succeed")
    void testUpdateUserWithImage() throws Exception {
        UserDto createdUser = createUser("Alice", "Johnson", false);
        Long userId = createdUser.getId();

        UserDto updatedUserDto = UserDto.builder()
                .id(userId)
                .tenant(TENANT_ID)
                .firstName("Alice")
                .lastName("Brown")
                .active(false)
                .imagePath("")
                .build();

        MockMultipartFile userPart = new MockMultipartFile(
                "dto", "", "application/json", JsonHelper.toJson(updatedUserDto).getBytes());
        MockMultipartFile imagePart = createMockImage();

        mockMvc.perform(buildMultipartRequest(IMAGE_URL + "/" + userId, "PUT", userPart, imagePart)
                        .header(TENANT_HEADER, TENANT_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.tenant").value(TENANT_ID))
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.lastName").value("Brown"))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.imagePath").isNotEmpty());
    }

    @Test
    @Order(4)
    @DisplayName("Partial update user - should succeed")
    void testPartialUpdateUser() throws Exception {
        UserDto createdUser = createUser("Bob", "Wilson", false);

        UserDto updatedUserDto = UserDto.builder()
                .id(createdUser.getId())
                .tenant(TENANT_ID)
                .firstName(createdUser.getFirstName())
                .lastName("Smith")
                .active(true)
                .imagePath("")
                .build();

        mockMvc.perform(put(BASE_URL + "/{id}", createdUser.getId())
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonHelper.toJson(updatedUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdUser.getId()))
                .andExpect(jsonPath("$.tenant").value(TENANT_ID))
                .andExpect(jsonPath("$.firstName").value("Bob"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.imagePath").isEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("Upload image to existing user - should succeed")
    void testUploadImageToUser() throws Exception {
        UserDto createdUser = createUser("Carol", "Davis", false);
        Long userId = createdUser.getId();

        MockMultipartFile imagePart = createMockImage();

        mockMvc.perform(multipart(IMAGE_URL + "/upload/" + userId)
                        .file(imagePart)
                        .header(TENANT_HEADER, TENANT_ID)
                        .with(req -> {
                            req.setMethod("PUT");
                            return req;
                        }))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.tenant").value(TENANT_ID))
                .andExpect(jsonPath("$.imagePath").isNotEmpty());
    }

    @Test
    @Order(6)
    @DisplayName("Download image from user - should succeed")
    void testDownloadImageFromUser() throws Exception {
        UserDto createdUser = createUser("David", "Miller", true);
        Long userId = createdUser.getId();

        MvcResult result = mockMvc.perform(get(IMAGE_URL + "/download/" + userId)
                        .header(TENANT_HEADER, TENANT_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment; filename=")))
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andReturn();

        byte[] downloadedContent = result.getResponse().getContentAsByteArray();
        byte[] expectedContent = "mock image content" .getBytes();
        assertArrayEquals(expectedContent, downloadedContent, "Downloaded image content should match uploaded content");
    }

    @Test
    @Order(7)
    @DisplayName("Get user by ID - should succeed")
    void testGetUserById() throws Exception {
        UserDto createdUser = createUser("Eve", "Taylor", false);
        Long userId = createdUser.getId();

        mockMvc.perform(get(BASE_URL + "/{id}", userId)
                        .header(TENANT_HEADER, TENANT_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.tenant").value(TENANT_ID))
                .andExpect(jsonPath("$.firstName").value("Eve"))
                .andExpect(jsonPath("$.lastName").value("Taylor"));
    }

    @Test
    @Order(8)
    @DisplayName("List all users - should succeed")
    void testListAllUsers() throws Exception {
        for (int i = 0; i < 3; i++) {
            createUser("User" + i, "Test", false);
        }

        mockMvc.perform(get(BASE_URL)
                        .header(TENANT_HEADER, TENANT_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$[*].tenant", everyItem(is(TENANT_ID))));
    }

    @Test
    @Order(9)
    @DisplayName("Delete user - should succeed")
    void testDeleteUser() throws Exception {
        UserDto createdUser = createUser("Frank", "White", false);
        Long userId = createdUser.getId();

        mockMvc.perform(delete(BASE_URL + "/{id}", userId)
                        .header(TENANT_HEADER, TENANT_ID))
                .andDo(print())
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL + "/{id}", userId)
                        .header(TENANT_HEADER, TENANT_ID))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(11)
    @DisplayName("Upload empty image - should fail")
    void testUploadEmptyImage() throws Exception {
        UserDto createdUser = createUser("Henry", "Adams", false);
        Long userId = createdUser.getId();

        MockMultipartFile emptyImage = new MockMultipartFile("file", "empty.png", MediaType.IMAGE_PNG_VALUE, new byte[0]);

        mockMvc.perform(multipart(IMAGE_URL + "/upload/" + userId)
                        .file(emptyImage)
                        .header(TENANT_HEADER, TENANT_ID)
                        .with(req -> {
                            req.setMethod("PUT");
                            return req;
                        }))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(12)
    @DisplayName("Concurrent create with image - should handle correctly")
    void testConcurrentCreateWithImage() throws Exception {
        int threadCount = 3;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    UserDto userDto = UserDto.builder()
                            .tenant(TENANT_ID)
                            .firstName("Concurrent" + index)
                            .lastName("User")
                            .active(true)
                            .imagePath("")
                            .build();

                    MockMultipartFile userPart = new MockMultipartFile(
                            "dto", "", "application/json", JsonHelper.toJson(userDto).getBytes());
                    MockMultipartFile imagePart = createMockImage();

                    mockMvc.perform(buildMultipartRequest(IMAGE_URL, "POST", userPart, imagePart)
                                    .header(TENANT_HEADER, TENANT_ID))
                            .andDo(print())
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.id").exists())
                            .andExpect(jsonPath("$.imagePath").isNotEmpty());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        mockMvc.perform(get(BASE_URL)
                        .header(TENANT_HEADER, TENANT_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(threadCount))));
    }

    @Test
    @Order(13)
    @DisplayName("Create user with invalid DTO - should fail")
    void testCreateUserWithInvalidDto() throws Exception {
        UserDto userDto = UserDto.builder()
                .tenant(null)
                .firstName("")
                .lastName("")
                .active(false)
                .imagePath("")
                .build();

        mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonHelper.toJson(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(14)
    @DisplayName("Create user with invalid image format - should fail")
    void testCreateUserWithInvalidImageFormat() throws Exception {
        UserDto userDto = UserDto.builder()
                .tenant(TENANT_ID)
                .firstName("Invalid")
                .lastName("Image")
                .active(true)
                .imagePath("")
                .build();

        MockMultipartFile userPart = new MockMultipartFile(
                "dto", "", "application/json", JsonHelper.toJson(userDto).getBytes());
        MockMultipartFile invalidImage = createInvalidImage();

        mockMvc.perform(buildMultipartRequest(IMAGE_URL, "POST", userPart, invalidImage)
                        .header(TENANT_HEADER, TENANT_ID))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(15)
    @DisplayName("Create user without tenant header - should fail")
    void testCreateUserWithoutTenantHeader() throws Exception {
        UserDto userDto = UserDto.builder()
                .tenant(TENANT_ID)
                .firstName("No")
                .lastName("Header")
                .active(true)
                .imagePath("")
                .build();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonHelper.toJson(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(16)
    @DisplayName("List users with pagination - should succeed")
    void testListUsersWithPagination() throws Exception {
        for (int i = 0; i < 5; i++) {
            createUser("Paged" + i, "User", false);
        }

        mockMvc.perform(get(BASE_URL)
                        .header(TENANT_HEADER, TENANT_ID)
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].tenant", everyItem(is(TENANT_ID))));
    }

    @Test
    @Order(17)
    @DisplayName("Concurrent update with image - should handle correctly")
    void testConcurrentUpdateWithImage() throws Exception {
        UserDto createdUser = createUser("Concurrent", "Update", false);
        Long userId = createdUser.getId();

        int threadCount = 3;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    UserDto updatedUserDto = UserDto.builder()
                            .id(userId)
                            .tenant(TENANT_ID)
                            .firstName("Concurrent")
                            .lastName("Update" + index)
                            .active(false)
                            .imagePath("")
                            .build();

                    MockMultipartFile userPart = new MockMultipartFile(
                            "dto", "", "application/json", JsonHelper.toJson(updatedUserDto).getBytes());
                    MockMultipartFile imagePart = createMockImage();

                    mockMvc.perform(buildMultipartRequest(IMAGE_URL + "/" + userId, "PUT", userPart, imagePart)
                                    .header(TENANT_HEADER, TENANT_ID))
                            .andDo(print())
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        mockMvc.perform(get(BASE_URL + "/{id}", userId)
                        .header(TENANT_HEADER, TENANT_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagePath").isNotEmpty());
    }

    @Test
    @Order(18)
    @DisplayName("Batch create users - should succeed")
    void testBatchCreateUsers() throws Exception {
        List<UserDto> users = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            users.add(UserDto.builder()
                    .tenant(TENANT_ID)
                    .firstName("Batch" + i)
                    .lastName("User")
                    .active(true)
                    .imagePath("")
                    .build());
        }

        mockMvc.perform(post(BASE_URL + "/batch")
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonHelper.toJson(users)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].tenant", everyItem(is(TENANT_ID))));
    }

    @Test
    @Order(19)
    @DisplayName("Batch delete users - should succeed")
    void testBatchDeleteUsers() throws Exception {
        List<Long> userIds = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            UserDto createdUser = createUser("Delete" + i, "User", false);
            userIds.add(createdUser.getId());
        }

        mockMvc.perform(delete(BASE_URL + "/batch")
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonHelper.toJson(userIds)))
                .andExpect(status().isNoContent());

        for (Long userId : userIds) {
            mockMvc.perform(get(BASE_URL + "/{id}", userId)
                            .header(TENANT_HEADER, TENANT_ID))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    @Order(20)
    @DisplayName("Download image from non-existent user - should fail")
    void testDownloadImageFromNonExistentUser() throws Exception {
        mockMvc.perform(get(IMAGE_URL + "/download/999")
                        .header(TENANT_HEADER, TENANT_ID))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(21)
    @DisplayName("Update non-existent user - should fail")
    void testUpdateNonExistentUser() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(999L)
                .tenant(TENANT_ID)
                .firstName("Non")
                .lastName("Existent")
                .active(true)
                .imagePath("")
                .build();

        MockMultipartFile userPart = new MockMultipartFile(
                "dto", "", "application/json", JsonHelper.toJson(userDto).getBytes());
        MockMultipartFile imagePart = createMockImage();

        mockMvc.perform(buildMultipartRequest(IMAGE_URL + "/999", "PUT", userPart, imagePart)
                        .header(TENANT_HEADER, TENANT_ID))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(23)
    @DisplayName("Create user with invalid JSON DTO - should fail")
    void testCreateUserWithInvalidJsonDto() throws Exception {
        MockMultipartFile userPart = new MockMultipartFile(
                "dto", "", "application/json", "invalid json" .getBytes());
        MockMultipartFile imagePart = createMockImage();

        mockMvc.perform(buildMultipartRequest(IMAGE_URL, "POST", userPart, imagePart)
                        .header(TENANT_HEADER, TENANT_ID))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(24)
    @DisplayName("Concurrent image downloads - should handle correctly")
    void testConcurrentImageDownloads() throws Exception {
        UserDto createdUser = createUser("Concurrent", "Download", true);
        Long userId = createdUser.getId();

        int threadCount = 3;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    MvcResult result = mockMvc.perform(get(IMAGE_URL + "/download/" + userId)
                                    .header(TENANT_HEADER, TENANT_ID))
                            .andDo(print())
                            .andExpect(status().isOk())
                            .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                            .andReturn();

                    byte[] downloadedContent = result.getResponse().getContentAsByteArray();
                    byte[] expectedContent = "mock image content" .getBytes();
                    assertArrayEquals(expectedContent, downloadedContent, "Downloaded image content should match");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
    }

    @Test
    @Order(26)
    @DisplayName("Create user with oversized fields - should fail")
    void testCreateUserWithOversizedFields() throws Exception {
        String oversizedField = "A" .repeat(MAX_FIELD_LENGTH + 1);
        UserDto userDto = UserDto.builder()
                .tenant(TENANT_ID)
                .firstName(oversizedField)
                .lastName("Doe")
                .active(true)
                .imagePath("")
                .build();

        mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonHelper.toJson(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(28)
    @DisplayName("Multipart request without DTO - should fail")
    void testMultipartWithoutDto() throws Exception {
        MockMultipartFile imagePart = createMockImage();

        mockMvc.perform(buildMultipartRequest(IMAGE_URL, "POST", imagePart)
                        .header(TENANT_HEADER, TENANT_ID))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(29)
    @DisplayName("List large number of users with pagination - should succeed")
    void testListLargeNumberOfUsers() throws Exception {
        for (int i = 0; i < 50; i++) {
            createUser("Large" + i, "User", false);
        }

        mockMvc.perform(get(BASE_URL)
                        .header(TENANT_HEADER, TENANT_ID)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[*].tenant", everyItem(is(TENANT_ID))));
    }
}