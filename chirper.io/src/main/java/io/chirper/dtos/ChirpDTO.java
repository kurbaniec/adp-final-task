package io.chirper.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */
@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class ChirpDTO {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;
    private String text;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long likes;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private UUID imageId;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private UserDTO author;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private List<ReplyDTO> replies;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Instant createdOn;
}
