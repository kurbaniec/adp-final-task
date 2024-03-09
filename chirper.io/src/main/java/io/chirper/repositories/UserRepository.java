package io.chirper.repositories;

import io.chirper.entities.User;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(@NotNull @NotEmpty String username);
}
