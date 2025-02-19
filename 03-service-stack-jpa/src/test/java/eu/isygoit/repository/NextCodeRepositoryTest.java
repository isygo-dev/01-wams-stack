package eu.isygoit.repository;

import eu.isygoit.repository.bo.NextCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NextCodeRepository Test")
class NextCodeRepositoryTest {

    @Mock
    private NextCodeRepository<NextCode, Long> nextCodeRepository;

    private NextCode testModel;

    @BeforeEach
    void setUp() {
        testModel = NextCode.builder()
                .domain("testDomain")
                .entity("TestEntity")
                .attribute("TestAttribute")
                .prefix("PRE")
                .suffix("SUF")
                .value(100L)
                .valueLength(6L)
                .increment(2)
                .build();
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {

        @Test
        @DisplayName("Find by entity")
        void testFindByEntity() {
            String entity = "TestEntity";
            when(nextCodeRepository.findByEntity(entity)).thenReturn(Optional.of(testModel));

            Optional<NextCode> result = nextCodeRepository.findByEntity(entity);

            assertTrue(result.isPresent());
            assertEquals(testModel, result.get());
            assertEquals("PRE000100SUF", result.get().getCode());
            verify(nextCodeRepository, times(1)).findByEntity(entity);
        }

        @Test
        @DisplayName("Find by domain, entity, and attribute")
        void testFindByDomainIgnoreCaseAndEntityAndAttribute() {
            String domain = "testDomain";
            String entity = "TestEntity";
            String attribute = "TestAttribute";
            when(nextCodeRepository.findByDomainIgnoreCaseAndEntityAndAttribute(domain, entity, attribute))
                    .thenReturn(Optional.of(testModel));

            Optional<NextCode> result = nextCodeRepository.findByDomainIgnoreCaseAndEntityAndAttribute(domain, entity, attribute);

            assertTrue(result.isPresent());
            assertEquals(testModel, result.get());
            assertEquals("PRE000100SUF", result.get().getCode());
            verify(nextCodeRepository, times(1)).findByDomainIgnoreCaseAndEntityAndAttribute(domain, entity, attribute);
        }
    }

    @Nested
    @DisplayName("Pagination and Counting")
    class PaginationAndCounting {

        @Test
        @DisplayName("Find by domain with pagination")
        void testFindByDomainIgnoreCaseWithPagination() {
            String domain = "testDomain";
            Pageable pageable = mock(Pageable.class);
            Page<NextCode> page = new PageImpl<>(List.of(testModel));
            when(nextCodeRepository.findByDomainIgnoreCase(domain, pageable)).thenReturn(page);

            Page<NextCode> result = nextCodeRepository.findByDomainIgnoreCase(domain, pageable);

            assertFalse(result.isEmpty());
            assertEquals(1, result.getTotalElements());
            assertEquals(testModel, result.getContent().get(0));
        }

        @Test
        @DisplayName("Count by domain")
        void testCountByDomainIgnoreCase() {
            String domain = "testDomain";
            when(nextCodeRepository.countByDomainIgnoreCase(domain)).thenReturn(5L);

            Long count = nextCodeRepository.countByDomainIgnoreCase(domain);

            assertEquals(5L, count);
        }
    }

    @Nested
    @DisplayName("Update Operations")
    class UpdateOperations {

        @Test
        @DisplayName("Increment value")
        void testIncrement() {
            String domain = "testDomain";
            String entity = "TestEntity";
            int increment = 2;

            doNothing().when(nextCodeRepository).increment(domain, entity, increment);

            nextCodeRepository.increment(domain, entity, increment);

            verify(nextCodeRepository, times(1)).increment(domain, entity, increment);
        }
    }

    @Nested
    @DisplayName("Next Code Logic")
    class NextCodeLogic {

        @Test
        @DisplayName("Generate next code")
        void testNextCodeMethod() {
            NextCode nextModel = (NextCode) testModel.nextCode();

            assertEquals(102L, nextModel.getValue());
            assertEquals("PRE000102SUF", nextModel.getCode());
        }
    }
}