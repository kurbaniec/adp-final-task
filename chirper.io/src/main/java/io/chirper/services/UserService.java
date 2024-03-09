package io.chirper.services;

import io.chirper.entities.User;
import jakarta.validation.Valid;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */
public interface UserService {
    User createUser(@Valid User createUser);
}
