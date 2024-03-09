package io.chirper.services;

import io.chirper.entities.User;
import io.chirper.repositories.UserRepository;
import io.chirper.validators.CreateUserValidation;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        if (profileImage != null) {
            var image = imageService.createImage(profileImage);
            createUser.setImageId(image.getId());
        }
        var password = createUser.getPassword();
        password = encoder.encode(password);
        createUser.setPassword(password);
        return userRepository.save(createUser);
    }
}
