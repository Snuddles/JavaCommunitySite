package com.jcs.javacommunitysite.service;

import com.jcs.javacommunitysite.dto.comment.CommentDTO;
import com.jcs.javacommunitysite.dto.comment.CreateCommentRequest;
import com.jcs.javacommunitysite.dto.user.UserDTO;
import com.jcs.javacommunitysite.model.Comment;
import com.jcs.javacommunitysite.model.Post;
import com.jcs.javacommunitysite.model.User;
import com.jcs.javacommunitysite.repository.CommentRepository;
import com.jcs.javacommunitysite.repository.PostRepository;
import com.jcs.javacommunitysite.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CommentService {

    private CommentRepository commentRepo;
    private UserRepository userRepo;
    private PostRepository postRepo;

    public CommentService(CommentRepository commentRepo, UserRepository userRepo, PostRepository postRepo) {
        this.commentRepo = commentRepo;
        this.userRepo = userRepo;
        this.postRepo = postRepo;
    }

    public CommentDTO createComment(CreateCommentRequest request){
        if (request.getContent() == null || request.getContent().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }

        User commentUser = userRepo.findById(UUID.fromString(request.getUserId()))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Post commentPost = postRepo.findById(UUID.fromString(request.getPostId()))
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Comment comment = Comment.builder()
                .user(commentUser)
                .post(commentPost)
                .content(request.getContent())
                .build();

        commentRepo.save(comment);

        return CommentDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
