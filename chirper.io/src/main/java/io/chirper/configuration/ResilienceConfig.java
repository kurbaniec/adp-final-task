package io.chirper.configuration;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-10
 */
public class ResilienceConfig {
    // Mutation
    public static final String CIRCUIT_BREAKER_MUTATION = "circuitBreakerMutationApi";
    public static final String TIME_LIMITER_MUTATION = "timeLimiterMutationApi";
    public static final String BULKHEAD_MUTATION = "bulkheadMutationApi";

    // Query
    public static final String RETRY = "retryApi";
    public static final String CIRCUIT_BREAKER = "circuitBreakerApi";
    public static final String RATE_LIMITER = "rateLimiterApi";
}
