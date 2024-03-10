package io.chirper.repositories;

import io.chirper.entities.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-10
 */
public interface LikeRepository extends JpaRepository<Like, UUID> {

    Optional<Like> findByResourceIdAndUserId(UUID resourceId, UUID userId);
}
