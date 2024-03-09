package io.chirper.controllers;

import io.chirper.common.PrincipalUtil;
import io.chirper.dtos.ChirpDTO;
import io.chirper.services.ChirpService;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

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
        logger.info(principal.getName());
        var userId = PrincipalUtil.getUserId(principal);
        var chirp = mapper.chirpToEntity(createChirp);
        chirp = chirpService.createChirp(chirp, file, userId);
        var chirpDto = mapper.chirpToDto(chirp);
        return ResponseEntity.ok(chirpDto);
    }
}
