package io.chirper.configuration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-10
 */

@Configuration
@EnableCaching
public class CacheConfiguration {

    @Bean
    public ConcurrentMapCacheManager cacheManager() {
        return new ConcurrentMapCacheManager("images");
    }
}