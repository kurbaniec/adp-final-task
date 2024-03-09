package io.chirper.controllers;

import io.chirper.dtos.UserDTO;
import io.chirper.services.UserService;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */

@Validated
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final ServiceMapper mapper;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public UserController(UserService userService, ServiceMapper mapper) {
        this.userService = userService;
        this.mapper = mapper;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(
        @RequestPart(value = "data") @NotNull UserDTO createUser,
        @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        logger.debug("register({}, {})", createUser, file);
        var user = mapper.userToEntity(createUser);
        user = userService.createUser(user);
        var userDto = mapper.userToDto(user);
        return ResponseEntity.ok(userDto);
    }
}
