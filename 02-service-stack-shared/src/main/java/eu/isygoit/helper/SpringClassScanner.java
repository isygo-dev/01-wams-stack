package eu.isygoit.helper;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The type Spring class scanner.
 * This service scans for classes annotated with a specific annotation within a given package.
 * It returns a set of bean definitions representing those classes.
 */
@Service
@Transactional
@Slf4j
public class SpringClassScanner {

    /**
     * Finds all classes in the specified package annotated with the provided annotation.
     * This method scans the given package for components matching the annotation and returns the corresponding bean definitions.
     *
     * @param annotationClass the annotation class to filter by
     * @param scanPackage     the package to scan for annotated classes
     * @return a set of BeanDefinitions for the classes that match the annotation
     */
    public Set<BeanDefinition> findAnnotatedClasses(Class<? extends Annotation> annotationClass, String scanPackage) {
        log.info("Starting scan in package '{}' for classes annotated with '{}'.", scanPackage, annotationClass.getSimpleName());

        // Validate input parameters before proceeding
        if (!isValidPackage(scanPackage)) {
            log.error("Invalid package name provided: '{}'. Please provide a valid package.", scanPackage);
            return Set.of();
        }
        if (!isValidAnnotationClass(annotationClass)) {
            log.error("Invalid annotation class provided: '{}'.", annotationClass.getSimpleName());
            return Set.of();
        }

        // Create the component scanner with the appropriate annotation filter
        var provider = createComponentScanner(annotationClass);
        log.debug("Component scanner created with annotation filter '{}'.", annotationClass.getSimpleName());

        // Perform the scan and retrieve the candidate components (classes) from the specified package
        Set<BeanDefinition> candidates = scanForClasses(provider, scanPackage);

        // Check if candidates were found and log the result
        logCandidateScanResult(candidates, annotationClass, scanPackage);

        // Extract and log class names from the candidates (if any)
        logClassNames(candidates);

        return candidates;
    }

    /**
     * Scans the specified package for candidate components using the given provider.
     * This method centralizes the scanning logic.
     *
     * @param provider the configured component provider
     * @param scanPackage the package to scan
     * @return a set of BeanDefinitions representing the matching classes
     */
    private Set<BeanDefinition> scanForClasses(ClassPathScanningCandidateComponentProvider provider, String scanPackage) {
        log.debug("Scanning package '{}' for candidate components.", scanPackage);
        return provider.findCandidateComponents(scanPackage);
    }

    /**
     * Logs the result of the annotation class scan.
     * This utility method ensures consistent logging of scan results.
     *
     * @param candidates the set of candidate classes found
     * @param annotationClass the annotation class being searched for
     * @param scanPackage the package that was scanned
     */
    private void logCandidateScanResult(Set<BeanDefinition> candidates, Class<? extends Annotation> annotationClass, String scanPackage) {
        Optional.ofNullable(candidates)
                .filter(set -> !set.isEmpty())
                .ifPresentOrElse(
                        set -> log.info("Successfully found {} classes annotated with '{}'.", set.size(), annotationClass.getSimpleName()),
                        () -> log.warn("No classes annotated with '{}' found in package '{}'.", annotationClass.getSimpleName(), scanPackage)
                );
    }

    /**
     * Creates and configures a component scanner that includes only classes annotated with the specified annotation.
     * This method is responsible for setting up the scanner to filter by the given annotation type.
     *
     * @param annotationClass the annotation class to filter the components by
     * @return a configured ClassPathScanningCandidateComponentProvider
     */
    private ClassPathScanningCandidateComponentProvider createComponentScanner(Class<? extends Annotation> annotationClass) {
        log.debug("Initializing component scanner for annotation '{}'.", annotationClass.getSimpleName());

        // Initialize the scanner with 'false' to disable scanning of non-annotated components (like primitive types)
        var provider = new ClassPathScanningCandidateComponentProvider(false);

        // Add an annotation filter for the provided annotation class
        provider.addIncludeFilter(new AnnotationTypeFilter(annotationClass));

        // Log the successful configuration of the scanner
        log.debug("Component scanner successfully configured to filter by annotation '{}'.", annotationClass.getSimpleName());

        return provider;
    }

    /**
     * Utility method to log a message for an empty or null set of candidates.
     * This method centralizes logging logic to ensure consistency and avoid repetition.
     *
     * @param candidates the set of candidates to check
     * @param annotationClass the annotation class being looked for
     * @param scanPackage the package being scanned
     */
    private void logEmptyCandidateSet(Set<BeanDefinition> candidates, Class<? extends Annotation> annotationClass, String scanPackage) {
        if (candidates == null || candidates.isEmpty()) {
            log.warn("No candidates found for annotation '{}' in package '{}'.", annotationClass.getSimpleName(), scanPackage);
        }
    }

    /**
     * Validates if the provided package name is valid.
     * A valid package should not be null or empty.
     *
     * @param packageName the package name to validate
     * @return true if the package is valid, false otherwise
     */
    private boolean isValidPackage(String packageName) {
        return packageName != null && !packageName.trim().isEmpty();
    }

    /**
     * Validates if the provided annotation class is valid.
     * An annotation class should not be null.
     *
     * @param annotationClass the annotation class to validate
     * @return true if the annotation class is valid, false otherwise
     */
    private boolean isValidAnnotationClass(Class<? extends Annotation> annotationClass) {
        return annotationClass != null;
    }

    /**
     * Logs the names of all classes found in the candidates set.
     * This utility method extracts and logs the class names for better insight into the scan result.
     *
     * @param candidates the set of candidate classes
     */
    private void logClassNames(Set<BeanDefinition> candidates) {
        candidates.stream()
                .map(BeanDefinition::getBeanClassName)
                .forEach(className -> log.debug("Found class: {}", className));
    }

    /**
     * Filters the classes based on specific criteria (example: excluding certain classes).
     * This method can be customized with additional filters as needed.
     *
     * @param candidates the set of candidate classes
     * @return a set of filtered candidates
     */
    private Set<BeanDefinition> filterClasses(Set<BeanDefinition> candidates) {
        // Example: Exclude classes whose names contain "Test" (can be extended)
        return candidates.stream()
                .filter(candidate -> !candidate.getBeanClassName().contains("Test"))
                .collect(Collectors.toSet());
    }

    /**
     * Handles exceptions that may occur during the scanning process.
     * This method can be expanded to handle specific exceptions (e.g., ClassNotFoundException).
     *
     * @param exception the exception to handle
     */
    private void handleScanException(Exception exception) {
        log.error("An error occurred during the class scanning process: {}", exception.getMessage(), exception);
    }
}