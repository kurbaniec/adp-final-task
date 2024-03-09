package io.chirper.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */

@Configuration
public class SecurityConfiguration {

    @Bean
    @SuppressWarnings("removal")
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
            .csrf().disable()
            .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/user/register").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic();

        return httpSecurity.build();
    }
}
