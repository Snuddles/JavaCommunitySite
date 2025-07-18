package com.jcs.javacommunitysite.service;

import com.jcs.javacommunitysite.dto.post.CreatePostRequest;
import com.jcs.javacommunitysite.dto.post.PostDTO;
import com.jcs.javacommunitysite.model.Community;
import com.jcs.javacommunitysite.model.Post;
import com.jcs.javacommunitysite.model.User;
import com.jcs.javacommunitysite.repository.CommunityRepository;
import com.jcs.javacommunitysite.repository.PostRepository;
import com.jcs.javacommunitysite.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PostService {

    private PostRepository postRepo;
    private UserRepository userRepo;
    private CommunityRepository communityRepo;

    public PostService(PostRepository postRepo, UserRepository userRepo, CommunityRepository communityRepo) {
        this.postRepo = postRepo;
        this.userRepo = userRepo;
        this.communityRepo = communityRepo;
    }

    public PostDTO createPost(CreatePostRequest request){

        User userPosting = userRepo.findById(UUID.fromString(request.getUserId()))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Community communityPosting = communityRepo.findById(UUID.fromString(request.getCommunityId()))
                .orElseThrow(() -> new IllegalArgumentException("Community not found"));

        Post post = Post.builder()
                .user(userPosting)
                .community(communityPosting)
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        postRepo.save(post);

        return PostDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
