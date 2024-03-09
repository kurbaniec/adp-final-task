package io.chirper.controllers;

import io.chirper.services.ImageService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.UUID;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */
@Validated
@RestController
@RequestMapping("/image")
public class ImageController {
    private final ImageService imageService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/download/{file_id}")
    public void download(
        @PathVariable("file_id") @NotNull UUID fileId,
        HttpServletResponse response
    ) throws IOException {
        logger.debug("download({})", fileId);
        var stream = imageService.findImageDataById(fileId);
        StreamUtils.copy(stream, response.getOutputStream());
    }
}
