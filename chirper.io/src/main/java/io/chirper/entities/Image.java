package io.chirper.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */

@Entity
@Table(name = "images")
@Getter
@Setter
public class Image {
    @Id
    @GeneratedValue
    private UUID id;

    @Pattern(regexp = ".*\\.(png|jpg)$", message = "Images must end with .png or .jpg")
    private String filename;

}
