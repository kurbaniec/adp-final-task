package io.chirper.controllers;

import io.chirper.services.ImageService;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Cacheable("images")
    @GetMapping("/download/{file_id}")
    public ResponseEntity<byte[]> download(@PathVariable("file_id") @NotNull UUID fileId) throws IOException {
        logger.info("download({})", fileId);
        var storageFile = imageService.findImageDataById(fileId);

        var responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(storageFile.getMediaType());
        responseHeaders.setContentLength(storageFile.getLength());
        responseHeaders.setContentDispositionFormData("attachment", storageFile.getName());

        var stream = storageFile.getStream();
        var data = stream.readAllBytes();
        return new ResponseEntity<>(data, responseHeaders, HttpStatus.OK);
    }
}
