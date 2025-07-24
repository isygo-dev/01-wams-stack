package eu.isygoit.multitenancy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.multitenancy.dto.TutorialDto;
import eu.isygoit.multitenancy.model.EventType;
import eu.isygoit.multitenancy.model.TimeLineEvent;
import eu.isygoit.multitenancy.repository.TimelineEventRepository;
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
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    private static final String BASE_URL = "/api/tutorials";
    private static final String TIMELINE_URL = "/api/timeline";

    private static Long tutorialId;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TimelineEventRepository timelineEventRepository;

    @Value("${multitenancy.mode}")
    private String multiTenancyProperty;

    @BeforeAll
    static void initSharedSchema(@Autowired ITenantService tenantService) {
        tenantService.initializeTenantSchema("public");
    }

    @BeforeEach
    void setUp() {
        timelineEventRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("Should create tutorial and record CREATED timeline event with full entity attributes")
    void testCreateTutorial_ShouldRecordCreatedEventWithAttributes() throws Exception {
        // Given
        TutorialDto tutorial = TutorialDto.builder()
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
        String responseContent = result.getResponse().getContentAsString();
        TutorialDto createdTutorial = objectMapper.readValue(responseContent, TutorialDto.class);
        tutorialId = createdTutorial.getId();

        // Wait for async timeline event processing
        TimeUnit.MILLISECONDS.sleep(500);

        // Then - Verify timeline event was created
        List<TimeLineEvent> events = timelineEventRepository
                .findByElementTypeAndElementIdAndTenant("Tutorial", tutorialId.toString(), TENANT_1);

        assertFalse(events.isEmpty(), "Timeline event should be created");
        assertEquals(1, events.size(), "Should have exactly one timeline event");

        TimeLineEvent event = events.get(0);
        assertEquals(EventType.CREATED, event.getEventType());
        assertEquals("Tutorial", event.getElementType());
        assertEquals(tutorialId.toString(), event.getElementId());
        assertNotNull(event.getTimestamp());
        assertNotNull(event.getAttributes());

        // Verify attributes structure and content
        JsonNode attributes = objectMapper.readTree(event.getAttributes().asText());
        JsonNode dataNode = attributes.path("data");

        assertFalse(dataNode.isMissingNode(), "Attributes should have 'data' field");
        assertEquals(tutorialId.longValue(), dataNode.path("id").asLong());
        assertEquals("Spring Boot Tutorial", dataNode.path("title").asText());
        assertEquals("Learn Spring Boot basics", dataNode.path("description").asText());
        assertEquals(false, dataNode.path("published").asBoolean());
        assertEquals(TENANT_1, dataNode.path("tenant").asText());

        // Verify audit fields (should be present even if null)
        assertTrue(dataNode.has("createDate"));
        assertTrue(dataNode.has("createdBy"));
        assertTrue(dataNode.has("updateDate"));
        assertTrue(dataNode.has("updatedBy"));

        System.out.println("CREATED event attributes: " + attributes.toPrettyString());
    }

    @Test
    @Order(2)
    @DisplayName("Should update tutorial and record UPDATED timeline event with diff attributes")
    void testUpdateTutorial_ShouldRecordUpdatedEventWithDiffAttributes() throws Exception {
        // Given - Ensure we have a tutorial to update
        if (tutorialId == null) {
            testCreateTutorial_ShouldRecordCreatedEventWithAttributes();
        }

        TutorialDto updatedTutorial = TutorialDto.builder()
                .id(tutorialId)
                .title("Advanced Spring Boot Tutorial")
                .description("Learn advanced Spring Boot concepts and best practices")
                .published(true)
                .build();

        // When
        mockMvc.perform(put(BASE_URL + "/" + tutorialId)
                        .header(TENANT_HEADER, TENANT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTutorial)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Advanced Spring Boot Tutorial"))
                .andExpect(jsonPath("$.published").value(true));

        // Wait for async processing
        TimeUnit.MILLISECONDS.sleep(500);

        // Then - Verify timeline events
        List<TimeLineEvent> events = timelineEventRepository
                .findByElementTypeAndElementIdAndTenant("Tutorial", tutorialId.toString(), TENANT_1);

        assertEquals(2, events.size(), "Should have 2 timeline events (CREATE + UPDATE)");

        // Find the UPDATE event
        TimeLineEvent updateEvent = events.stream()
                .filter(e -> e.getEventType() == EventType.UPDATED)
                .findFirst()
                .orElse(null);

        assertNotNull(updateEvent, "UPDATE event should exist");

        // Verify attributes structure
        JsonNode attributes = objectMapper.readTree(updateEvent.getAttributes().asText());
        JsonNode dataNode = attributes.path("data");

        assertFalse(dataNode.isMissingNode(), "Attributes should have 'data' field");

        // Verify changed fields
        assertTrue(dataNode.has("title"), "Title should be in update diff");
        assertEquals("Advanced Spring Boot Tutorial", dataNode.path("title").asText());

        assertTrue(dataNode.has("description"), "Description should be in update diff");
        assertEquals("Learn advanced Spring Boot concepts and best practices",
                dataNode.path("description").asText());

        assertTrue(dataNode.has("published"), "Published should be in update diff");
        assertEquals(true, dataNode.path("published").asBoolean());

        System.out.println("UPDATED event attributes: " + attributes.toPrettyString());
    }

    @Test
    @Order(3)
    @DisplayName("Should delete tutorial and record DELETED timeline event with minimal attributes")
    void testDeleteTutorial_ShouldRecordDeletedEventWithMinimalAttributes() throws Exception {
        // Given - Ensure we have a tutorial to delete
        if (tutorialId == null) {
            testCreateTutorial_ShouldRecordCreatedEventWithAttributes();
        }

        // When
        mockMvc.perform(delete(BASE_URL + "/" + tutorialId)
                        .header(TENANT_HEADER, TENANT_1))
                .andExpect(status().isNoContent());

        // Wait for async processing
        TimeUnit.MILLISECONDS.sleep(500);

        // Then - Verify timeline events
        List<TimeLineEvent> events = timelineEventRepository
                .findByElementTypeAndElementIdAndTenant("Tutorial", tutorialId.toString(), TENANT_1);

        assertEquals(3, events.size(), "Should have 3 timeline events (CREATE + UPDATE + DELETE)");

        // Find the DELETE event
        TimeLineEvent deleteEvent = events.stream()
                .filter(e -> e.getEventType() == EventType.DELETED)
                .findFirst()
                .orElse(null);

        assertNotNull(deleteEvent, "DELETE event should exist");

        // Verify attributes structure
        JsonNode attributes = objectMapper.readTree(deleteEvent.getAttributes().asText());
        JsonNode dataNode = attributes.path("data");

        // For DELETE events, we expect minimal information
        assertTrue(dataNode.has("id"), "DELETE event should have ID");
        assertEquals(tutorialId.longValue(), dataNode.path("id").asLong());
        assertTrue(dataNode.has("tenant"), "DELETE event should have tenant");
        assertEquals(TENANT_1, dataNode.path("tenant").asText());

        System.out.println("DELETED event attributes: " + attributes.toPrettyString());
    }

    @Test
    @Order(4)
    @DisplayName("Should create tutorial with different data and verify attributes structure")
    void testCreateTutorialWithDifferentData_ShouldHaveCorrectAttributesStructure() throws Exception {
        // Given
        TutorialDto tutorial = TutorialDto.builder()
                .title("React Development Guide")
                .description("Complete guide to React development with hooks and context")
                .published(true)
                .build();

        // When
        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tutorial)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        TutorialDto createdTutorial = objectMapper.readValue(responseContent, TutorialDto.class);
        Long newTutorialId = createdTutorial.getId();

        // Wait for async processing
        TimeUnit.MILLISECONDS.sleep(500);

        // Then - Verify timeline event
        List<TimeLineEvent> events = timelineEventRepository
                .findByElementTypeAndElementIdAndTenant("Tutorial", newTutorialId.toString(), TENANT_1);

        assertEquals(1, events.size(), "Should have one CREATED event");
        TimeLineEvent event = events.get(0);

        JsonNode attributes = objectMapper.readTree(event.getAttributes().asText());
        JsonNode dataNode = attributes.path("data");

        // Verify structure and values
        assertEquals("React Development Guide", dataNode.path("title").asText());
        assertEquals("Complete guide to React development with hooks and context",
                dataNode.path("description").asText());
        assertEquals(true, dataNode.path("published").asBoolean());
        assertEquals(TENANT_1, dataNode.path("tenant").asText());

        System.out.println("Second tutorial CREATED event attributes: " + attributes.toPrettyString());
    }

    @Test
    @Order(5)
    @DisplayName("Should handle multiple updates and verify each UPDATE event attributes")
    void testMultipleUpdates_ShouldRecordEachUpdateWithCorrectAttributes() throws Exception {
        // Given - Create a tutorial first
        TutorialDto tutorial = TutorialDto.builder()
                .title("Multi-Update Test")
                .description("Testing multiple updates")
                .published(false)
                .build();

        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tutorial)))
                .andExpect(status().isCreated())
                .andReturn();

        TutorialDto createdTutorial = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), TutorialDto.class);
        Long testTutorialId = createdTutorial.getId();

        TimeUnit.MILLISECONDS.sleep(200);

        // When - Perform first update (title only)
        TutorialDto firstUpdate = TutorialDto.builder()
                .id(testTutorialId)
                .title("Multi-Update Test - Updated Title")
                .description("Testing multiple updates")
                .published(false)
                .build();

        mockMvc.perform(put(BASE_URL + "/" + testTutorialId)
                        .header(TENANT_HEADER, TENANT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstUpdate)))
                .andExpect(status().isOk());

        TimeUnit.MILLISECONDS.sleep(200);

        // Second update (description and published)
        TutorialDto secondUpdate = TutorialDto.builder()
                .id(testTutorialId)
                .title("Multi-Update Test - Updated Title")
                .description("Testing multiple updates - Updated description")
                .published(true)
                .build();

        mockMvc.perform(put(BASE_URL + "/" + testTutorialId)
                        .header(TENANT_HEADER, TENANT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondUpdate)))
                .andExpect(status().isOk());

        TimeUnit.MILLISECONDS.sleep(500);

        // Then - Verify all timeline events
        List<TimeLineEvent> events = timelineEventRepository
                .findByElementTypeAndElementIdAndTenant("Tutorial", testTutorialId.toString(), TENANT_1);

        assertEquals(3, events.size(), "Should have CREATE + 2 UPDATE events");

        // Sort by timestamp to ensure correct order
        events.sort((e1, e2) -> e1.getTimestamp().compareTo(e2.getTimestamp()));

        // Verify first UPDATE event attributes
        JsonNode firstUpdateAttributes = objectMapper.readTree(events.get(1).getAttributes().asText());
        JsonNode firstDataNode = firstUpdateAttributes.path("data");
        assertEquals("Multi-Update Test - Updated Title", firstDataNode.path("title").asText());
        assertEquals("Testing multiple updates", firstDataNode.path("description").asText());
        assertEquals(false, firstDataNode.path("published").asBoolean());

        // Verify second UPDATE event attributes
        JsonNode secondUpdateAttributes = objectMapper.readTree(events.get(2).getAttributes().asText());
        JsonNode secondDataNode = secondUpdateAttributes.path("data");
        assertEquals("Multi-Update Test - Updated Title", secondDataNode.path("title").asText());
        assertEquals("Testing multiple updates - Updated description", secondDataNode.path("description").asText());
        assertEquals(true, secondDataNode.path("published").asBoolean());
    }

    @Test
    @Order(6)
    @DisplayName("Should verify timeline events chronological order and completeness")
    void testCompleteLifecycle_ShouldRecordAllEventsWithCorrectAttributes() throws Exception {
        // Given
        TutorialDto tutorial = TutorialDto.builder()
                .title("Complete Lifecycle Test")
                .description("Testing complete CRUD lifecycle")
                .published(false)
                .build();

        // When - Complete lifecycle: CREATE -> UPDATE -> DELETE
        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tutorial)))
                .andExpect(status().isCreated())
                .andReturn();

        TutorialDto createdTutorial = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), TutorialDto.class);
        Long lifecycleTestId = createdTutorial.getId();

        TimeUnit.MILLISECONDS.sleep(100);

        TutorialDto updatedTutorial = TutorialDto.builder()
                .id(lifecycleTestId)
                .title("Complete Lifecycle Test - Updated")
                .description("Testing complete CRUD lifecycle - Updated")
                .published(true)
                .build();

        mockMvc.perform(put(BASE_URL + "/" + lifecycleTestId)
                        .header(TENANT_HEADER, TENANT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTutorial)))
                .andExpect(status().isOk());

        TimeUnit.MILLISECONDS.sleep(100);

        mockMvc.perform(delete(BASE_URL + "/" + lifecycleTestId)
                        .header(TENANT_HEADER, TENANT_1))
                .andExpect(status().isNoContent());

        TimeUnit.MILLISECONDS.sleep(500);

        // Then - Verify complete timeline
        List<TimeLineEvent> events = timelineEventRepository
                .findByElementTypeAndElementIdAndTenant("Tutorial", lifecycleTestId.toString(), TENANT_1);

        assertEquals(3, events.size(), "Should have CREATE, UPDATE, and DELETE events");

        // Sort by timestamp
        events.sort((e1, e2) -> e1.getTimestamp().compareTo(e2.getTimestamp()));

        // Verify event types in order
        assertEquals(EventType.CREATED, events.get(0).getEventType());
        assertEquals(EventType.UPDATED, events.get(1).getEventType());
        assertEquals(EventType.DELETED, events.get(2).getEventType());

        // Verify timestamp ordering
        assertTrue(events.get(0).getTimestamp().isBefore(events.get(1).getTimestamp()));
        assertTrue(events.get(1).getTimestamp().isBefore(events.get(2).getTimestamp()));

        // Verify CREATE event has full entity data
        JsonNode createAttributes = objectMapper.readTree(events.get(0).getAttributes().asText());
        JsonNode createData = createAttributes.path("data");
        assertEquals("Complete Lifecycle Test", createData.path("title").asText());
        assertEquals("Testing complete CRUD lifecycle", createData.path("description").asText());

        // Verify UPDATE event has changes
        JsonNode updateAttributes = objectMapper.readTree(events.get(1).getAttributes().asText());
        JsonNode updateData = updateAttributes.path("data");
        assertEquals("Complete Lifecycle Test - Updated", updateData.path("title").asText());
        assertEquals(true, updateData.path("published").asBoolean());

        // Verify DELETE event has minimal info
        JsonNode deleteAttributes = objectMapper.readTree(events.get(2).getAttributes().asText());
        JsonNode deleteData = deleteAttributes.path("data");
        assertEquals(lifecycleTestId.longValue(), deleteData.path("id").asLong());
        assertEquals(TENANT_1, deleteData.path("tenant").asText());
    }

    @Test
    @Order(7)
    @DisplayName("Should verify tenant isolation of timeline events")
    void testTimelineEventsTenantIsolation() throws Exception {
        // Given - Create tutorial for tenant1
        TutorialDto tenant1Tutorial = TutorialDto.builder()
                .title("Tenant1 Tutorial")
                .description("Only visible to tenant1")
                .published(true)
                .build();

        MvcResult tenant1Result = mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tenant1Tutorial)))
                .andExpect(status().isCreated())
                .andReturn();

        TutorialDto createdTenant1Tutorial = objectMapper.readValue(
                tenant1Result.getResponse().getContentAsString(), TutorialDto.class);
        Long tenant1TutorialId = createdTenant1Tutorial.getId();

        // Create tutorial for tenant2
        TutorialDto tenant2Tutorial = TutorialDto.builder()
                .title("Tenant2 Tutorial")
                .description("Only visible to tenant2")
                .published(true)
                .build();

        MvcResult tenant2Result = mockMvc.perform(post(BASE_URL)
                        .header(TENANT_HEADER, TENANT_2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tenant2Tutorial)))
                .andExpect(status().isCreated())
                .andReturn();

        TutorialDto createdTenant2Tutorial = objectMapper.readValue(
                tenant2Result.getResponse().getContentAsString(), TutorialDto.class);
        Long tenant2TutorialId = createdTenant2Tutorial.getId();

        TimeUnit.MILLISECONDS.sleep(500);

        // When - Query timeline events for tenant1
        List<TimeLineEvent> tenant1Events = timelineEventRepository
                .findByElementTypeAndElementIdAndTenant("Tutorial", tenant1TutorialId.toString(), TENANT_1);

        List<TimeLineEvent> tenant2Events = timelineEventRepository
                .findByElementTypeAndElementIdAndTenant("Tutorial", tenant2TutorialId.toString(), TENANT_2);

        // Then - Verify isolation
        assertEquals(1, tenant1Events.size(), "Tenant1 should have 1 event");
        assertEquals(TENANT_1, tenant1Events.get(0).getTenant(), "Event should belong to tenant1");

        assertEquals(1, tenant2Events.size(), "Tenant2 should have 1 event");
        assertEquals(TENANT_2, tenant2Events.get(0).getTenant(), "Event should belong to tenant2");

        // Verify tenant1 cannot see tenant2's events
        List<TimeLineEvent> tenant1ViewOfTenant2Events = timelineEventRepository
                .findByElementTypeAndElementIdAndTenant("Tutorial", tenant2TutorialId.toString(), TENANT_1);

        assertTrue(tenant1ViewOfTenant2Events.isEmpty(),
                "Tenant1 should not see tenant2's timeline events");
    }

    @AfterEach
    void tearDown() {
        // Clean up any remaining test data if needed
    }

    @AfterAll
    static void cleanUp() {
        // Any final cleanup if needed
    }
}