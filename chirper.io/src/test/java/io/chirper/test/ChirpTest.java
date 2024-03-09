package io.chirper.test;

import io.chirper.dtos.ChirpDTO;
import io.chirper.dtos.UserDTO;
import io.chirper.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.LinkedMultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ChirpTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    private final String passwd = "testtest";
    private final String userDoe = "Doe";
    private final String userSmith = "Smith";
    private final String userMuster = "Muster";

    @BeforeEach
    public void beforeEach() {
        clear();
        addUser(userDoe);
        addUser(userSmith);
        addUser(userMuster);
    }

    @AfterEach
    public void afterEach() { clear(); }

    public void clear() {
        userRepository.deleteAll();
    }

    @Test
    void chirp() {
        var requestUrl = "/chirp/chirp";
        var createChirpDto = ChirpDTO.builder()
            .text("Hello World!")
            .build();
        var request = authEntity(createChirpDto, userDoe);

        var chirpDto = restTemplate
            .postForObject(requestUrl, request, ChirpDTO.class);

        assertNotNull(chirpDto.getId());
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
