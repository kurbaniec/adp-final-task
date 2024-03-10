package io.chirper.services;

import io.chirper.entities.Chirp;
import io.chirper.entities.Reply;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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

    Reply createReply(
        @NotNull UUID chirpId,
        @Valid @NotNull Reply createReply,
        MultipartFile media,
        @NotNull UUID userId
    );

    Chirp fetchChirp(@NotNull UUID chirpId);

    List<Chirp> fetchFeed(
        @Max(50) @Min(1) int size,
        @Min(0) int page,
        boolean descending,
        @NotNull UUID userId
    );

    void likeChirp(
        @NotNull UUID chirpId,
        @NotNull UUID userId
    );
}
