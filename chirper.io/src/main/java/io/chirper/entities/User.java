package io.chirper.entities;

import io.chirper.validators.CreateUserValidation;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue
    @Null(groups = CreateUserValidation.class)
    private UUID id;

    @NotNull
    @NotEmpty
    @Column(unique = true)
    private String username;

    @NotNull
    @NotEmpty
    @Size(min = 8, groups = CreateUserValidation.class)
    private String password;

    private UUID imageId;
}
