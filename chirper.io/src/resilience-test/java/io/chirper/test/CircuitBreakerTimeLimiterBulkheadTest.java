package io.chirper.test;

import io.chirper.configuration.ResilienceConfig;
import io.chirper.controllers.ServiceMapper;
import io.chirper.dtos.ChirpDTO;
import io.chirper.entities.Chirp;
import io.chirper.entities.User;
import io.chirper.repositories.UserRepository;
import io.chirper.services.ChirpService;
import io.chirper.services.UserService;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static io.chirper.test.TestUtil.authEntity;
import static io.chirper.test.TestUtil.passwd;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
public class CircuitBreakerTimeLimiterBulkheadTest {

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
            .circuitBreaker(ResilienceConfig.CIRCUIT_BREAKER_MUTATION);
        circuitBreaker.reset();
    }

    @BeforeEach
    public void users() {
        userRepository.deleteAll();
        var user = new User();
        user.setUsername("test");
        user.setPassword(passwd);
        userService.createUser(user, null);
    }

    @Test
    @Order(1)
    void chirp_Bulkhead() {
        var chirpId = UUID.randomUUID();
        var text = "Hello World!";
        var user = new User();
        user.setUsername("test");
        var chirp = new Chirp();
        chirp.setId(chirpId);
        chirp.setText(text);
        chirp.setAuthor(user);

        when(chirpService.createChirp(any(), any(), any()))
            .thenAnswer(invocation -> {
                Thread.sleep(250);
                return chirp;
            });

        var responseStatusCount = new ConcurrentHashMap<Integer, Integer>();
        IntStream.rangeClosed(1, 5)
            .parallel()
            .forEach(i -> {
                var response = addChirp("test", text);
                var statusCode = response.getStatusCode().value();
                responseStatusCount.merge(statusCode, 1, Integer::sum);
            });

        assertEquals(2, responseStatusCount.keySet().size());
        assertTrue(responseStatusCount.containsKey(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED.value()));
        assertTrue(responseStatusCount.containsKey(HttpStatus.OK.value()));
        verify(chirpService, times(3)).createChirp(any(), any(), any());
    }

    @Test
    void chirp_TimeLimiter() {
        when(chirpService.createChirp(any(), any(), any()))
            .thenAnswer(invocation -> {
                Thread.sleep(5000);
                return new Chirp();
            });

        var response = addChirp("test");
        assertEquals(HttpStatus.REQUEST_TIMEOUT, response.getStatusCode());
    }

    private ResponseEntity<String> addChirp(String username) {
        return addChirp(username, "Hello World!");
    }

    private ResponseEntity<String> addChirp(String username, String chirp) {
        var requestUrl = "/chirp/chirp";
        var createChirpDto = ChirpDTO.builder()
            .text(chirp)
            .build();
        var body = new LinkedMultiValueMap<String, Object>();
        body.add("data", createChirpDto);
        var request = authEntity(body, username);

        return restTemplate
            .postForEntity(requestUrl, request, String.class);
    }
}
