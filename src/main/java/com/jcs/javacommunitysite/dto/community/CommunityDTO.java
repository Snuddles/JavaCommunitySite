package com.jcs.javacommunitysite.dto.community;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CommunityDTO(
        UUID id,
        String name,
        String description
) {}