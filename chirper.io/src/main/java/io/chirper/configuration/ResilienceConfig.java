package io.chirper.configuration;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-10
 */
public class ResilienceConfig {
    public static final String RETRY = "retryApi";
    public static final String CIRCUIT_BREAKER = "circuitBreakerApi";
    public static final String RATE_LIMITER = "rateLimiterApi";
}
