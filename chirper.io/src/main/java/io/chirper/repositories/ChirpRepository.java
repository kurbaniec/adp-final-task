package io.chirper.repositories;

import io.chirper.entities.Chirp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */
public interface ChirpRepository extends JpaRepository<Chirp, UUID> {
    @Query("SELECT c FROM Chirp c LEFT JOIN FETCH c.replies WHERE c.id = :chirpId")
    Optional<Chirp> findByIdWithReplies(UUID chirpId);
}
