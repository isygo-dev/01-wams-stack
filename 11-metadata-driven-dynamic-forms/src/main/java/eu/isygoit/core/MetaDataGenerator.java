package eu.isygoit.core;


import eu.isygoit.annotation.FormView;
import eu.isygoit.domain.ViewMetaData;
import eu.isygoit.exception.MetaDataGenerationException;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main orchestrator of the metadata library.
 * Provides clean API for generating ViewMetaData from annotated classes.
 *
 * This is the primary class that users of the library will interact with.
 */
public class MetaDataGenerator {

    private final MetaDataScanner scanner;
    private final MetaDataCache cache;
    private final AnnotationProcessor processor;   // kept for direct access if needed

    // Registry to avoid repeated scanning of the same class
    private final ConcurrentHashMap<String, Class<?>> viewClassRegistry = new ConcurrentHashMap<>();

    public MetaDataGenerator() {
        this.processor = new AnnotationProcessor();
        this.scanner = new MetaDataScanner(processor);
        this.cache = new MetaDataCache();
    }

    /**
     * Register a View class so it can be referenced by name.
     */
    public void registerView(Class<?> viewClass) {
        FormView formView = viewClass.getAnnotation(FormView.class);
        if (formView == null) {
            throw new MetaDataGenerationException("Class " + viewClass.getName() + " must be annotated with @FormView");
        }
        viewClassRegistry.put(formView.name(), viewClass);
    }

    /**
     * Generate metadata for a registered view by name.
     */
    public ViewMetaData generate(String viewName) {
        return generate(viewName, "1.0");
    }

    /**
     * Generate metadata with explicit version.
     */
    public ViewMetaData generate(String viewName, String version) {
        Objects.requireNonNull(viewName, "viewName cannot be null");

        // Try cache first
        ViewMetaData cached = cache.get(viewName, version);
        if (cached != null) {
            return cached;
        }

        // Get registered class
        Class<?> viewClass = viewClassRegistry.get(viewName);
        if (viewClass == null) {
            throw new MetaDataGenerationException("No view registered with name: " + viewName);
        }

        // Generate using scanner (reflection happens here)
        ViewMetaData metadata = scanner.scan(viewClass);

        // Cache the result
        cache.put(viewName, version, metadata);

        return metadata;
    }

    /**
     * Generate directly from class (bypasses registry - useful for testing or one-off use).
     */
    public ViewMetaData generateFromClass(Class<?> viewClass) {
        Objects.requireNonNull(viewClass, "viewClass cannot be null");

        FormView formView = viewClass.getAnnotation(FormView.class);
        if (formView == null) {
            throw new MetaDataGenerationException("Class must be annotated with @FormView");
        }

        String viewName = formView.name();
        String version = formView.version();

        return cache.getOrCompute(viewName, version, () -> scanner.scan(viewClass));
    }

    /**
     * Invalidate cache for a specific view (useful during development or hot-reload scenarios).
     */
    public void invalidate(String viewName) {
        cache.invalidate(viewName);
    }

    public void clearCache() {
        cache.clear();
    }

    // Expose cache stats for monitoring
    public com.github.benmanes.caffeine.cache.stats.CacheStats getCacheStats() {
        return cache.stats();
    }
}