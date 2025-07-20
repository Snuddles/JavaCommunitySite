package com.jcs.javacommunitysite.controller;

import com.jcs.javacommunitysite.dto.vote.VoteOnPostRequest;
import com.jcs.javacommunitysite.service.VoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class VoteController {

    VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @Operation(summary = "Vote on a post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Voted successfully"),
    })
    @PostMapping("/{postId}/vote")
    public ResponseEntity<?> voteOnPost(@PathVariable String postId, @RequestBody VoteOnPostRequest request) {
        try {
            voteService.voteOnPost(request);
            return ResponseEntity.status(201).body("Vote recorded successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }
}

