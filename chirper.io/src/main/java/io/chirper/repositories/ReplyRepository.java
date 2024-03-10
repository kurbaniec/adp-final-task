package io.chirper.repositories;

import io.chirper.entities.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * @author Kacper Urbaniec
 * @version 2024-03-09
 */
public interface ReplyRepository extends JpaRepository<Reply, UUID> {

}
