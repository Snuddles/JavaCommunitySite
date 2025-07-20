package com.jcs.javacommunitysite.repository;

import com.jcs.javacommunitysite.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VoteRepository extends JpaRepository<Vote, UUID> {
    Optional<Vote> findByUserIdAndPostId(UUID userId, UUID postId);
}
