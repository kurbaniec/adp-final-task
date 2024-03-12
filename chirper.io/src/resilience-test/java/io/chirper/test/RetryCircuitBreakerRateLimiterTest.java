package io.chirper.test;

import io.chirper.configuration.ResilienceConfig;
import io.chirper.controllers.ServiceMapper;
import io.chirper.dtos.ChirpDTO;
import io.chirper.dtos.UserDTO;
import io.chirper.entities.Chirp;
import io.chirper.entities.User;
import io.chirper.repositories.UserRepository;
import io.chirper.services.ChirpService;
import io.chirper.services.UserService;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-10
 */

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class RetryCircuitBreakerRateLimiterTest {

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private ChirpService chirpService;

    @MockBean
    private ServiceMapper mapper;

    @BeforeEach
    public void resetCircuitBreakerRetry() {
        var circuitBreaker = circuitBreakerRegistry
            .circuitBreaker(ResilienceConfig.CIRCUIT_BREAKER);
        circuitBreaker.reset();
    }

    @BeforeEach
    public void users() {
        userRepository.deleteAll();
        var user = new User();
        user.setUsername("test");
        user.setPassword("testtest");
        userService.createUser(user, null);
    }

    private final String passwd = "testtest";

    @Test
    void chirp_Retry_Success() {
        var chirpId = UUID.randomUUID();

        var chirp = new Chirp();
        chirp.setId(chirpId);
        when(chirpService.fetchChirp(eq(chirpId)))
            .thenThrow(new RuntimeException("Something went wrong!"))
            .thenThrow(new RuntimeException("Something went wrong!"))
            .thenReturn(chirp);

        var chirpDto = ChirpDTO.builder()
            .id(chirpId)
            .build();
        when(mapper.chirpToDto(any(Chirp.class)))
            .thenReturn(chirpDto);

        var response = fetchChirp("test", chirpId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(chirpService, times(3)).fetchChirp(eq(chirpId));
    }

    @Test
    void chirp_Retry_Failure() {
        var chirpId = UUID.randomUUID();

        var chirp = new Chirp();
        chirp.setId(chirpId);
        when(chirpService.fetchChirp(eq(chirpId)))
            .thenThrow(new RuntimeException("Something went wrong!"));

        var chirpDto = ChirpDTO.builder()
            .id(chirpId)
            .build();
        when(mapper.chirpToDto(any(Chirp.class)))
            .thenReturn(chirpDto);

        var response = fetchChirp("test", chirpId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertEquals("all retries have exhausted", response.getBody());
        verify(chirpService, times(3)).fetchChirp(eq(chirpId));
    }

    @Test
    void chirp_CircuitBreaker() {
        var chirpId = UUID.randomUUID();
        when(chirpService.fetchChirp(eq(chirpId)))
            .thenThrow(new RuntimeException("Something went wrong!"));

        IntStream.rangeClosed(1, 5)
            .forEach(i -> {
                // Retry responses
                var response = fetchChirp("test", chirpId);
                assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
                assertEquals("all retries have exhausted", response.getBody());
            });

        IntStream.rangeClosed(1, 5)
            .forEach(i -> {
                // Circuit breaker responses
                var response = fetchChirp("test", chirpId);
                assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
                assertEquals("service is unavailable", response.getBody());
            });

        verify(chirpService, times(5*3)).fetchChirp(eq(chirpId));
    }

    @Test
    void chirp_RateLimiter() {
        var chirpId = UUID.randomUUID();

        var chirp = new Chirp();
        chirp.setId(chirpId);
        when(chirpService.fetchChirp(eq(chirpId)))
            .thenReturn(chirp);

        var chirpDto = ChirpDTO.builder()
            .id(chirpId)
            .build();
        when(mapper.chirpToDto(any(Chirp.class)))
            .thenReturn(chirpDto);

        Map<Integer, Integer> responseStatusCount = new ConcurrentHashMap<>();
        IntStream.rangeClosed(1, 150)
            .parallel()
            .forEach(i -> {
                var response = fetchChirp("test", chirpId);
                var statusCode = response.getStatusCode().value();
                responseStatusCount.put(statusCode, responseStatusCount.getOrDefault(statusCode, 0) + 1);
            });

        assertEquals(2, responseStatusCount.keySet().size());
        assertTrue(responseStatusCount.containsKey(HttpStatus.TOO_MANY_REQUESTS.value()));
        assertTrue(responseStatusCount.containsKey(HttpStatus.OK.value()));
        verify(chirpService, atMost(150)).fetchChirp(eq(chirpId));
    }

    private ChirpDTO addChirp(String username) {
        return addChirp(username, "Hello World!");
    }

    private ChirpDTO addChirp(String username, String chirp) {
        var requestUrl = "/chirp/chirp";
        var createChirpDto = ChirpDTO.builder()
            .text(chirp)
            .build();
        var body = new LinkedMultiValueMap<String, Object>();
        body.add("data", createChirpDto);
        var request = authEntity(body, username);

        return restTemplate
            .postForObject(requestUrl, request, ChirpDTO.class);
    }

    private void addUser(String username) {
        var requestUrl = "/user/register";
        var createUserDto = UserDTO.builder()
            .username(username)
            .password(passwd)
            .build();
        var body = new LinkedMultiValueMap<String, Object>();
        body.add("data", createUserDto);

        var userDto = restTemplate
            .postForObject(requestUrl, body, UserDTO.class);

        assertNotNull(userDto.getId());
    }

    private ResponseEntity<String> fetchChirp(
        String username,
        UUID chirpId
    ) {
        var requestUrl = "/chirp/chirp/" + chirpId;
        var request = authEntity(null, username);
        return restTemplate.exchange(requestUrl, HttpMethod.GET, request, String.class);
    }

    private <T> HttpEntity<T> authEntity(T body, String username) {
        HttpHeaders headers = new HttpHeaders();
        var auth = username + ":" + passwd;
        var encodedAuth = Base64.getEncoder().encodeToString(
            auth.getBytes(StandardCharsets.US_ASCII));
        var authHeader = "Basic " + encodedAuth;
        headers.add("Authorization", authHeader);
        return new HttpEntity<>(body, headers);
    }


}
