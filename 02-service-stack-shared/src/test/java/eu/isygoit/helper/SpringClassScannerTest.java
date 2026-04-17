package eu.isygoit.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.stereotype.Service;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SpringClassScanner Tests")
class SpringClassScannerTest {

    private final SpringClassScanner scanner = new SpringClassScanner();

    @Test
    @DisplayName("should find annotated classes")
    void findAnnotatedClasses_shouldFindClasses() {
        // Search for @Service in eu.isygoit.helper package
        Set<BeanDefinition> candidates = scanner.findAnnotatedClasses(Service.class, "eu.isygoit.helper");

        assertFalse(candidates.isEmpty(), "Should find at least SpringClassScanner itself");
        assertTrue(candidates.stream().anyMatch(bd -> bd.getBeanClassName().equals(SpringClassScanner.class.getName())));
    }

    @Test
    @DisplayName("should handle non-existent package")
    void findAnnotatedClasses_shouldHandleNonExistentPackage() {
        Set<BeanDefinition> candidates = scanner.findAnnotatedClasses(Service.class, "non.existent.package");
        assertTrue(candidates.isEmpty());
    }

    @Test
    @DisplayName("should handle invalid inputs")
    void findAnnotatedClasses_shouldHandleInvalidInputs() {
        assertTrue(scanner.findAnnotatedClasses(null, "eu.isygoit.helper").isEmpty());
        assertTrue(scanner.findAnnotatedClasses(Service.class, null).isEmpty());
        assertTrue(scanner.findAnnotatedClasses(Service.class, "").isEmpty());
    }
}
