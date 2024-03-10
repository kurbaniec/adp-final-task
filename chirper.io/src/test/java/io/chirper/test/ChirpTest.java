package io.chirper.test;

import io.chirper.dtos.ChirpDTO;
import io.chirper.dtos.ReplyDTO;
import io.chirper.dtos.UserDTO;
import io.chirper.repositories.ChirpRepository;
import io.chirper.repositories.ReplyRepository;
import io.chirper.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.LinkedMultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private ChirpRepository chirpRepository;
    @Autowired
    private ReplyRepository replyRepository;

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
    public void afterEach() {
        clear();
    }

    public void clear() {
        replyRepository.deleteAll();
        chirpRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void chirp() {
        var requestUrl = "/chirp/chirp";
        var text = "Hello World!";
        var createChirpDto = ChirpDTO.builder()
            .text(text)
            .build();
        var body = new LinkedMultiValueMap<String, Object>();
        body.add("data", createChirpDto);
        var request = authEntity(body, userDoe);

        var chirpDto = restTemplate
            .postForObject(requestUrl, request, ChirpDTO.class);

        assertNotNull(chirpDto.getId());
        assertEquals(text, chirpDto.getText());
        assertEquals(0, chirpDto.getLikes());
        assertEquals(userDoe, chirpDto.getAuthor().getUsername());
    }

    @Test
    void reply() {
        var chirp = addChirp(userDoe);
        var requestUrl = "/chirp/reply";
        var text = "Programmer?";
        var createReplyDto = ReplyDTO.builder()
            .chirpId(chirp.getId())
            .text(text)
            .build();
        var body = new LinkedMultiValueMap<String, Object>();
        body.add("data", createReplyDto);
        var request = authEntity(body, userSmith);

        var replyDto = restTemplate
            .postForObject(requestUrl, request, ReplyDTO.class);
        assertNotNull(replyDto.getId());
        assertEquals(text, replyDto.getText());
        assertEquals(userSmith, replyDto.getAuthor().getUsername());
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
