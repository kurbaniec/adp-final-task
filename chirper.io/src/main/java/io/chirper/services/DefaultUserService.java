package io.chirper.services;

import io.chirper.entities.User;
import io.chirper.repositories.UserRepository;
import io.chirper.validators.CreateUserValidation;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */

@Service
@Validated
public class DefaultUserService implements UserService {
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public DefaultUserService(UserRepository userRepository, ImageService imageService) {
        this.userRepository = userRepository;
        this.imageService = imageService;
    }

    @Override
    @Transactional
    @Validated({CreateUserValidation.class})
    public User createUser(@Valid @NotNull User createUser, MultipartFile profileImage) {
        logger.debug("createUser({})", createUser);
        if (userRepository.existsByUsername(createUser.getUsername())) {
            throw new ConstraintViolationException("Username is already taken", null);
        }
        if (profileImage != null) {
            var image = imageService.createImage(profileImage);
            createUser.setImageId(image.getId());
        }
        var password = createUser.getPassword();
        password = encoder.encode(password);
        createUser.setPassword(password);
        return userRepository.save(createUser);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository
            .findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User with username " + username + " not found"));
        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getId().toString())
            .password(user.getPassword())
            .roles("USER")
            .build();
    }
}
