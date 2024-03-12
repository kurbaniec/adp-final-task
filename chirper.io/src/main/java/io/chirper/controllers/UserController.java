package io.chirper.controllers;

import io.chirper.configuration.ResilienceConfig;
import io.chirper.dtos.UserDTO;
import io.chirper.services.UserService;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeoutException;

import static io.chirper.configuration.ResilienceConfig.catchValidationAndNotFoundExAsync;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */

@Validated
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final ServiceMapper mapper;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public UserController(UserService userService, ServiceMapper mapper) {
        this.userService = userService;
        this.mapper = mapper;
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @CircuitBreaker(name = ResilienceConfig.CIRCUIT_BREAKER_MUTATION, fallbackMethod = "circuitBreakerFallbackCompletion")
    @TimeLimiter(name = ResilienceConfig.TIME_LIMITER_MUTATION, fallbackMethod = "timeLimiterFallbackCompletion")
    @Bulkhead(name = ResilienceConfig.BULKHEAD_MUTATION, fallbackMethod = "bulkheadFallbackCompletion")
    @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = UserDTO.class))})
    public CompletionStage<ResponseEntity<?>> register(
        @Schema(example = "{\"username\": \"test\", \"password\": \"testtest\"}")
        @RequestPart(value = "data") @NotNull UserDTO createUser,
        @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        logger.info("register({}, {})", createUser, file);
        return catchValidationAndNotFoundExAsync(() -> {
            var user = mapper.userToEntity(createUser);
            user = userService.createUser(user, file);
            var userDto = mapper.userToDto(user);
            return ResponseEntity.ok(userDto);
        }, logger);
    }

    //================================================================================
    // region Resilience4j Fallbacks
    //================================================================================

    @SuppressWarnings("unused")
    public ResponseEntity<String> circuitBreakerFallback(CallNotPermittedException ex) {
        logger.warn("circuitBreakerFallback {}", ex.getMessage());
        return new ResponseEntity<>("service is unavailable", HttpStatus.SERVICE_UNAVAILABLE);
    }

    @SuppressWarnings("unused")
    public CompletionStage<ResponseEntity<String>> circuitBreakerFallbackCompletion(CallNotPermittedException ex) {
        return CompletableFuture.completedFuture(circuitBreakerFallback(ex));
    }

    @SuppressWarnings("unused")
    public CompletionStage<ResponseEntity<String>> timeLimiterFallbackCompletion(TimeoutException ex) {
        logger.warn("timeLimiterFallback {}", ex.getMessage());
        return CompletableFuture.completedFuture(new ResponseEntity<>("request timed out", HttpStatus.REQUEST_TIMEOUT));
    }

    @SuppressWarnings("unused")
    public CompletionStage<ResponseEntity<String>> bulkheadFallbackCompletion(BulkheadFullException ex) {
        logger.warn("bulkheadFallback {}", ex.getMessage());
        return CompletableFuture.completedFuture(new ResponseEntity<>("limit exceeded", HttpStatus.BANDWIDTH_LIMIT_EXCEEDED));
    }

    // endregion
}
