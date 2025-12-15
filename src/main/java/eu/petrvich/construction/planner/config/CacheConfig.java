package eu.petrvich.construction.planner.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for caching project statistics.
 * Uses Spring's simple concurrent map cache implementation for in-memory caching.
 *
 * Can be enabled/disabled via configuration property: app.cache.enabled
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "app.cache.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class CacheConfig {

    public static final String PROJECT_STATISTICS_CACHE = "projectStatistics";

    private final AppProperties appProperties;

    @Bean
    public CacheManager cacheManager() {
        log.info("Configuring cache manager with cache: {} (enabled: {})",
                PROJECT_STATISTICS_CACHE,
                appProperties.getCache().isEnabled());
        return new ConcurrentMapCacheManager(PROJECT_STATISTICS_CACHE);
    }
}
