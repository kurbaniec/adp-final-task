package io.chirper.repositories;

import java.io.InputStream;
import java.util.UUID;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */
public interface StorageRepository {
    void save(UUID fileId, String name, byte[] file);
    InputStream fetch(UUID fileId);

}
