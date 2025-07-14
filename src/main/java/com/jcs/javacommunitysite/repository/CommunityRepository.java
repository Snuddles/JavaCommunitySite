package com.jcs.javacommunitysite.repository;

import com.jcs.javacommunitysite.model.Community;
import com.jcs.javacommunitysite.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityRepository extends JpaRepository<Community, Long> {
    boolean existsByName(String name);

    Optional<Community> findByName(String name);
}