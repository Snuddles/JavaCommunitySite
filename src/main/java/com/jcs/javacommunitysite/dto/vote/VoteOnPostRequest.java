package com.jcs.javacommunitysite.dto.vote;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VoteOnPostRequest {
    private String userId;
    private String postId;
    private Integer vote;
}