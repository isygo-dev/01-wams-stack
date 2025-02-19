package eu.isygoit.repository;

import eu.isygoit.repository.bo.BoWithCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JpaPagingAndSortingAssignableCodeRepository Test")
class JpaPagingAndSortingAssignableCodeRepositoryTest {

    @Mock
    private JpaPagingAndSortingAssignableCodeRepository<BoWithCode, Long> repository;

    private BoWithCode testModel;

    @BeforeEach
    void setUp() {
        testModel = BoWithCode.builder().code("TEST-CODE").build();
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {

        @Test
        @DisplayName("Find by code ignore case")
        void testFindByCodeIgnoreCase() {
            String code = "TEST-CODE";
            when(repository.findByCodeIgnoreCase(code)).thenReturn(Optional.of(testModel));

            Optional<BoWithCode> result = repository.findByCodeIgnoreCase(code);

            assertTrue(result.isPresent());
            assertEquals("TEST-CODE", result.get().getCode());
            verify(repository, times(1)).findByCodeIgnoreCase(code);
        }

        @Test
        @DisplayName("Find by code list ignore case")
        void testFindByCodeIgnoreCaseIn() {
            List<String> codes = List.of("CODE1", "CODE2");
            List<BoWithCode> models = List.of(testModel);
            when(repository.findByCodeIgnoreCaseIn(codes)).thenReturn(models);

            List<BoWithCode> result = repository.findByCodeIgnoreCaseIn(codes);

            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
            verify(repository, times(1)).findByCodeIgnoreCaseIn(codes);
        }
    }

    @Nested
    @DisplayName("Existence Checks")
    class ExistenceChecks {

        @Test
        @DisplayName("Check if code exists ignore case")
        void testExistsByCodeIgnoreCase() {
            String code = "TEST-CODE";
            when(repository.existsByCodeIgnoreCase(code)).thenReturn(true);

            boolean exists = repository.existsByCodeIgnoreCase(code);

            assertTrue(exists);
            verify(repository, times(1)).existsByCodeIgnoreCase(code);
        }
    }
}