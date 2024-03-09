package io.chirper.services;

import io.chirper.entities.Image;
import io.chirper.entities.StorageFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */
public interface ImageService {
    Image createImage(MultipartFile file);

    StorageFile findImageDataById(UUID fileId);
}
