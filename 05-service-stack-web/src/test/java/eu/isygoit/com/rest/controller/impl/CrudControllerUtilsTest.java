package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.annotation.InjectMapper;
import eu.isygoit.annotation.InjectMapperAndService;
import eu.isygoit.annotation.InjectService;
import eu.isygoit.annotation.RestConfiguration;
import eu.isygoit.app.ApplicationContextService;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.dto.IDto;
import eu.isygoit.dto.IIdAssignableDto;
import eu.isygoit.exception.BeanNotFoundException;
import eu.isygoit.exception.handler.IExceptionHandler;
import eu.isygoit.mapper.EntityMapper;
import eu.isygoit.model.IIdAssignable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CrudControllerUtils Tests")
class CrudControllerUtilsTest {

    @Mock
    private ApplicationContextService applicationContextService;

    @Mock
    private ControllerExceptionHandler controllerExceptionHandler;

    @InjectMocks
    private TestCrudController controller;

    @BeforeEach
    void setUp() {
        // Clear caches to ensure test isolation
        ((com.github.benmanes.caffeine.cache.Cache) ReflectionTestUtils.getField(CrudControllerUtils.class, "serviceClassCache")).invalidateAll();
        ((com.github.benmanes.caffeine.cache.Cache) ReflectionTestUtils.getField(CrudControllerUtils.class, "mapperClassCache")).invalidateAll();
        ((com.github.benmanes.caffeine.cache.Cache) ReflectionTestUtils.getField(CrudControllerUtils.class, "minMapperClassCache")).invalidateAll();

        // CrudControllerUtils uses @Autowired for ControllerExceptionHandler
        ReflectionTestUtils.setField(controller, "controllerExceptionHandler", controllerExceptionHandler);
        lenient().when(controllerExceptionHandler.getApplicationContextService()).thenReturn(applicationContextService);
        lenient().when(applicationContextService.getBean(any())).thenReturn(Optional.of(mock(Object.class)));
    }

    interface TestService extends ICrudServiceUtils<Long, TestEntity> {
    }

    interface TestMapper extends EntityMapper<TestEntity, TestDto> {
    }

    // Test helper classes
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestEntity implements IIdAssignable<Long> {
        private Long id;
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestDto implements IIdAssignableDto<Long>, IDto {
        private Long id;

        @Override
        public void setId(Long id) {
            this.id = id;
        }

        @Override
        public boolean isEmpty() {
            return id == null;
        }

        @Override
        public String getSectionName() {
            return "test";
        }
    }

    @InjectMapperAndService(
            mapper = TestMapper.class,
            minMapper = TestMapper.class,
            service = TestService.class,
            handler = IExceptionHandler.class
    )
    static class TestCrudController extends CrudControllerUtils<Long, TestEntity, TestDto, TestDto, TestService> {
    }

    @RestConfiguration(
            mapper = TestMapper.class,
            minMapper = TestMapper.class,
            service = TestService.class,
            exceptionHandler = IExceptionHandler.class
    )
    static class TestRestConfigurationController extends CrudControllerUtils<Long, TestEntity, TestDto, TestDto, TestService> {
    }

    @InjectMapper(mapper = TestMapper.class, minMapper = TestMapper.class)
    @InjectService(TestService.class)
    static class TestCrudControllerWithSeparateAnnotations extends CrudControllerUtils<Long, TestEntity, TestDto, TestDto, TestService> {
    }

    static class TestCrudControllerNoAnnotations extends CrudControllerUtils<Long, TestEntity, TestDto, TestDto, TestService> {
    }

    @Nested
    @DisplayName("Annotation-based Injection Tests")
    class InjectionTests {

        @Test
        @DisplayName("crudService() should return service from InjectMapperAndService annotation")
        void testCrudServiceInjectionFromMapperAndService() throws Exception {
            TestService mockService = mock(TestService.class);
            when(applicationContextService.getBean(TestService.class)).thenReturn(Optional.of(mockService));

            TestService result = controller.crudService();

            assertNotNull(result);
            assertEquals(mockService, result);
        }

        @Test
        @DisplayName("crudService() should return service from InjectService annotation")
        void testCrudServiceInjectionFromInjectService() throws Exception {
            TestCrudControllerWithSeparateAnnotations controller2 = new TestCrudControllerWithSeparateAnnotations();
            ReflectionTestUtils.setField(controller2, "controllerExceptionHandler", controllerExceptionHandler);
            // Re-mock because it's a new instance calling crudService()
            when(controllerExceptionHandler.getApplicationContextService()).thenReturn(applicationContextService);

            TestService mockService = mock(TestService.class);
            when(applicationContextService.getBean(TestService.class)).thenReturn(Optional.of(mockService));

            TestService result = controller2.crudService();

            assertNotNull(result);
            assertEquals(mockService, result);
        }

        @Test
        @DisplayName("crudService() should return service from RestConfiguration annotation")
        void testCrudServiceInjectionFromRestConfiguration() throws Exception {
            TestRestConfigurationController controllerRest = new TestRestConfigurationController();
            ReflectionTestUtils.setField(controllerRest, "controllerExceptionHandler", controllerExceptionHandler);
            // Re-mock because it's a new instance calling crudService()
            when(controllerExceptionHandler.getApplicationContextService()).thenReturn(applicationContextService);

            TestService mockService = mock(TestService.class);
            when(applicationContextService.getBean(TestService.class)).thenReturn(Optional.of(mockService));

            TestService result = controllerRest.crudService();

            assertNotNull(result);
            assertEquals(mockService, result);
        }

        @Test
        @DisplayName("mapper() should return mapper from InjectMapperAndService annotation")
        void testMapperInjectionFromMapperAndService() throws Exception {
            TestMapper mockMapper = mock(TestMapper.class);
            when(applicationContextService.getBean(TestMapper.class)).thenReturn(Optional.of(mockMapper));

            TestMapper result = (TestMapper) controller.mapper();

            assertNotNull(result);
            assertEquals(mockMapper, result);
        }

        @Test
        @DisplayName("minDtoMapper() should return min mapper from InjectMapperAndService annotation")
        void testMinDtoMapperInjectionFromMapperAndService() throws Exception {
            TestMapper mockMapper = mock(TestMapper.class);
            when(applicationContextService.getBean(TestMapper.class)).thenReturn(Optional.of(mockMapper));

            TestMapper result = (TestMapper) controller.minDtoMapper();

            assertNotNull(result);
            assertEquals(mockMapper, result);
        }

        @Test
        @DisplayName("crudService() should throw BeanNotFoundException if service bean not found")
        void testCrudServiceNotFound() {
            when(applicationContextService.getBean(TestService.class)).thenReturn(Optional.empty());

            assertThrows(BeanNotFoundException.class, () -> controller.crudService());
        }
    }

    @Nested
    @DisplayName("Miscellaneous Utility Tests")
    class UtilityTests {

        @Test
        @DisplayName("fullDtoClass and minDtoClass should be correctly resolved from generic types")
        void testGenericClassResolution() {
            assertEquals(TestDto.class, controller.getFullDtoClass());
            assertEquals(TestDto.class, controller.getMinDtoClass());
        }

        @Test
        @DisplayName("validateBulkOperation should throw exception for empty list")
        void testValidateBulkOperationEmpty() {
            assertThrows(eu.isygoit.exception.EmptyListException.class, () -> CrudControllerUtils.validateBulkOperation(java.util.Collections.emptyList()));
        }

        @Test
        @DisplayName("validateBulkOperation should throw exception for large list")
        void testValidateBulkOperationTooLarge() {
            java.util.List<String> largeList = java.util.stream.IntStream.range(0, 1001).mapToObj(i -> "item").toList();
            assertThrows(eu.isygoit.exception.BadArgumentException.class, () -> CrudControllerUtils.validateBulkOperation(largeList));
        }
    }
}
