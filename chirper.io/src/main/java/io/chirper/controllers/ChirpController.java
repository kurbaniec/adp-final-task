package io.chirper.controllers;

import io.chirper.common.PrincipalUtil;
import io.chirper.dtos.ChirpDTO;
import io.chirper.dtos.ReplyDTO;
import io.chirper.services.ChirpService;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.UUID;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */

@Validated
@RestController
@RequestMapping("/chirp")
public class ChirpController {
    private final ChirpService chirpService;
    private final ServiceMapper mapper;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ChirpController(ChirpService chirpService, ServiceMapper mapper) {
        this.chirpService = chirpService;
        this.mapper = mapper;
    }

    @PostMapping("/chirp")
    public ResponseEntity<ChirpDTO> chirp(
        @RequestPart(value = "data") @NotNull ChirpDTO createChirp,
        @RequestPart(value = "file", required = false) MultipartFile file,
        Principal principal
    ) {
        logger.info("chirp({})", createChirp);
        var userId = PrincipalUtil.getUserId(principal);
        var chirp = mapper.chirpToEntity(createChirp);
        chirp = chirpService.createChirp(chirp, file, userId);
        var chirpDto = mapper.chirpToDto(chirp);
        return ResponseEntity.ok(chirpDto);
    }

    @PostMapping("/reply")
    public ResponseEntity<?> reply(
        @RequestPart(value = "data") @NotNull ReplyDTO createReply,
        @RequestPart(value = "file", required = false) MultipartFile file,
        Principal principal
    ) {
        logger.info("reply({})", createReply);
        var chirpId = createReply.getChirpId();
        if (chirpId == null) {
            return new ResponseEntity<>("[chirpId] not provided", HttpStatus.BAD_REQUEST);
        }
        var userId = PrincipalUtil.getUserId(principal);
        var reply = mapper.replyToEntity(createReply);
        reply = chirpService.createReply(chirpId, reply, file, userId);
        var replyDto = mapper.replyToDTO(reply);
        return ResponseEntity.ok(replyDto);
    }

    @GetMapping("/chirp/{chirp_id}")
    public ResponseEntity<ChirpDTO> chirp(
        @PathVariable("chirp_id") UUID chirpId
    ) {
        logger.info("chirp({})", chirpId);
        var chirp = chirpService.fetchChirp(chirpId);
        var chirpDto = mapper.chirpToDto(chirp);
        return ResponseEntity.ok(chirpDto);
    }
}
