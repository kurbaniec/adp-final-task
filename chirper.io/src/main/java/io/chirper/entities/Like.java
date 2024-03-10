package io.chirper.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-10
 */

@Entity
@Table(name = "likes")
@Getter
@Setter
public class Like {

    public enum Resource {
        POST, REPLY
    }

    @Id
    @GeneratedValue
    private UUID id;

    private UUID resourceId;

    @Enumerated(EnumType.STRING)
    private Resource resource;

    private UUID userId;
}
