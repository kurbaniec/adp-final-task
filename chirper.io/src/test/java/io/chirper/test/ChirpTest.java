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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */

@SuppressWarnings("VulnerableCodeUsages")
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

    @Test
    void query_chirp_with_replies() {
        var createChirpDto = addChirp(userDoe);
        var replySmith = addReply(userSmith, createChirpDto, "Hey!");
        var replyMuster = addReply(userMuster, createChirpDto, "Moi!");

        var requestUrl = "/chirp/chirp/" + createChirpDto.getId();
        var request = authEntity(null, userDoe);
        var chirpDto = restTemplate
            .exchange(requestUrl, HttpMethod.GET, request, ChirpDTO.class)
                .getBody();

        assertNotNull(chirpDto);
        assertEquals(createChirpDto.getText(), chirpDto.getText());
        var replies = chirpDto.getReplies();
        assertNotNull(replies);
        assertEquals(2, replies.size());
        var checkReplies = List.of(replySmith, replyMuster);
        for (var i = 0; i < replies.size(); ++i) {
            var reply = replies.get(i);
            var check = checkReplies.get(i);
            assertEquals(check.getId(), reply.getId());
            assertEquals(check.getText(), reply.getText());
            assertEquals(
                check.getAuthor().getUsername(),
                reply.getAuthor().getUsername()
            );
            assertEquals(
                check.getCreatedOn().truncatedTo(ChronoUnit.MILLIS),
                reply.getCreatedOn().truncatedTo(ChronoUnit.MILLIS)
            );
        }
    }

    @Test
    void query_feed() {
        for (var i = 0; i < 5; ++i) {
            var createChirpDto = addChirp(userMuster, "Test #" + i);
            addReply(userSmith, createChirpDto, "Already #" + i + " ?!?");
        }

        var pageSize = 2;
        var expected = List.of(2, 2, 1, 0);
        for (var page = 0; page < expected.size(); ++page) {
            var size = expected.get(page);
            var chirpDtos = fetchFeed(
                userDoe, page, pageSize,  true
            );
            assertNotNull(chirpDtos);
            assertEquals(size, chirpDtos.size());
        }
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

    private ReplyDTO addReply(String username, ChirpDTO chirp) {
        return addReply(username, chirp, "Programmer?");
    }

    private ReplyDTO addReply(String username, ChirpDTO chirp, String reply) {
        var requestUrl = "/chirp/reply";
        var text = "Programmer?";
        var createReplyDto = ReplyDTO.builder()
            .chirpId(chirp.getId())
            .text(text)
            .build();
        var body = new LinkedMultiValueMap<String, Object>();
        body.add("data", createReplyDto);
        var request = authEntity(body, username);

        return restTemplate
            .postForObject(requestUrl, request, ReplyDTO.class);
    }

    private List<ChirpDTO> fetchFeed(
        String username,
        int page, int pageSize, boolean descending
    ) {
        var requestUrl = "/chirp/feed";
        requestUrl = UriComponentsBuilder.fromPath(requestUrl)
            .queryParam("page", page)
            .queryParam("size", pageSize)
            .queryParam("descending", descending)
            .encode()
            .toUriString();
        var request = authEntity(null, username);
        return restTemplate
            .exchange(requestUrl, HttpMethod.GET, request, new ParameterizedTypeReference<List<ChirpDTO>>() {})
            .getBody();
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
