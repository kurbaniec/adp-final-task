package io.chirper.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class UserDTO {
    private UUID id;
    private String username;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String password;
}
