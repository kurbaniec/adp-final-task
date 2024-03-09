package io.chirper.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Kacper Urbaniec
 * @version 2024-01-07
 */
@Component
@Profile("datagen") // Use environment "spring.profiles.active=datagen" to perform data generation
public class DataGenConfiguration {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @EventListener(ApplicationReadyEvent.class)
    public void generate() {
        logger.info("Generating test data...");



        logger.info("Generation finished!");
    }
}
