package com.jcs.javacommunitysite.postpage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jcs.javacommunitysite.atproto.AtUri;
import com.jcs.javacommunitysite.atproto.AtprotoClient;
import com.jcs.javacommunitysite.atproto.exceptions.AtprotoUnauthorized;
import com.jcs.javacommunitysite.atproto.records.PostRecord;
import com.jcs.javacommunitysite.atproto.service.AtprotoSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

import static com.jcs.javacommunitysite.JavaCommunitySiteApplication.addLexiconPrefix;

@RestController
@RequestMapping("/api/postpage")
public class PostpageController {

    private final AtprotoSessionService sessionService;

    //Constructor
    public PostpageController(AtprotoSessionService sessionService) {
        this.sessionService = sessionService;
    }

    //Create post API
    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@RequestBody Map<String, Object> postData) {
        try {
            //Get current client from the session and validate it
            Optional<AtprotoClient> clientOpt = sessionService.getCurrentClient();
            if (clientOpt.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Not authenticated"
                ));
            }

            AtprotoClient client = clientOpt.get();

            //Extract the data for the post
            String title = (String) postData.get("title");
            String content = (String) postData.get("content");
            String categoryUri = (String) postData.get("category");
            String forum = (String) postData.get("forum");
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) postData.getOrDefault("tags", new ArrayList<>());

            // Validate required fields
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Title is required"
                ));
            }
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Content is required"
                ));
            }
            if (categoryUri == null || categoryUri.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Category is required"
                ));
            }
            if (forum == null || forum.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Forum is required"
                ));
            }

            PostRecord post = new PostRecord(title, content, new AtUri(categoryUri), forum, tags);

            //Create the record via ATProtocol
            client.createRecord(post);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Post created successfully",
                    "atUri", Objects.requireNonNull(post.getAtUri().map(AtUri::toString).orElse(null)),
                    "post", Map.of(
                            "title", post.getTitle(),
                            "content", post.getContent(),
                            "createdAt", post.getCreatedAt().toString(),
                            "category", post.getCategory().toString(),
                            "forum", post.getForum(),
                            "tags", post.getTags()
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error creating post: " + e.getMessage()
            ));
        }
    }

    //Get single post API
    @GetMapping("/posts/{atUri}")
    public ResponseEntity<?> getPost(@PathVariable String atUri) {
        try {
            Optional<AtprotoClient> clientOpt = sessionService.getCurrentClient();
            if (clientOpt.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Not authenticated"
                ));
            }

            AtprotoClient client = clientOpt.get();
            AtUri parsedUri = new AtUri(atUri);
            
            JsonObject response = client.getRecord(
                    parsedUri.getDid(),
                    parsedUri.getCollection(),
                    parsedUri.getRecordKey()
            );

            JsonObject record = response.getAsJsonObject("value");
            PostRecord post = new PostRecord(parsedUri, record);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "post", Map.of(
                            "atUri", atUri,
                            "title", post.getTitle(),
                            "content", post.getContent(),
                            "createdAt", post.getCreatedAt().toString(),
                            "updatedAt", post.getUpdatedAt() != null ? post.getUpdatedAt().toString() : null,
                            "category", post.getCategory().toString(),
                            "forum", post.getForum(),
                            "tags", post.getTags(),
                            "solution", post.getSolution() != null ? post.getSolution().toString() : null
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error retrieving post: " + e.getMessage()
            ));
        }
    }

    //Update post API
    @PutMapping("/posts/{atUri}")
    public ResponseEntity<?> updatePost(@PathVariable String atUri, @RequestBody Map<String, Object> postData) {
        try {
            Optional<AtprotoClient> clientOpt = sessionService.getCurrentClient();
            if (clientOpt.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Not authenticated"
                ));
            }

            AtprotoClient client = clientOpt.get();
            AtUri parsedUri = new AtUri(atUri);

            // First get the existing record
            JsonObject response = client.getRecord(
                    parsedUri.getDid(),
                    parsedUri.getCollection(),
                    parsedUri.getRecordKey()
            );

            JsonObject record = response.getAsJsonObject("value");
            PostRecord post = new PostRecord(parsedUri, record);

            // Update fields if provided
            if (postData.containsKey("title")) {
                String title = (String) postData.get("title");
                if (title != null && !title.trim().isEmpty()) {
                    post.setTitle(title);
                }
            }
            
            if (postData.containsKey("content")) {
                String content = (String) postData.get("content");
                if (content != null && !content.trim().isEmpty()) {
                    post.setContent(content);
                }
            }
            
            if (postData.containsKey("tags")) {
                @SuppressWarnings("unchecked")
                List<String> tags = (List<String>) postData.get("tags");
                if (tags != null) {
                    post.setTags(tags);
                }
            }
            
            if (postData.containsKey("category")) {
                String categoryUri = (String) postData.get("category");
                if (categoryUri != null && !categoryUri.trim().isEmpty()) {
                    post.setCategory(new AtUri(categoryUri));
                }
            }

            post.setUpdatedAt(Instant.now());

            // Update the record via ATProtocol
            client.updateRecord(post);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Post updated successfully",
                    "post", Map.of(
                            "atUri", atUri,
                            "title", post.getTitle(),
                            "content", post.getContent(),
                            "createdAt", post.getCreatedAt().toString(),
                            "updatedAt", post.getUpdatedAt().toString(),
                            "category", post.getCategory().toString(),
                            "forum", post.getForum(),
                            "tags", post.getTags(),
                            "solution", post.getSolution() != null ? post.getSolution().toString() : null
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error updating post: " + e.getMessage()
            ));
        }
    }

    //Delete post API
    @DeleteMapping("/posts/{atUri}")
    public ResponseEntity<?> deletePost(@PathVariable String atUri) {
        try {
            Optional<AtprotoClient> clientOpt = sessionService.getCurrentClient();
            if (clientOpt.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Not authenticated"
                ));
            }

            AtprotoClient client = clientOpt.get();
            AtUri parsedUri = new AtUri(atUri);
            PostRecord post = new PostRecord(parsedUri);

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

    //List posts API with filtering and pagination
    @GetMapping("/posts")
    public ResponseEntity<?> listPosts(
            @RequestParam(required = false) String forum,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) String cursor) {
        try {
            Optional<AtprotoClient> clientOpt = sessionService.getCurrentClient();
            if (clientOpt.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Not authenticated"
                ));
            }

            AtprotoClient client = clientOpt.get();
            String collection = addLexiconPrefix("feed.post");
            
            // For listing posts, we'll need to query from multiple repositories
            // This is a simplified implementation that gets posts from the current user's repo
            JsonObject response = client.listRecords(
                    sessionService.getCurrentSession().orElseThrow().getHandle(),
                    collection,
                    limit,
                    cursor
            );

            JsonArray records = response.getAsJsonArray("records");
            List<Map<String, Object>> posts = new ArrayList<>();

            for (JsonElement recordElement : records) {
                JsonObject recordObj = recordElement.getAsJsonObject();
                String recordUri = recordObj.get("uri").getAsString();
                JsonObject value = recordObj.getAsJsonObject("value");
                
                PostRecord post = new PostRecord(new AtUri(recordUri), value);
                
                // Apply filters
                boolean includePost = true;
                
                if (forum != null && !forum.equals(post.getForum())) {
                    includePost = false;
                }
                
                if (category != null && !category.equals(post.getCategory().toString())) {
                    includePost = false;
                }
                
                if (tag != null && !post.getTags().contains(tag)) {
                    includePost = false;
                }
                
                if (includePost) {
                    posts.add(Map.of(
                            "atUri", recordUri,
                            "title", post.getTitle(),
                            "content", post.getContent(),
                            "createdAt", post.getCreatedAt().toString(),
                            "updatedAt", post.getUpdatedAt() != null ? post.getUpdatedAt().toString() : null,
                            "category", post.getCategory().toString(),
                            "forum", post.getForum(),
                            "tags", post.getTags(),
                            "solution", post.getSolution() != null ? post.getSolution().toString() : null
                    ));
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("posts", posts);
            
            if (response.has("cursor")) {
                result.put("cursor", response.get("cursor").getAsString());
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error listing posts: " + e.getMessage()
            ));
        }
    }

    //Mark solution API
    @PutMapping("/posts/{atUri}/solution")
    public ResponseEntity<?> markSolution(@PathVariable String atUri, @RequestBody Map<String, Object> solutionData) {
        try {
            Optional<AtprotoClient> clientOpt = sessionService.getCurrentClient();
            if (clientOpt.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Not authenticated"
                ));
            }

            AtprotoClient client = clientOpt.get();
            AtUri parsedUri = new AtUri(atUri);

            // Get the existing post
            JsonObject response = client.getRecord(
                    parsedUri.getDid(),
                    parsedUri.getCollection(),
                    parsedUri.getRecordKey()
            );

            JsonObject record = response.getAsJsonObject("value");
            PostRecord post = new PostRecord(parsedUri, record);

            // Set the solution
            String solutionUri = (String) solutionData.get("solutionUri");
            if (solutionUri != null && !solutionUri.trim().isEmpty()) {
                post.setSolution(new AtUri(solutionUri));
            } else {
                post.setSolution(null); // Remove solution
            }

            post.setUpdatedAt(Instant.now());

            // Update the record
            client.updateRecord(post);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Solution updated successfully",
                    "solution", post.getSolution() != null ? post.getSolution().toString() : null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error updating solution: " + e.getMessage()
            ));
        }
    }

    //Search posts API
    @GetMapping("/posts/search")
    public ResponseEntity<?> searchPosts(
            @RequestParam String query,
            @RequestParam(required = false) String forum,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) String cursor) {
        try {
            Optional<AtprotoClient> clientOpt = sessionService.getCurrentClient();
            if (clientOpt.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Not authenticated"
                ));
            }

            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Search query is required"
                ));
            }

            AtprotoClient client = clientOpt.get();
            String collection = addLexiconPrefix("feed.post");
            
            // Get all posts and filter them
            JsonObject response = client.listRecords(
                    sessionService.getCurrentSession().orElseThrow().getHandle(),
                    collection,
                    null, // Get all posts for searching
                    cursor
            );

            JsonArray records = response.getAsJsonArray("records");
            List<Map<String, Object>> matchingPosts = new ArrayList<>();
            String queryLower = query.toLowerCase();

            for (JsonElement recordElement : records) {
                JsonObject recordObj = recordElement.getAsJsonObject();
                String recordUri = recordObj.get("uri").getAsString();
                JsonObject value = recordObj.getAsJsonObject("value");
                
                PostRecord post = new PostRecord(new AtUri(recordUri), value);
                
                // Check if post matches search criteria
                boolean matches = false;
                
                // Search in title
                if (post.getTitle().toLowerCase().contains(queryLower)) {
                    matches = true;
                }
                
                // Search in content
                if (!matches && post.getContent().toLowerCase().contains(queryLower)) {
                    matches = true;
                }
                
                // Search in tags
                if (!matches) {
                    for (String tag : post.getTags()) {
                        if (tag.toLowerCase().contains(queryLower)) {
                            matches = true;
                            break;
                        }
                    }
                }
                
                if (matches) {
                    // Apply additional filters
                    boolean includePost = true;
                    
                    if (forum != null && !forum.equals(post.getForum())) {
                        includePost = false;
                    }
                    
                    if (category != null && !category.equals(post.getCategory().toString())) {
                        includePost = false;
                    }
                    
                    if (includePost) {
                        matchingPosts.add(Map.of(
                                "atUri", recordUri,
                                "title", post.getTitle(),
                                "content", post.getContent(),
                                "createdAt", post.getCreatedAt().toString(),
                                "updatedAt", post.getUpdatedAt() != null ? post.getUpdatedAt().toString() : null,
                                "category", post.getCategory().toString(),
                                "forum", post.getForum(),
                                "tags", post.getTags(),
                                "solution", post.getSolution() != null ? post.getSolution().toString() : null
                        ));
                    }
                }
                
                // Limit results
                if (matchingPosts.size() >= limit) {
                    break;
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("posts", matchingPosts);
            result.put("query", query);
            result.put("totalResults", matchingPosts.size());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error searching posts: " + e.getMessage()
            ));
        }
    }
}
