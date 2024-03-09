package io.chirper.controllers;

import io.chirper.dtos.ChirpDTO;
import io.chirper.dtos.ReplyDTO;
import io.chirper.dtos.UserDTO;
import io.chirper.entities.Chirp;
import io.chirper.entities.Reply;
import io.chirper.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */
@Mapper(componentModel = "spring")
public interface ServiceMapper {

    User userToEntity(UserDTO dto);

    @Mapping(target = "password", ignore = true)
    UserDTO userToDto(User entity);

    Chirp chirpToEntity(ChirpDTO dto);

    ChirpDTO chirpToDto(Chirp entity);

    Reply replyToEntity(ReplyDTO dto);

    ReplyDTO replyToDTO(Reply entity);
}
