package io.chirper.dtos;

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
    private UUID id;
    private String text;
    private Long likes;
    private UUID imageId;
    private UserDTO author;
    private List<ReplyDTO> replies;
    private Instant createdOn;
}
