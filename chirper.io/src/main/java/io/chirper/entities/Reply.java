package io.chirper.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */

@Entity
@Table(name = "replies")
@Getter
@Setter
public class Reply {
    @Id
    @GeneratedValue
    private UUID id;

    @Lob
    @NotNull
    @NotEmpty
    @Size(max = 280)
    private String text;
    private Long likes;

    private UUID imageId;

    @OneToOne(fetch = FetchType.EAGER)
    private User author;
    @ManyToOne(fetch = FetchType.LAZY)
    private Chirp chirp;

    private Instant createdOn = Instant.now();
}
