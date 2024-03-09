package io.chirper.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */

@Entity
@Table(name = "chirps")
@Getter
@Setter
public class Chirp {
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
    @OneToMany(fetch = FetchType.LAZY)
    private List<Reply> replies = Collections.emptyList();

    private Instant createdOn = Instant.now();
}
