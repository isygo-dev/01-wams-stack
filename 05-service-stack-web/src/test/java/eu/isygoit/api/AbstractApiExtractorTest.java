package eu.isygoit.api;

import eu.isygoit.enums.IEnumRequest;
import eu.isygoit.model.extendable.ApiPermissionModel;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AbstractApiExtractor Tests")
class AbstractApiExtractorTest {

    @SuperBuilder
    @NoArgsConstructor
    public static class TestApiPermissionModel extends ApiPermissionModel<Long> {
        private Long id;

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public void setId(Long id) {
            this.id = id;
        }
    }

    private static class TestApiExtractor extends AbstractApiExtractor<TestApiPermissionModel> {
        @Override
        public TestApiPermissionModel saveApi(TestApiPermissionModel api) {
            // Simulate saving by returning the same object
            return api;
        }

        @Override
        public TestApiPermissionModel newInstance() {
            return new TestApiPermissionModel();
        }
    }

    @RestController
    @RequestMapping(path = "/api/test")
    private static class SampleController {
        @GetMapping(path = "/get-method")
        public void getMethod() {}

        @PostMapping(path = "/post-method")
        public void postMethod() {}

        @PutMapping(path = "/put-method")
        public void putMethod() {}

        @DeleteMapping(path = "/delete-method")
        public void deleteMethod() {}

        @PatchMapping(path = "/patch-method")
        public void patchMethod() {}

        public void nonApiMethod() {}
    }

    private TestApiExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new TestApiExtractor();
    }

    @Test
    @DisplayName("Should extract all API endpoints from controller")
    void testExtractApis() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        List<TestApiPermissionModel> apis = extractor.extractApis(SampleController.class);

        assertNotNull(apis);
        // GET, POST, PUT, DELETE, PATCH = 5 methods
        assertEquals(5, apis.size());

        // Check if all types are present
        assertTrue(apis.stream().anyMatch(api -> api.getRqType() == IEnumRequest.Types.GET && api.getPath().equals("/api/test/get-method")));
        assertTrue(apis.stream().anyMatch(api -> api.getRqType() == IEnumRequest.Types.POST && api.getPath().equals("/api/test/post-method")));
        assertTrue(apis.stream().anyMatch(api -> api.getRqType() == IEnumRequest.Types.PUT && api.getPath().equals("/api/test/put-method")));
        assertTrue(apis.stream().anyMatch(api -> api.getRqType() == IEnumRequest.Types.DELETE && api.getPath().equals("/api/test/delete-method")));
        assertTrue(apis.stream().anyMatch(api -> api.getRqType() == IEnumRequest.Types.PATCH && api.getPath().equals("/api/test/patch-method")));

        // Check common fields
        for (TestApiPermissionModel api : apis) {
            assertEquals("Sample", api.getObject());
            assertNotNull(api.getMethod());
            assertNotNull(api.getDescription());
        }
    }

    @Test
    @DisplayName("Should return empty list if controller has no RequestMapping")
    void testExtractApisNoRequestMapping() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        class NoMappingController {}
        List<TestApiPermissionModel> apis = extractor.extractApis(NoMappingController.class);
        assertTrue(apis.isEmpty());
    }
}
