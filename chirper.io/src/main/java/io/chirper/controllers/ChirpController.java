package io.chirper.controllers;

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
import org.springframework.web.bind.annotation.RestController;

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
        @NotNull ChirpDTO createChirp,
        Principal principal
    ) {
        logger.info(principal.getName());


        return ResponseEntity.ok(
            ChirpDTO.builder().build()
        );
    }
}
