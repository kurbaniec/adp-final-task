package io.chirper.controllers;

import io.chirper.services.ImageService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<InputStreamResource> download(@PathVariable("file_id") @NotNull UUID fileId) {
        logger.debug("download({})", fileId);
        var storageFile = imageService.findImageDataById(fileId);

        var responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(storageFile.getMediaType());
        responseHeaders.setContentLength(storageFile.getLength());
        responseHeaders.setContentDispositionFormData("attachment", storageFile.getName());
        var inputStreamResource = new InputStreamResource(storageFile.getStream());
        return new ResponseEntity<>(inputStreamResource, responseHeaders, HttpStatus.OK);
    }
}
