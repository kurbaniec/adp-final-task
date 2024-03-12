package io.chirper.test;

import io.chirper.entities.StorageFile;
import io.chirper.entities.User;
import io.chirper.repositories.UserRepository;
import io.chirper.services.ImageService;
import io.chirper.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
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
public class CacheTest {

    @MockBean
    private ImageService imageService;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void users() {
        userRepository.deleteAll();
        var user = new User();
        user.setUsername("test");
        user.setPassword("testtest");
        userService.createUser(user, null);
    }

    @Test
    void download_image() {
        var imageId = UUID.randomUUID();
        var text = "🐶";
        var bytes = text.getBytes(StandardCharsets.UTF_16);
        var stream = new ByteArrayInputStream(bytes);
        var storageFile = new StorageFile("foo.png", bytes.length, MediaType.IMAGE_PNG, stream);
        when(imageService.findImageDataById(eq(imageId)))
            .thenReturn(storageFile);

        for (var i = 0; i < 5; ++i) {
            var imageResponse = ImageUtil.donwloadImage(
                imageId, "test", "testtest", restTemplate
            );
            assertTrue(imageResponse.getStatusCode().is2xxSuccessful());
            var body = imageResponse.getBody();
            assertNotNull(body);
            assertEquals(text, new String(body, StandardCharsets.UTF_16));
        }

        verify(imageService, times(1)).findImageDataById(eq(imageId));
    }

    @Test
    void download_images() {
        var imageIdDog = UUID.randomUUID();
        var textDog = "🐶";
        var bytesDog = textDog.getBytes(StandardCharsets.UTF_16);
        var streamDog = new ByteArrayInputStream(bytesDog);
        var storageFileDog = new StorageFile("foo.png", bytesDog.length, MediaType.TEXT_PLAIN, streamDog);
        when(imageService.findImageDataById(eq(imageIdDog)))
            .thenReturn(storageFileDog);

        var imageIdCat = UUID.randomUUID();
        var textCat = "😺";
        var bytesCat = textCat.getBytes(StandardCharsets.UTF_16);
        var streamCat = new ByteArrayInputStream(bytesCat);
        var storageFileCat = new StorageFile("foo.png", bytesCat.length, MediaType.TEXT_PLAIN, streamCat);
        when(imageService.findImageDataById(eq(imageIdCat)))
            .thenReturn(storageFileCat);

        for (var i = 0; i < 5; ++i) {
            var imageResponse = ImageUtil.donwloadImage(
                imageIdDog, "test", "testtest", restTemplate
            );
            assertTrue(imageResponse.getStatusCode().is2xxSuccessful());
            var body = imageResponse.getBody();
            assertNotNull(body);
            assertEquals(textDog, new String(body, StandardCharsets.UTF_16));
        }
        for (var i = 0; i < 5; ++i) {
            var imageResponse = ImageUtil.donwloadImage(
                imageIdCat, "test", "testtest", restTemplate
            );
            assertTrue(imageResponse.getStatusCode().is2xxSuccessful());
            var body = imageResponse.getBody();
            assertNotNull(body);
            assertEquals(textCat, new String(body, StandardCharsets.UTF_16));
        }

        verify(imageService, times(1)).findImageDataById(eq(imageIdDog));
        verify(imageService, times(1)).findImageDataById(eq(imageIdCat));
    }
}
