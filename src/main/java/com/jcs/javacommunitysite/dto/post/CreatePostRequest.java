package com.jcs.javacommunitysite.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreatePostRequest {
    private String userId;
    private String communityId;
    private String title;
    private String content;
}
