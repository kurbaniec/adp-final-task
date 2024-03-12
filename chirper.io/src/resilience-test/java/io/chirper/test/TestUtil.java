package io.chirper.test;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-12
 */
public class TestUtil {
    public static final String passwd = "testtest";

    public static <T> HttpEntity<T> authEntity(T body, String username) {
        HttpHeaders headers = new HttpHeaders();
        var auth = username + ":" + passwd;
        var encodedAuth = Base64.getEncoder().encodeToString(
            auth.getBytes(StandardCharsets.US_ASCII));
        var authHeader = "Basic " + encodedAuth;
        headers.add("Authorization", authHeader);
        return new HttpEntity<>(body, headers);
    }
}
