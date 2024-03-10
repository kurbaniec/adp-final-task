package io.chirper.services;

import io.chirper.entities.Chirp;
import io.chirper.entities.Reply;
import io.chirper.entities.User;
import io.chirper.repositories.ChirpRepository;
import io.chirper.repositories.ReplyRepository;
import io.chirper.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.query.SortDirection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */

@Service
@Validated
public class DefaultChirpService implements ChirpService {
    private final ChirpRepository chirpRepository;
    private final ReplyRepository replyRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public DefaultChirpService(ChirpRepository chirpRepository, ReplyRepository replyRepository, UserRepository userRepository, ImageService imageService) {
        this.chirpRepository = chirpRepository;
        this.replyRepository = replyRepository;
        this.userRepository = userRepository;
        this.imageService = imageService;
    }

    @Override
    @Transactional
    public Chirp createChirp(
        @Valid @NotNull Chirp createChirp,
        MultipartFile media,
        @NotNull UUID userId
    ) {
        logger.debug("createChirp({}, {})", createChirp, userId);
        var user = userRepository
            .findById(userId)
            .orElseThrow(EntityNotFoundException::new);
        if (media != null) {
            var image = imageService.createImage(media);
            createChirp.setImageId(image.getId());
        }
        createChirp.setLikes(0L);
        createChirp.setAuthor(user);
        createChirp.setCreatedOn(Instant.now());
        return chirpRepository.save(createChirp);
    }

    @Override
    @Transactional
    public Reply createReply(
        @NotNull UUID chirpId,
        @Valid @NotNull Reply createReply,
        MultipartFile media,
        @NotNull UUID userId
    ) {
        logger.debug("createReply({}, {}, {})", chirpId, createReply, userId);
        var chirp = chirpRepository
            .findById(chirpId)
            .orElseThrow(EntityNotFoundException::new);
        var user = userRepository
            .findById(userId)
            .orElseThrow(EntityNotFoundException::new);
        if (media != null) {
            var image = imageService.createImage(media);
            createReply.setImageId(image.getId());
        }
        createReply.setChirp(chirp);
        createReply.setAuthor(user);
        createReply.setCreatedOn(Instant.now());
        return replyRepository.save(createReply);
    }

    @Override
    @Transactional(readOnly = true)
    public Chirp fetchChirp(@NotNull UUID chirpId) {
        logger.debug("fetchChirp({})", chirpId);
        return chirpRepository
            .findByIdWithReplies(chirpId)
            .orElseThrow(EntityNotFoundException::new);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Chirp> fetchFeed(
        @Max(50) @Min(1) int size,
        @Min(0) int page,
        boolean descending,
        @NotNull UUID userId
    ) {
        logger.debug("fetchFeed({}, {}, {})", size, page, descending);
        var sortedByCreatedOn = descending ?
            Sort.by("createdOn").descending() :
            Sort.by("createdOn").ascending();
        var pageRequest = PageRequest.of(page, size, sortedByCreatedOn);
        return chirpRepository
            .findAllByAuthorIdNotIn(List.of(userId), pageRequest)
            .toList();
    }


}
