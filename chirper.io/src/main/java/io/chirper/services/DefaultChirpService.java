package io.chirper.services;

import io.chirper.entities.Chirp;
import io.chirper.entities.Like;
import io.chirper.entities.Reply;
import io.chirper.entities.User;
import io.chirper.repositories.ChirpRepository;
import io.chirper.repositories.LikeRepository;
import io.chirper.repositories.ReplyRepository;
import io.chirper.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
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
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */

@Service
@Validated
public class DefaultChirpService implements ChirpService {
    private final ChirpRepository chirpRepository;
    private final ReplyRepository replyRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public DefaultChirpService(ChirpRepository chirpRepository, ReplyRepository replyRepository, LikeRepository likeRepository, UserRepository userRepository, ImageService imageService) {
        this.chirpRepository = chirpRepository;
        this.replyRepository = replyRepository;
        this.likeRepository = likeRepository;
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
        createReply.setLikes(0L);
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

    @Override
    public List<Chirp> fetchOwn(int size, int page, boolean descending, UUID userId) {
        logger.debug("fetchOwn({}, {}, {})", size, page, descending);
        var sortedByCreatedOn = descending ?
            Sort.by("createdOn").descending() :
            Sort.by("createdOn").ascending();
        var pageRequest = PageRequest.of(page, size, sortedByCreatedOn);
        return chirpRepository
            .findAllByAuthorId(userId, pageRequest)
            .toList();
    }

    @Override
    @Transactional
    public void likeChirp(
        @NotNull UUID chirpId,
        @NotNull UUID userId
    ) {
        logger.debug("likeChirp({}, {})", chirpId, userId);
        var chirp = chirpRepository
            .findById(chirpId)
            .orElseThrow(EntityNotFoundException::new);
        if (chirp.getAuthor().getId().equals(userId)) {
            throw new ConstraintViolationException("User cannot like own chirp", null);
        }
        onLike(chirpId, userId, () -> {
            // Remove
            var newLikes = chirp.getLikes() - 1;
            chirp.setLikes(newLikes);
        }, () -> {
            // Add
            var newLikes = chirp.getLikes() + 1;
            chirp.setLikes(newLikes);
        });
    }

    @Override
    @Transactional
    public void likeReply(UUID replyId, UUID userId) {
        logger.debug("likeReply({}, {})", replyId, userId);
        var reply = replyRepository
            .findById(replyId)
            .orElseThrow(EntityNotFoundException::new);
        if (reply.getAuthor().getId().equals(userId)) {
            throw new ConstraintViolationException("User cannot like own reply", null);
        }
        onLike(replyId, userId, () -> {
            // Remove
            var newLikes = reply.getLikes() - 1;
            reply.setLikes(newLikes);
        }, () -> {
            // Add
            var newLikes = reply.getLikes() + 1;
            reply.setLikes(newLikes);
        });
    }

    private void onLike(
        UUID resourceId, UUID userId,
        Runnable isPresent,
        Runnable notPresent
    ) {
        var likeMarker = likeRepository
            .findByResourceIdAndUserId(resourceId, userId);
        if (likeMarker.isPresent()) {
            // Remove like
            likeRepository.delete(likeMarker.get());
            isPresent.run();
        } else {
            // Add like
            var like = new Like();
            like.setResourceId(resourceId);
            like.setUserId(userId);
            likeRepository.save(like);
            notPresent.run();
        }
    }

}
