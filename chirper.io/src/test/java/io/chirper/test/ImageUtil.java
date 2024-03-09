package io.chirper.test;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */
public class ImageUtil {


    public static HttpEntity<byte[]> mockPngImage(String name, String body) {
        // Based on https://stackoverflow.com/a/52005690/12347616
        var headerMap = new LinkedMultiValueMap<String, String >();
        headerMap.add("Content-disposition", "form-data; name=file; filename=" + name);
        headerMap.add("Content-type", "image/png");
        return new HttpEntity<>(body.getBytes(), headerMap);
    }

    public static ResponseEntity<byte[]> donwloadImage(
        UUID fileId, String username, String password,
        TestRestTemplate restTemplate
    ) {
        var imageRequestUrl = "/image/download/" + fileId;
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_OCTET_STREAM));
        var auth = username + ":" + password;
        var encodedAuth = Base64.getEncoder().encodeToString(
            auth.getBytes(StandardCharsets.US_ASCII));
        var authHeader = "Basic " + encodedAuth;
        headers.add("Authorization", authHeader);
        var entity = new HttpEntity<String>(headers);
        return restTemplate
            .exchange(imageRequestUrl, HttpMethod.GET,entity, byte[].class);
    }

}
