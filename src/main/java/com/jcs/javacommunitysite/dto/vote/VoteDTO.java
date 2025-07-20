package com.jcs.javacommunitysite.dto.vote;


import lombok.Builder;

import java.util.UUID;

@Builder
public record VoteDTO(
        UUID id,
        String postId,
        String userId,
        Integer voteType
) {}
