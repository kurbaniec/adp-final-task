package io.chirper.configuration;

import io.chirper.services.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */

@Configuration
public class SecurityConfiguration {
    private final UserService userService;

    public SecurityConfiguration(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @SuppressWarnings("removal")
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        AuthenticationManagerBuilder authBuilder =
            httpSecurity.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder
            .userDetailsService(userService)
            .passwordEncoder(bCryptPasswordEncoder());
        var authManager = authBuilder.build();

        httpSecurity
            .csrf().disable()
            .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/user/register").permitAll()
                .anyRequest().authenticated()
            )
            .authenticationManager(authManager)
            .httpBasic();

        return httpSecurity.build();
    }
}
