package io.chirper.services;

import io.chirper.entities.User;
import io.chirper.repositories.UserRepository;
import io.chirper.validators.CreateUserValidation;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */

@Service
@Validated
public class DefaultUserService implements UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public DefaultUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    @Validated({CreateUserValidation.class})
    public User createUser(@Valid User createUser) {
        logger.debug("createUser({})", createUser);
        // TODO: file service

        var password = createUser.getPassword();
        password = encoder.encode(password);
        createUser.setPassword(password);
        return userRepository.save(createUser);
    }
}
