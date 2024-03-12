package io.chirper.controllers;

import io.chirper.common.PrincipalUtil;
import io.chirper.configuration.ResilienceConfig;
import io.chirper.dtos.ChirpDTO;
import io.chirper.dtos.ReplyDTO;
import io.chirper.services.ChirpService;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeoutException;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */

@Validated
@RestController
@RequestMapping("/chirp")
public class ChirpController {
    private final ChirpService chirpService;
    private final ServiceMapper mapper;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ChirpController(ChirpService chirpService, ServiceMapper mapper) {
        this.chirpService = chirpService;
        this.mapper = mapper;
    }


    @CircuitBreaker(name = ResilienceConfig.CIRCUIT_BREAKER_MUTATION, fallbackMethod = "circuitBreakerFallbackCompletion")
    @TimeLimiter(name = ResilienceConfig.TIME_LIMITER_MUTATION, fallbackMethod = "timeLimiterFallbackCompletion")
    @Bulkhead(name = ResilienceConfig.BULKHEAD_MUTATION, fallbackMethod = "bulkheadFallbackCompletion")
    @PostMapping("/chirp")
    public CompletionStage<ResponseEntity<ChirpDTO>> chirp(
        @RequestPart(value = "data") @NotNull ChirpDTO createChirp,
        @RequestPart(value = "file", required = false) MultipartFile file,
        Principal principal
    ) {
        logger.info("chirp({})", createChirp);
        var userId = PrincipalUtil.getUserId(principal);
        var chirp = mapper.chirpToEntity(createChirp);
        chirp = chirpService.createChirp(chirp, file, userId);
        var chirpDto = mapper.chirpToDto(chirp);
        var response = ResponseEntity.ok(chirpDto);
        return CompletableFuture.completedFuture(response);
    }

    @PostMapping("/reply")
    public ResponseEntity<?> reply(
        @RequestPart(value = "data") @NotNull ReplyDTO createReply,
        @RequestPart(value = "file", required = false) MultipartFile file,
        Principal principal
    ) {
        logger.info("reply({})", createReply);
        var chirpId = createReply.getChirpId();
        if (chirpId == null) {
            return new ResponseEntity<>("[chirpId] not provided", HttpStatus.BAD_REQUEST);
        }
        var userId = PrincipalUtil.getUserId(principal);
        var reply = mapper.replyToEntity(createReply);
        reply = chirpService.createReply(chirpId, reply, file, userId);
        var replyDto = mapper.replyToDTO(reply);
        return ResponseEntity.ok(replyDto);
    }

    @Retry(name = ResilienceConfig.RETRY, fallbackMethod = "retryFallback")
    @CircuitBreaker(name = ResilienceConfig.CIRCUIT_BREAKER, fallbackMethod = "circuitBreakerFallback")
    @RateLimiter(name = ResilienceConfig.RATE_LIMITER, fallbackMethod = "rateLimiterFallback")
    @GetMapping("/chirp/{chirp_id}")
    public ResponseEntity<ChirpDTO> chirp(
        @PathVariable("chirp_id") UUID chirpId
    ) {
        logger.info("chirp({})", chirpId);
        var chirp = chirpService.fetchChirp(chirpId);
        var chirpDto = mapper.chirpToDto(chirp);
        return ResponseEntity.ok(chirpDto);
    }

    @GetMapping("/feed")
    public ResponseEntity<List<ChirpDTO>> chirp(
        @RequestParam int size,
        @RequestParam int page,
        @RequestParam boolean descending,
        Principal principal
    ) {
        logger.info("feed({}, {}, {})", size, page, descending);
        var userId = PrincipalUtil.getUserId(principal);
        var chirps = chirpService.fetchFeed(size, page, descending, userId);
        var chirpDtos = chirps
            .stream()
            .map(mapper::chirpToDtoWithoutReplies)
            .toList();
        return ResponseEntity.ok(chirpDtos);
    }

    @PostMapping("/chirp/like/{chirp_id}")
    public ResponseEntity<Void> likeChirp(
        @PathVariable("chirp_id") UUID chirpId,
        Principal principal
    ) {
        logger.info("likeChirp({})", chirpId);
        var userId = PrincipalUtil.getUserId(principal);
        chirpService.likeChirp(chirpId, userId);
        return ResponseEntity.ok().build();
    }

    //================================================================================
    // Resilience4j Fallbacks
    //================================================================================

    @SuppressWarnings("unused")
    public ResponseEntity<String> retryFallback(Exception ex) {
        logger.warn("retryFallback", ex);
        return new ResponseEntity<>("all retries have exhausted", HttpStatus.SERVICE_UNAVAILABLE);
    }

    @SuppressWarnings("unused")
    public ResponseEntity<String> circuitBreakerFallback(CallNotPermittedException ex) {
        logger.warn("circuitBreakerFallback {}", ex.getMessage());
        return new ResponseEntity<>("service is unavailable", HttpStatus.SERVICE_UNAVAILABLE);
    }

    @SuppressWarnings("unused")
    public ResponseEntity<String> rateLimiterFallback(RequestNotPermitted ex) {
        logger.warn("rateLimiterFallback", ex);
        return new ResponseEntity<>("too many requests", HttpStatus.TOO_MANY_REQUESTS);
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

}
