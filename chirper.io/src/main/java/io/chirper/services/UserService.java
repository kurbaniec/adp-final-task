package io.chirper.services;

import io.chirper.entities.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */
public interface UserService extends UserDetailsService {
    User createUser(@Valid @NotNull User createUser, MultipartFile profileImage);
}
