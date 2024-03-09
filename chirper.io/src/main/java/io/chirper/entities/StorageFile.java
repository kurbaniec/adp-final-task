package io.chirper.entities;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.MediaType;

import java.io.InputStream;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class StorageFile {
    private String name;
    private long length;
    private MediaType mediaType;
    private InputStream stream;
}
