package io.chirper.repositories;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */
@Component
public class FileSystemRepository implements StorageRepository {
    private final String RESOURCES_DIR;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public FileSystemRepository(ResourceLoader resourceLoader) throws IOException {
        var resource = resourceLoader.getResource("classpath:/");
        RESOURCES_DIR = resource.getFile().getAbsolutePath();
    }

    @Override
    public void save(UUID fileId, String name, byte[] file) {
        try {
            var path = Paths.get(RESOURCES_DIR + fileId + "/" + name);
            Files.createDirectories(path.getParent());
            Files.write(path, file);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new PersistenceException();
        }
    }

    @Override
    public InputStream fetch(UUID fileId) {
        try {
            var path = Paths.get(RESOURCES_DIR + fileId);
            var folder = path.toFile();
            var file = Objects.requireNonNull(folder.listFiles())[0];
            var resource = new FileSystemResource(file);
            return resource.getInputStream();
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new EntityNotFoundException();
        }
    }
}
