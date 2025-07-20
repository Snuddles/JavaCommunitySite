package com.jcs.javacommunitysite.service;

import com.jcs.javacommunitysite.dto.post.CreatePostRequest;
import com.jcs.javacommunitysite.dto.post.PostDTO;
import com.jcs.javacommunitysite.dto.vote.VoteOnPostRequest;
import com.jcs.javacommunitysite.model.Community;
import com.jcs.javacommunitysite.model.Post;
import com.jcs.javacommunitysite.model.User;
import com.jcs.javacommunitysite.model.Vote;
import com.jcs.javacommunitysite.repository.CommunityRepository;
import com.jcs.javacommunitysite.repository.PostRepository;
import com.jcs.javacommunitysite.repository.UserRepository;
import com.jcs.javacommunitysite.repository.VoteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class VoteService {

    private VoteRepository voteRepo;
    private PostRepository postRepo;
    private UserRepository userRepo;

    public VoteService(VoteRepository voteRepo, PostRepository postRepo, UserRepository userRepo) {
        this.voteRepo = voteRepo;
        this.postRepo = postRepo;
        this.userRepo = userRepo;
    }

    public void voteOnPost(VoteOnPostRequest request){

        UUID postId = UUID.fromString(request.getPostId());
        UUID userId = UUID.fromString(request.getUserId());

        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Optional<Vote> existingVote = voteRepo.findByUserIdAndPostId(userId, postId);

        Vote vote;
        if (existingVote.isPresent()) {
            vote = existingVote.get();
            vote.setVoteType(request.getVote().shortValue());
        } else {
            vote = Vote.builder()
                    .user(user)
                    .post(post)
                    .voteType(request.getVote().shortValue())
                    .build();

        }
        voteRepo.save(vote);
    }
}
