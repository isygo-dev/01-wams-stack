package eu.isygoit.form.core;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import eu.isygoit.form.domain.ViewMetaData;

import java.time.Duration;

/**
 * High-performance cache for ViewMetaData using Caffeine.
 * Supports viewName + version + locale as composite key for future i18n support.
 */
public class MetaDataCache {

    private final Cache<String, ViewMetaData> cache;

    public MetaDataCache() {
        this.cache = Caffeine.newBuilder()
                .maximumSize(500)                    // Configurable via properties later
                .expireAfterAccess(Duration.ofMinutes(30))
                .recordStats()
                .build();
    }

    /**
     * Retrieves metadata from cache or computes and caches it.
     */
    public ViewMetaData getOrCompute(String viewName, String version, java.util.function.Supplier<ViewMetaData> loader) {
        String cacheKey = buildCacheKey(viewName, version);
        return cache.get(cacheKey, k -> loader.get());
    }

    /**
     * Simple get without loader (for manual invalidation scenarios).
     */
    public ViewMetaData get(String viewName, String version) {
        String cacheKey = buildCacheKey(viewName, version);
        return cache.getIfPresent(cacheKey);
    }

    public void put(String viewName, String version, ViewMetaData metadata) {
        String cacheKey = buildCacheKey(viewName, version);
        cache.put(cacheKey, metadata);
    }

    public void invalidate(String viewName) {
        // Invalidate all versions for a view
        cache.asMap().keySet().removeIf(key -> key.startsWith(viewName + ":"));
    }

    public void clear() {
        cache.invalidateAll();
    }

    private String buildCacheKey(String viewName, String version) {
        return viewName + ":" + (version != null ? version : "1.0");
    }

    // For monitoring / actuator in future Spring integration
    public com.github.benmanes.caffeine.cache.stats.CacheStats stats() {
        return cache.stats();
    }
}