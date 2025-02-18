package eu.isygoit.repository;

import eu.isygoit.repository.bo.NextCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NextCodeRepositoryTest {

    @Mock
    private NextCodeRepository<Long, NextCode> nextCodeRepository;

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

    @Test
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

    @Test
    void testIncrement() {
        String domain = "testDomain";
        String entity = "TestEntity";
        int increment = 2;

        doNothing().when(nextCodeRepository).increment(domain, entity, increment);

        nextCodeRepository.increment(domain, entity, increment);

        verify(nextCodeRepository, times(1)).increment(domain, entity, increment);
    }

    @Test
    void testNextCodeMethod() {
        NextCode nextModel = (NextCode) testModel.nextCode();

        assertEquals(102L, nextModel.getValue());
        assertEquals("PRE000102SUF", nextModel.getCode());
    }
}
