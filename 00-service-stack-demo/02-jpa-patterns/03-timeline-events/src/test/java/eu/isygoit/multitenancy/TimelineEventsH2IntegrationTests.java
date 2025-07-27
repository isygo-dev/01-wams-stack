package eu.isygoit.multitenancy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.multitenancy.dto.TutorialDto;
import eu.isygoit.multitenancy.model.TimeLineEvent;
import eu.isygoit.multitenancy.utils.ITenantService;
import eu.isygoit.timeline.repository.TimelineEventRepository;
import eu.isygoit.timeline.schema.EventType;
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
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The type Timeline events h 2 integration tests.
 */
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
    private static final String BASE_URL = "/api/tutorials";

    private static Long tutorialId;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TimelineEventRepository timelineEventRepository;

    @Value("${multitenancy.mode}")
    private String multiTenancyProperty;

    /**
     * Init shared schema.
     *
     * @param tenantService the tenant service
     */
    @BeforeAll
    static void initSharedSchema(@Autowired ITenantService tenantService) {
        tenantService.initializeTenantSchema("public");
    }

    /**
     * Clean up.
     */
    @AfterAll
    static void cleanUp() {
        // Any final cleanup if needed
    }

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        tutorialId = null; // Reset tutorialId for each test
    }

    private List<TimeLineEvent> waitForEvents(String elementType, String elementId, String tenant, int expectedCount, long timeoutMs) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        List<TimeLineEvent> events;
        do {
            events = timelineEventRepository.findByElementTypeAndElementIdAndTenant(elementType, elementId, tenant);
            if (events.size() >= expectedCount) {
                return events;
            }
            TimeUnit.MILLISECONDS.sleep(100);
        } while (System.currentTimeMillis() - startTime < timeoutMs);
        return events;
    }

    /**
     * Should validate discriminator mode.
     */
    @Test
    @Order(0)
    void shouldValidateDiscriminatorMode() {
        Assertions.assertEquals("GDM", multiTenancyProperty);
    }

    /**
     * Test create tutorial should record created event with full attributes.
     *
     * @throws Exception the exception
     */
    @Test
    @Order(1)
    @DisplayName("Should create tutorial and record CREATED timeline event with full entity attributes")
    void testCreateTutorial_ShouldRecordCreatedEventWithFullAttributes() throws Exception {
        // Given
        TutorialDto tutorial = TutorialDto.builder()
                .tenant(TENANT_1)
                .title("Spring Boot Tutorial")
                .description("Learn Spring Boot basics")
                .published(false)
                .build();

        // When
        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tutorial)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Spring Boot Tutorial"))
                .andExpect(jsonPath("$.description").value("Learn Spring Boot basics"))
                .andExpect(jsonPath("$.published").value(false))
                .andReturn();

        // Extract created tutorial ID
        TutorialDto createdTutorial = objectMapper.readValue(result.getResponse().getContentAsString(), TutorialDto.class);
        tutorialId = createdTutorial.getId();

        // Wait for async timeline event
        List<TimeLineEvent> events = waitForEvents("Tutorial", tutorialId.toString(), TENANT_1, 1, 2000);

        // Then
        assertEquals(1, events.size(), "Should have exactly one CREATED event");
        TimeLineEvent event = events.get(0);

        assertEquals(EventType.CREATED, event.getEventType(), "Event type should be CREATED");
        assertEquals("Tutorial", event.getElementType(), "Element type should be Tutorial");
        assertEquals(tutorialId.toString(), event.getElementId(), "Element ID should match");
        assertNotNull(event.getTimestamp(), "Timestamp should not be null");
        assertNotNull(event.getAttributes(), "Attributes should not be null");

        // Verify attributes structure
        JsonNode attributes = objectMapper.readTree(event.getAttributes().asText());
        JsonNode dataNode = attributes.path("data");

        assertFalse(dataNode.isMissingNode(), "Attributes should have 'data' field");
        assertEquals(tutorialId.longValue(), dataNode.path("id").asLong(), "ID should match");
        assertEquals("Spring Boot Tutorial", dataNode.path("title").asText(), "Title should match");
        assertEquals("Learn Spring Boot basics", dataNode.path("description").asText(), "Description should match");
        assertEquals(false, dataNode.path("published").asBoolean(), "Published should match");
        assertEquals(TENANT_1, dataNode.path("tenant").asText(), "Tenant should match");

        // Verify audit fields
        assertTrue(dataNode.has("createDate"), "createDate should be present");
        assertTrue(dataNode.has("createdBy"), "createdBy should be present");
        assertTrue(dataNode.has("updateDate"), "updateDate should be present");
        assertTrue(dataNode.has("updatedBy"), "updatedBy should be present");

        System.out.println("CREATED event attributes: " + attributes.toPrettyString());
    }

    /**
     * Test update tutorial should record updated event with diff attributes.
     *
     * @throws Exception the exception
     */
    @Test
    @Order(2)
    @DisplayName("Should update tutorial and record UPDATED timeline event with diff attributes")
    void testUpdateTutorial_ShouldRecordUpdatedEventWithDiffAttributes() throws Exception {
        // Given - Create a tutorial first
        testCreateTutorial_ShouldRecordCreatedEventWithFullAttributes();

        TutorialDto updatedTutorial = TutorialDto.builder()
                .id(tutorialId)
                .tenant(TENANT_1)
                .title("Spring Boot Tutorial v1.0")
                .description("Learn Spring Boot basics") // Unchanged
                .published(true)
                .build();

        // When
        mockMvc.perform(put(BASE_URL + "/" + tutorialId)
                        .header(TENANT_HEADER, TENANT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTutorial)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Spring Boot Tutorial v1.0"))
                .andExpect(jsonPath("$.description").value("Learn Spring Boot basics"))
                .andExpect(jsonPath("$.published").value(true));

        // Wait for async timeline event
        List<TimeLineEvent> events = waitForEvents("Tutorial", tutorialId.toString(), TENANT_1, 2, 2000);

        // Then
        assertEquals(2, events.size(), "Should have CREATED and UPDATED events");
        TimeLineEvent updateEvent = events.stream()
                .filter(e -> e.getEventType() == EventType.UPDATED)
                .findFirst()
                .orElseThrow(() -> new AssertionError("UPDATE event not found"));

        assertEquals(EventType.UPDATED, updateEvent.getEventType(), "Event type should be UPDATED");
        assertEquals("Tutorial", updateEvent.getElementType(), "Element type should be Tutorial");
        assertEquals(tutorialId.toString(), updateEvent.getElementId(), "Element ID should match");
        assertNotNull(updateEvent.getTimestamp(), "Timestamp should not be null");

        // Verify attributes structure
        JsonNode attributes = objectMapper.readTree(updateEvent.getAttributes().asText());
        JsonNode dataNode = attributes.path("data");

        assertFalse(dataNode.isMissingNode(), "Attributes should have 'data' field");
        assertEquals("Spring Boot Tutorial v1.0", dataNode.path("title").asText(), "Title should be updated");
        assertEquals(true, dataNode.path("published").asBoolean(), "Published should be updated");
        assertFalse(dataNode.has("description"), "Unchanged description should not be in diff");
        assertFalse(dataNode.has("id"), "Unchanged ID should not be in diff");
        assertFalse(dataNode.has("tenant"), "Unchanged tenant should not be in diff");
        assertFalse(dataNode.has("createDate"), "Audit fields should not be in diff");
        assertFalse(dataNode.has("createdBy"), "Audit fields should not be in diff");
        assertFalse(dataNode.has("updateDate"), "Audit fields should not be in diff");
        assertFalse(dataNode.has("updatedBy"), "Audit fields should not be in diff");

        System.out.println("UPDATED event attributes: " + attributes.toPrettyString());
    }

    /**
     * Test delete tutorial should record deleted event with empty attributes.
     *
     * @throws Exception the exception
     */
    @Test
    @Order(3)
    @DisplayName("Should delete tutorial and record DELETED timeline event with empty attributes")
    void testDeleteTutorial_ShouldRecordDeletedEventWithEmptyAttributes() throws Exception {
        // Given - Create a tutorial first
        testCreateTutorial_ShouldRecordCreatedEventWithFullAttributes();

        // When
        mockMvc.perform(delete(BASE_URL + "/" + tutorialId)
                        .header(TENANT_HEADER, TENANT_1))
                .andExpect(status().isNoContent());

        // Wait for async timeline event
        List<TimeLineEvent> events = waitForEvents("Tutorial", tutorialId.toString(), TENANT_1, 2, 2000);

        // Then
        assertEquals(2, events.size(), "Should have CREATED and DELETED events");
        TimeLineEvent deleteEvent = events.stream()
                .filter(e -> e.getEventType() == EventType.DELETED)
                .findFirst()
                .orElseThrow(() -> new AssertionError("DELETE event not found"));

        assertEquals(EventType.DELETED, deleteEvent.getEventType(), "Event type should be DELETED");
        assertEquals("Tutorial", deleteEvent.getElementType(), "Element type should be Tutorial");
        assertEquals(tutorialId.toString(), deleteEvent.getElementId(), "Element ID should match");
        assertNotNull(deleteEvent.getTimestamp(), "Timestamp should not be null");

        // Verify attributes structure
        JsonNode attributes = objectMapper.readTree(deleteEvent.getAttributes().asText());
        JsonNode dataNode = attributes.path("data");

        assertFalse(dataNode.isMissingNode(), "Attributes should have 'data' field");
        assertTrue(dataNode.isObject(), "Data node should be an object");
        assertEquals(0, dataNode.size(), "Data node should be empty for DELETE event");

        System.out.println("DELETED event attributes: " + attributes.toPrettyString());
    }

    /**
     * Test multiple updates should record diff attributes.
     *
     * @throws Exception the exception
     */
    @Test
    @Order(4)
    @DisplayName("Should handle multiple updates and verify diff attributes")
    void testMultipleUpdates_ShouldRecordDiffAttributes() throws Exception {
        // Given - Create a tutorial
        TutorialDto tutorial = TutorialDto.builder()
                .tenant(TENANT_1)
                .title("Multi-Update Test")
                .description("Initial description")
                .published(false)
                .build();

        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tutorial)))
                .andExpect(status().isCreated())
                .andReturn();

        TutorialDto createdTutorial = objectMapper.readValue(createResult.getResponse().getContentAsString(), TutorialDto.class);
        tutorialId = createdTutorial.getId();

        // First update: Change title
        TutorialDto firstUpdate = TutorialDto.builder()
                .id(tutorialId)
                .tenant(TENANT_1)
                .title("Updated Title")
                .description("Initial description")
                .published(false)
                .build();

        mockMvc.perform(put(BASE_URL + "/" + tutorialId)
                        .header(TENANT_HEADER, TENANT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstUpdate)))
                .andExpect(status().isOk());

        // Second update: Change description and published
        TutorialDto secondUpdate = TutorialDto.builder()
                .id(tutorialId)
                .tenant(TENANT_1)
                .title("Updated Title")
                .description("Updated description")
                .published(true)
                .build();

        mockMvc.perform(put(BASE_URL + "/" + tutorialId)
                        .header(TENANT_HEADER, TENANT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondUpdate)))
                .andExpect(status().isOk());

        // Wait for async timeline events
        List<TimeLineEvent> events = waitForEvents("Tutorial", tutorialId.toString(), TENANT_1, 3, 2000);

        // Then
        assertEquals(3, events.size(), "Should have CREATED and two UPDATED events");
        events.sort((e1, e2) -> e1.getTimestamp().compareTo(e2.getTimestamp()));

        // Verify first UPDATE event
        TimeLineEvent firstUpdateEvent = events.get(1);
        assertEquals(EventType.UPDATED, firstUpdateEvent.getEventType(), "First event should be UPDATED");
        JsonNode firstUpdateAttributes = objectMapper.readTree(firstUpdateEvent.getAttributes().asText());
        JsonNode firstDataNode = firstUpdateAttributes.path("data");

        assertFalse(firstDataNode.isMissingNode(), "First UPDATE should have 'data' field");
        assertEquals("Updated Title", firstDataNode.path("title").asText(), "Title should be updated");
        assertFalse(firstDataNode.has("description"), "Unchanged description should not be in diff");
        assertFalse(firstDataNode.has("published"), "Unchanged published should not be in diff");
        assertFalse(firstDataNode.has("id"), "Unchanged ID should not be in diff");
        assertFalse(firstDataNode.has("tenant"), "Unchanged tenant should not be in diff");

        // Verify second UPDATE event
        TimeLineEvent secondUpdateEvent = events.get(2);
        assertEquals(EventType.UPDATED, secondUpdateEvent.getEventType(), "Second event should be UPDATED");
        JsonNode secondUpdateAttributes = objectMapper.readTree(secondUpdateEvent.getAttributes().asText());
        JsonNode secondDataNode = secondUpdateAttributes.path("data");

        assertFalse(secondDataNode.isMissingNode(), "Second UPDATE should have 'data' field");
        assertEquals("Updated description", secondDataNode.path("description").asText(), "Description should be updated");
        assertEquals(true, secondDataNode.path("published").asBoolean(), "Published should be updated");
        assertFalse(secondDataNode.has("title"), "Unchanged title should not be in diff");
        assertFalse(secondDataNode.has("id"), "Unchanged ID should not be in diff");
        assertFalse(secondDataNode.has("tenant"), "Unchanged tenant should not be in diff");

        System.out.println("First UPDATE event attributes: " + firstUpdateAttributes.toPrettyString());
        System.out.println("Second UPDATE event attributes: " + secondUpdateAttributes.toPrettyString());
    }

    /**
     * Test complete lifecycle should record all events.
     *
     * @throws Exception the exception
     */
    @Test
    @Order(5)
    @DisplayName("Should handle complete lifecycle and verify chronological order")
    void testCompleteLifecycle_ShouldRecordAllEvents() throws Exception {
        // Given
        TutorialDto tutorial = TutorialDto.builder()
                .tenant(TENANT_1)
                .title("Lifecycle Test")
                .description("Testing CRUD lifecycle")
                .published(false)
                .build();

        // When - Complete lifecycle: CREATE -> UPDATE -> DELETE
        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tutorial)))
                .andExpect(status().isCreated())
                .andReturn();

        TutorialDto createdTutorial = objectMapper.readValue(createResult.getResponse().getContentAsString(), TutorialDto.class);
        tutorialId = createdTutorial.getId();

        TutorialDto updatedTutorial = TutorialDto.builder()
                .id(tutorialId)
                .tenant(TENANT_1)
                .title("Lifecycle Test Updated")
                .description("Updated CRUD lifecycle")
                .published(true)
                .build();

        mockMvc.perform(put(BASE_URL + "/" + tutorialId)
                        .header(TENANT_HEADER, TENANT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTutorial)))
                .andExpect(status().isOk());

        mockMvc.perform(delete(BASE_URL + "/" + tutorialId)
                        .header(TENANT_HEADER, TENANT_1))
                .andExpect(status().isNoContent());

        // Wait for async timeline events
        List<TimeLineEvent> events = waitForEvents("Tutorial", tutorialId.toString(), TENANT_1, 3, 2000);

        // Then
        assertEquals(3, events.size(), "Should have CREATED, UPDATED, and DELETED events");
        events.sort((e1, e2) -> e1.getTimestamp().compareTo(e2.getTimestamp()));

        // Verify event types and order
        assertEquals(EventType.CREATED, events.get(0).getEventType(), "First event should be CREATED");
        assertEquals(EventType.UPDATED, events.get(1).getEventType(), "Second event should be UPDATED");
        assertEquals(EventType.DELETED, events.get(2).getEventType(), "Third event should be DELETED");

        // Verify timestamp ordering
        assertTrue(events.get(0).getTimestamp().isBefore(events.get(1).getTimestamp()), "CREATED should be before UPDATED");
        assertTrue(events.get(1).getTimestamp().isBefore(events.get(2).getTimestamp()), "UPDATED should be before DELETED");

        // Verify CREATED event attributes
        JsonNode createAttributes = objectMapper.readTree(events.get(0).getAttributes().asText());
        JsonNode createData = createAttributes.path("data");
        assertEquals("Lifecycle Test", createData.path("title").asText(), "CREATED title should match");
        assertEquals("Testing CRUD lifecycle", createData.path("description").asText(), "CREATED description should match");
        assertEquals(false, createData.path("published").asBoolean(), "CREATED published should match");
        assertEquals(TENANT_1, createData.path("tenant").asText(), "CREATED tenant should match");

        // Verify UPDATED event attributes
        JsonNode updateAttributes = objectMapper.readTree(events.get(1).getAttributes().asText());
        JsonNode updateData = updateAttributes.path("data");
        assertEquals("Lifecycle Test Updated", updateData.path("title").asText(), "UPDATED title should match");
        assertEquals("Updated CRUD lifecycle", updateData.path("description").asText(), "UPDATED description should match");
        assertEquals(true, updateData.path("published").asBoolean(), "UPDATED published should match");
        assertFalse(updateData.has("id"), "Unchanged ID should not be in diff");
        assertFalse(updateData.has("tenant"), "Unchanged tenant should not be in diff");

        // Verify DELETED event attributes
        JsonNode deleteAttributes = objectMapper.readTree(events.get(2).getAttributes().asText());
        JsonNode deleteData = deleteAttributes.path("data");
        assertEquals(0, deleteData.size(), "DELETED data node should be empty");

        System.out.println("CREATED event attributes: " + createAttributes.toPrettyString());
        System.out.println("UPDATED event attributes: " + updateAttributes.toPrettyString());
        System.out.println("DELETED event attributes: " + deleteAttributes.toPrettyString());
    }

    /**
     * Test update with no changes should not record event.
     *
     * @throws Exception the exception
     */
    @Test
    @Order(6)
    @DisplayName("Should handle update with no changes and not record UPDATE event")
    void testUpdateWithNoChanges_ShouldNotRecordEvent() throws Exception {
        // Given - Create a tutorial
        TutorialDto tutorial = TutorialDto.builder()
                .tenant(TENANT_1)
                .title("No Change Test")
                .description("Testing no changes")
                .published(false)
                .build();

        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tutorial)))
                .andExpect(status().isCreated())
                .andReturn();

        TutorialDto createdTutorial = objectMapper.readValue(createResult.getResponse().getContentAsString(), TutorialDto.class);
        tutorialId = createdTutorial.getId();

        // When - Perform update with no changes
        mockMvc.perform(put(BASE_URL + "/" + tutorialId)
                        .header(TENANT_HEADER, TENANT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tutorial)))
                .andExpect(status().isOk());

        // Wait briefly to ensure no new events are created
        List<TimeLineEvent> events = waitForEvents("Tutorial", tutorialId.toString(), TENANT_1, 1, 1000);

        // Then
        assertEquals(1, events.size(), "Should only have CREATED event");
        assertEquals(EventType.CREATED, events.get(0).getEventType(), "Only CREATED event should exist");
    }

    /**
     * Test create without tenant header should fail.
     *
     * @throws Exception the exception
     */
    @Test
    @Order(7)
    @DisplayName("Should fail to create tutorial without tenant header")
    void testCreateWithoutTenantHeader_ShouldFail() throws Exception {
        // Given
        TutorialDto tutorial = TutorialDto.builder()
                .title("No Tenant Test")
                .description("Testing without tenant")
                .published(false)
                .build();

        // When/Then
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tutorial)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tear down.
     */
    @AfterEach
    void tearDown() {
        // Clean up test data if needed
        if (tutorialId != null) {
            try {
                mockMvc.perform(delete(BASE_URL + "/" + tutorialId)
                        .header(TENANT_HEADER, TENANT_1));
            } catch (Exception ignored) {
                // Ignore cleanup failures
            }
        }
    }
}