package io.chirper.configuration;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

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


    //================================================================================
    // Error Handling which does not trigger Resilience4j Fallbacks in some cases
    //================================================================================

    public static ResponseEntity<?> catchValidationAndNotFoundEx(
        @NotNull Supplier<ResponseEntity<?>> supplier, Logger logger
    ) {
        try {
            return supplier.get();
        } catch (ConstraintViolationException | EntityNotFoundException ex) {
            if (logger != null) logger.debug("catchValidationAndNotFound", ex);
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    public static CompletableFuture<ResponseEntity<?>> catchValidationAndNotFoundExAsync(
        @NotNull Supplier<ResponseEntity<?>> supplier, Logger logger
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (ConstraintViolationException | EntityNotFoundException ex) {
                if (logger != null) logger.debug("catchValidationAndNotFound", ex);
                return ResponseEntity.badRequest().body(ex.getMessage());
            }
        });
    }
}
