package io.chirper.repositories;

import io.chirper.entities.StorageFile;

import java.util.UUID;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */
public interface StorageRepository {
    void save(UUID fileId, String name, byte[] file);
    StorageFile fetch(UUID fileId);
}
