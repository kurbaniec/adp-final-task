package io.chirper.configuration;

import io.chirper.common.FileToMultipartFile;
import io.chirper.controllers.ServiceMapper;
import io.chirper.dtos.ChirpDTO;
import io.chirper.dtos.ReplyDTO;
import io.chirper.dtos.UserDTO;
import io.chirper.services.ChirpService;
import io.chirper.services.UserService;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Kacper Urbaniec
 * @version 2024-01-07
 */
@Component
@Profile("datagen") // Use environment "spring.profiles.active=datagen" to perform data generation
public class DataGenConfiguration {
    private final UserService userService;
    private final ChirpService chirpService;
    private final ServiceMapper serviceMapper;
    private final ResourceLoader resourceLoader;
    private final String passwd = "testtest";
    private final Map<String, UUID> users = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public DataGenConfiguration(UserService userService, ChirpService chirpService, ServiceMapper serviceMapper, ResourceLoader resourceLoader) {
        this.userService = userService;
        this.chirpService = chirpService;
        this.serviceMapper = serviceMapper;
        this.resourceLoader = resourceLoader;
    }


    @EventListener(ApplicationReadyEvent.class)
    public void generate() throws IOException {
        logger.info("Generating test data...");
        addUsers();

        addChirpWithReplies(
            "Just saw the Oscars 2024 Best Picture winner. It had more plot twists than my attempt to fold a fitted sheet! 🎥😂 #Oscars2024",
            datagenFile("post1.png"),
            "MovieBuff123",
            List.of(new ReplyGen(
                "Haha, folding fitted sheets should be an Olympic sport! 🏅#Relatable",
                null,
                "MovieBuff123"
            ))
        );

        addChirpWithReplies(
            "The best supporting actor's speech was so long, I managed to cook dinner, eat it, and do the dishes before he finished. #OscarTalks",
            datagenFile("post2.png"),
            "CinematicCritique",
            List.of(new ReplyGen(
                "Next year, they should introduce an orchestra playing them off... but with meme songs. 🤣 #OscarMemes",
                null,
                "PopCulturePundit"
            ))
        );

        addChirpWithReplies(
            "2024 Oscars fun fact: The red carpet is eco-friendly, made from recycled plastic bottles. Finally, a carpet that's as green as the celebrities' initiatives! 🌱 #EcoOscars",
            datagenFile("post3.png"),
            "PopCulturePundit",
            List.of(new ReplyGen(
                "Love this step forward! Next, let's see electric limos? #GreenCarpet",
                null,
                "EcoEddie"
            ))
        );

        addChirpWithReplies(
            "Imagine if the Oscars had a 'Best Pet in a Leading Role' category. The acceptance speeches would be *pawsitively* amazing! 🐾 #PetOscars",
            datagenFile("post4.png"),
            "LaughTrack",
            List.of(new ReplyGen(
                "And the award goes to... Mr. Whiskers, for his purrformance in 'Cats & Dogs: The Revenge of Kitty Galore 2' 😹",
                null,
                "MovieBuff123"
            ))
        );

        addChirpWithReplies(
            "Did anyone else notice the plant-based menu at the Oscars this year? Guess it's bye-bye to the steak and hello to the steak-plant! 🌿🥩 #VeganOscars",
            datagenFile("post5.png"),
            "EcoEddie",
            List.of(new ReplyGen(
                "Plant-based diets are taking over, even the Oscars aren't immune! Maybe next year, they'll serve popcorn in edible bowls? 🍿 #FutureFood",
                null,
                "CinematicCritique"
            ))
        );


        logger.info("Generation finished!");
    }

    private void addUsers() {
        var usernames = List.of("EcoEddie", "CinematicCritique", "LaughTrack", "MovieBuff123", "PopCulturePundit");
        for (var username : usernames) {
            var userDto = UserDTO.builder()
                .username(username)
                .password(passwd)
                .build();
            var user = serviceMapper.userToEntity(userDto);
            var createdUser = userService.createUser(user, null);
            users.put(username, createdUser.getId());
        }
    }

    private record ReplyGen(String text, MultipartFile file, String username) {
    }

    private void addChirpWithReplies(
        @NotNull String text, MultipartFile file, String username,
        List<ReplyGen> replies
    ) {
        var chirpDto = ChirpDTO.builder()
            .text(text)
            .build();
        var chirp = serviceMapper.chirpToEntity(chirpDto);
        var createdChirp = chirpService.createChirp(chirp, file, users.get(username));
        for (var replyGen : replies) {
            var replyDto = ReplyDTO.builder()
                .text(replyGen.text)
                .build();
            var reply = serviceMapper.replyToEntity(replyDto);
            chirpService.createReply(createdChirp.getId(), reply, replyGen.file, users.get(username));
        }
    }

    private MultipartFile datagenFile(String path) throws IOException {
        var resource = resourceLoader.getResource("classpath:/datagen/" + path);
        var file = resource.getFile();
        return new FileToMultipartFile(file);
    }
}
