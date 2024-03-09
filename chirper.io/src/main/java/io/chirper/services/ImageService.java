package io.chirper.services;

import io.chirper.entities.Image;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */
public interface ImageService {
    Image createImage(MultipartFile file);

    InputStream findImageDataById(UUID fileId);
}
