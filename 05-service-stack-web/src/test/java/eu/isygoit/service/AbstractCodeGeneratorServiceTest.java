package eu.isygoit.service;

import eu.isygoit.model.extendable.NextCodeModel;
import eu.isygoit.repository.code.NextCodeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AbstractCodeGeneratorService Tests")
class AbstractCodeGeneratorServiceTest {

    @Mock
    private NextCodeRepository<TestNextCode, Long> repository;

    static class TestNextCode extends NextCodeModel<Long> {
        private Long id;
        @Override
        public Long getId() { return id; }
        @Override
        public void setId(Long id) { this.id = id; }
        @Override
        public String getTenant() { return "tenant"; }
        @Override
        public void setTenant(String tenant) {}
    }

    static class TestCodeGeneratorService extends AbstractCodeGeneratorService<TestNextCode> {
        private final NextCodeRepository<TestNextCode, Long> repository;

        TestCodeGeneratorService(NextCodeRepository<TestNextCode, Long> repository) {
            this.repository = repository;
        }

        @Override
        public NextCodeRepository nextCodeRepository() {
            return repository;
        }
    }

    @Test
    @DisplayName("findByEntity should call repository")
    void testFindByEntity() {
        TestCodeGeneratorService service = new TestCodeGeneratorService(repository);
        TestNextCode nextCode = new TestNextCode();
        when(repository.findByEntity("entity")).thenReturn(Optional.of(nextCode));

        Optional<TestNextCode> result = service.findByEntity("entity");

        assertTrue(result.isPresent());
        assertEquals(nextCode, result.get());
        verify(repository).findByEntity("entity");
    }

    @Test
    @DisplayName("findByTenantAndEntityAndAttribute should call repository")
    void testFindByTenantAndEntityAndAttribute() {
        TestCodeGeneratorService service = new TestCodeGeneratorService(repository);
        TestNextCode nextCode = new TestNextCode();
        when(repository.findByTenantIgnoreCaseAndEntityAndAttribute("tenant", "entity", "attr")).thenReturn(Optional.of(nextCode));

        Optional<TestNextCode> result = service.findByTenantAndEntityAndAttribute("tenant", "entity", "attr");

        assertTrue(result.isPresent());
        assertEquals(nextCode, result.get());
        verify(repository).findByTenantIgnoreCaseAndEntityAndAttribute("tenant", "entity", "attr");
    }

    @Test
    @DisplayName("increment should call repository increment and flush")
    void testIncrement() {
        TestCodeGeneratorService service = new TestCodeGeneratorService(repository);
        service.increment("tenant", "entity", 1);

        verify(repository).increment("tenant", "entity", 1);
        verify(repository).flush();
    }

    @Test
    @DisplayName("saveAndFlush should call repository saveAndFlush")
    void testSaveAndFlush() {
        TestCodeGeneratorService service = new TestCodeGeneratorService(repository);
        TestNextCode nextCode = new TestNextCode();
        when(repository.saveAndFlush(nextCode)).thenReturn(nextCode);

        TestNextCode result = service.saveAndFlush(nextCode);

        assertEquals(nextCode, result);
        verify(repository).saveAndFlush(nextCode);
    }

    @Test
    @DisplayName("save should call repository save")
    void testSave() {
        TestCodeGeneratorService service = new TestCodeGeneratorService(repository);
        TestNextCode nextCode = new TestNextCode();
        when(repository.save(nextCode)).thenReturn(nextCode);

        TestNextCode result = service.save(nextCode);

        assertEquals(nextCode, result);
        verify(repository).save(nextCode);
    }
}
