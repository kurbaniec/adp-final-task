package io.chirper.test;

import io.chirper.dtos.UserDTO;
import io.chirper.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.LinkedMultiValueMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class UserTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void beforeEach() { clear(); }

    @AfterEach
    public void afterEach() { clear(); }

    public void clear() {
        userRepository.deleteAll();
    }

    @Test
    void user_registration() {
        var requestUrl = "/user/register";
        var createUserDto = UserDTO.builder()
            .username("test")
            .password("testtest")
            .build();
        var body = new LinkedMultiValueMap<String, Object>();
        body.add("data", createUserDto);

        var userDto = restTemplate
            .postForObject(requestUrl, body, UserDTO.class);

        assertNotNull(userDto.getId());
        assertEquals("test", userDto.getUsername());
        assertNull(userDto.getPassword());
    }

    @Test
    void fail_user_registration_blank_username() {
        var requestUrl = "/user/register";
        var createUserDto = UserDTO.builder()
            .username("")
            .password("testtest")
            .build();
        var body = new LinkedMultiValueMap<String, Object>();
        body.add("data", createUserDto);

        var response = restTemplate
            .postForEntity(requestUrl, body, UserDTO.class);

        assertTrue(response.getStatusCode().is4xxClientError());
    }
}
