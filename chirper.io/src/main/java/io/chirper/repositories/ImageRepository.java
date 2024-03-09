package io.chirper.repositories;

import io.chirper.entities.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */
public interface ImageRepository extends JpaRepository<Image, UUID> {
}
