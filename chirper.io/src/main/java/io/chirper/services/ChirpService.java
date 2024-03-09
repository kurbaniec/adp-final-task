package io.chirper.services;

import io.chirper.entities.Chirp;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */
public interface ChirpService {
    Chirp createChirp(
        @Valid @NotNull Chirp createChirp,
        MultipartFile media,
        @NotNull UUID userId
    );
}
