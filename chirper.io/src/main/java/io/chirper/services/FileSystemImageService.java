package io.chirper.services;

import io.chirper.entities.Image;
import io.chirper.repositories.ImageRepository;
import io.chirper.repositories.StorageRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */
@Service
@Validated
public class FileSystemImageService implements ImageService {
    private final ImageRepository imageRepository;
    private final StorageRepository storageRepository;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public FileSystemImageService(ImageRepository imageRepository, StorageRepository storageRepository) {
        this.imageRepository = imageRepository;
        this.storageRepository = storageRepository;
    }

    @Override
    public Image createImage(MultipartFile file) {
        logger.debug("createImage({})", file);
        try {
            var name = file.getOriginalFilename();
            var data = file.getBytes();
            var image = new Image();
            image.setFilename(name);
            image = imageRepository.save(image);
            storageRepository.save(image.getId(), name, data);
            return image;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new PersistenceException();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public InputStream findImageDataById(UUID fileId) {
        logger.debug("findImageDataById({})", fileId);
        if (!imageRepository.existsById(fileId)) {
            throw new EntityNotFoundException();
        }
        return storageRepository.fetch(fileId);
    }

}
