package com.jcs.javacommunitysite.homepage;

import com.google.gson.JsonObject;
import com.jcs.javacommunitysite.atproto.AtprotoClient;
import com.jcs.javacommunitysite.atproto.service.AtprotoSessionService;
import com.jcs.javacommunitysite.atproto.records.PostRecord;
import com.jcs.javacommunitysite.atproto.AtUri;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class HomepageController {
    
    private final AtprotoSessionService sessionService;

    public HomepageController(AtprotoSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@RequestBody Map<String, Object> postData) {
        try {
            Optional<AtprotoClient> clientOpt = sessionService.getCurrentClient();
            if (clientOpt.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Not authenticated"
                ));
            }
            
            AtprotoClient client = clientOpt.get();
            
            // Extract post data
            String text = (String) postData.get("text");
            String categoryUri = (String) postData.get("category");
            String forum = (String) postData.get("forum");
            
            // Create post record
            PostRecord post = new PostRecord(new AtUri(categoryUri), new JsonObject());
            
            // Create the record via ATProtocol
            client.createRecord(post);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Post created successfully",
                "atUri", Objects.requireNonNull(post.getAtUri().map(AtUri::toString).orElse(null))
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error creating post: " + e.getMessage()
            ));
        }
    }
    
    @GetMapping("/posts")
    public ResponseEntity<?> getPosts() {
        // Implementation for fetching posts
        return ResponseEntity.ok(Map.of("posts", "[]"));
    }

    @DeleteMapping("/posts")
    public ResponseEntity<?> deletePost(@RequestBody Map<String, Object> postData) {
        try {
            Optional<AtprotoClient> clientOpt = sessionService.getCurrentClient();
            if (clientOpt.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Not authenticated"
                ));
            }

            AtprotoClient client = clientOpt.get();

            String postUri = postData.get("uri").toString();
            PostRecord post = new PostRecord(new AtUri(postUri));

            client.deleteRecord(post);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Post deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error deleting post: " + e.getMessage()
            ));
        }
    }
}