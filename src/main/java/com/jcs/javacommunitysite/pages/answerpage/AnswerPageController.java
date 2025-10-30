package com.jcs.javacommunitysite.pages.answerpage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.DSLContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jcs.javacommunitysite.atproto.AtprotoClient;
import com.jcs.javacommunitysite.atproto.AtprotoUtil;
import com.jcs.javacommunitysite.atproto.service.AtprotoSessionService;
import static com.jcs.javacommunitysite.jooq.tables.Post.POST;
import static com.jcs.javacommunitysite.jooq.tables.Reply.REPLY;
import static com.jcs.javacommunitysite.jooq.tables.User.USER;

import static dev.mccue.json.JsonDecoder.field;
import static dev.mccue.json.JsonDecoder.string;

/**
 * Controller for the Answer page where users can view and reply to posts.
 * Supports filtering posts by user participation.
 */
@Controller
public class AnswerPageController {

    private final AtprotoSessionService sessionService;
    private final DSLContext dsl;

    public AnswerPageController(AtprotoSessionService sessionService, DSLContext dsl) {
        this.sessionService = sessionService;
        this.dsl = dsl;
    }

    /**
     * Displays the answer page with posts.
     * 
     * @param model Spring MVC model for passing data to the view
     * @param filterMyPosts When true, shows only posts the user has replied to
     * @return The view name for the answer page
     */
    @GetMapping("/answer")
    public String answer(
        Model model,
        @RequestParam(name = "filterMyPosts", defaultValue = "false") boolean filterMyPosts
    ) {
        // Add empty reply form for posting new replies
        model.addAttribute("replyForm", new NewReplyForm());
        model.addAttribute("filterMyPosts", filterMyPosts);

        // Add current user's avatar URL to display in page header
        getCurrentUserAvatarUrl().ifPresent(avatarUrl ->
            model.addAttribute("currentUserAvatarUrl", avatarUrl)
        );

        if (sessionService.isAuthenticated()) {
            var clientOpt = sessionService.getCurrentClient();
            if (clientOpt.isPresent()) {
                try {
                    // Get authenticated user's DID from their Bluesky profile
                    AtprotoClient client = clientOpt.get();
                    String handle = client.getSession().getHandle();
                    var profile = AtprotoUtil.getBskyProfile(handle);
                    String userDid = profile.get("did").toString().replace("\"", "");

                    // Fetch posts based on filter preference
                    // If filterMyPosts is true, only show posts user has participated in
                    // Otherwise, show all posts ordered by most recent
                    List<Map<String, Object>> posts = filterMyPosts 
                        ? getPostsUserParticipatedIn(userDid)
                        : getAllPosts();
                    
                    model.addAttribute("posts", posts);
                    model.addAttribute("userDid", userDid);
                } catch (Exception e) {
                    System.err.println("Error fetching posts: " + e.getMessage());
                }
            }
        } else {
            // Unauthenticated users see all posts
            model.addAttribute("posts", getAllPosts());
        }
        
        return "pages/answer";
    }

    /**
     * REST API endpoint for fetching posts as JSON.
     * Supports filtering by user participation.
     * 
     * @param filterMyPosts When true, returns only posts the user has replied to
     * @return JSON response with posts and metadata
     */
    @GetMapping("/api/answer")
    @ResponseBody
    public ResponseEntity<?> answerApi(
        @RequestParam(name = "filterMyPosts", defaultValue = "false") boolean filterMyPosts
    ) {
        try {
            Map<String, Object> response = new HashMap<>();
            
            if (sessionService.isAuthenticated()) {
                var clientOpt = sessionService.getCurrentClient();
                if (clientOpt.isPresent()) {
                    // Extract user's DID from their AT Protocol session
                    AtprotoClient client = clientOpt.get();
                    String handle = client.getSession().getHandle();
                    var profile = AtprotoUtil.getBskyProfile(handle);
                    String userDid = profile.get("did").toString().replace("\"", "");

                    // Apply filter based on request parameter
                    List<Map<String, Object>> posts = filterMyPosts 
                        ? getPostsUserParticipatedIn(userDid)
                        : getAllPosts();
                    
                    // Build response with posts and metadata
                    response.put("posts", posts);
                    response.put("userDid", userDid);
                    response.put("filterMyPosts", filterMyPosts);
                    response.put("authenticated", true);
                    
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "No client session available"));
                }
            } else {
                // Unauthenticated users get all posts without filtering
                response.put("posts", getAllPosts());
                response.put("filterMyPosts", filterMyPosts);
                response.put("authenticated", false);
                
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Debug endpoint for testing post filtering with a specific user DID.
     * Useful for verifying that the filtering logic works correctly.
     * 
     * @param ownerDid The DID of the user to test filtering for
     * @return JSON response with debug information about replies and filtered posts
     */
    @GetMapping("/api/answer/debug")
    @ResponseBody
    public ResponseEntity<?> debugAnswerApi(
        @RequestParam(name = "ownerDid") String ownerDid
    ) {
        try {
            System.out.println("Debug - Testing with ownerDid: " + ownerDid);
            
            // Query all replies made by this user
            var userReplies = dsl.selectFrom(REPLY)
                .where(REPLY.OWNER_DID.eq(ownerDid))
                .fetch();
            
            // Log reply details for debugging
            System.out.println("Total replies by user: " + userReplies.size());
            userReplies.forEach(reply -> {
                System.out.println("Reply - Root Post ATURI: " + reply.getRootPostAturi());
            });
            
            // Get posts this user has participated in
            var posts = getPostsUserParticipatedIn(ownerDid);
            
            // Build debug response
            Map<String, Object> response = new HashMap<>();
            response.put("ownerDid", ownerDid);
            response.put("totalReplies", userReplies.size());
            response.put("posts", posts);
            response.put("postsCount", posts.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Fetches all posts from the database, ordered by creation date (most recent first).
     * Converts jOOQ records to Map for JSON serialization.
     * 
     * @return List of all posts as Maps
     */
    private List<Map<String, Object>> getAllPosts() {
        var records = dsl.selectFrom(POST)
            .orderBy(POST.CREATED_AT.desc())
            .fetch();
        
        return records.stream()
            .map(record -> {
                Map<String, Object> map = new HashMap<>();
                map.put("aturi", record.getAturi());
                map.put("title", record.getTitle());
                map.put("content", record.getContent());
                // Extract raw JSON string from jOOQ JSON type to avoid serialization issues
                map.put("tags", record.getTags() != null ? record.getTags().data() : null);
                map.put("isDeleted", record.getIsDeleted());
                map.put("isOpen", record.getIsOpen());
                map.put("status", record.getStatus());
                // Handle nullable timestamps
                map.put("createdAt", record.getCreatedAt() != null ? record.getCreatedAt().toString() : null);
                map.put("updatedAt", record.getUpdatedAt() != null ? record.getUpdatedAt().toString() : null);
                map.put("ownerDid", record.getOwnerDid());
                return map;
            })
            .toList();
    }

    /**
     * Fetches posts that a specific user has participated in (replied to).
     * Uses SQL EXISTS to find posts where the user has made at least one reply.
     * Results are ordered by post creation date (most recent first).
     * 
     * @param userDid The DID of the user to filter posts for
     * @return List of posts the user has participated in
     */
    private List<Map<String, Object>> getPostsUserParticipatedIn(String userDid) {
        var records = dsl.selectFrom(POST)
            .whereExists(
                // Subquery: check if there's a reply from this user on this post
                dsl.selectOne()
                    .from(REPLY)
                    .where(REPLY.ROOT_POST_ATURI.eq(POST.ATURI))  // Match reply to post
                    .and(REPLY.OWNER_DID.eq(userDid))              // Filter by user's DID
            )
            .orderBy(POST.CREATED_AT.desc())
            .fetch();
        
        // Debug logging to help troubleshoot filtering issues
        System.out.println("User DID: " + userDid);
        System.out.println("Filtered posts count: " + records.size());
        
        return records.stream()
            .map(record -> {
                Map<String, Object> map = new HashMap<>();
                map.put("aturi", record.getAturi());
                map.put("title", record.getTitle());
                map.put("content", record.getContent());
                map.put("tags", record.getTags() != null ? record.getTags().data() : null);
                map.put("isDeleted", record.getIsDeleted());
                map.put("isOpen", record.getIsOpen());
                map.put("status", record.getStatus());
                map.put("createdAt", record.getCreatedAt() != null ? record.getCreatedAt().toString() : null);
                map.put("updatedAt", record.getUpdatedAt() != null ? record.getUpdatedAt().toString() : null);
                map.put("ownerDid", record.getOwnerDid());
                return map;
            })
            .toList();
    }

    /**
     * Retrieves the avatar URL for the currently authenticated user.
     * Queries the user table using the DID from the AT Protocol session.
     * 
     * @return Optional containing the avatar URL if found, empty otherwise
     */
    private java.util.Optional<String> getCurrentUserAvatarUrl() {
        try {
            var clientOpt = sessionService.getCurrentClient();
            if (clientOpt.isEmpty()) {
                return java.util.Optional.empty();
            }
            AtprotoClient client = clientOpt.get();
            String handle = client.getSession().getHandle();
            
            // Get the DID from the user's Bluesky profile
            var profile = AtprotoUtil.getBskyProfile(handle);
            String userDid = field(profile, "did", string());
            
            // Look up user record in database by DID
            var userRecord = dsl.selectFrom(USER)
                    .where(USER.DID.eq(userDid))
                    .fetchOne();
            
            // Return avatar URL if it exists and is not empty
            if (userRecord != null && userRecord.getAvatarBloburl() != null && !userRecord.getAvatarBloburl().trim().isEmpty()) {
                return java.util.Optional.of(userRecord.getAvatarBloburl());
            }
            
            return java.util.Optional.empty();
            
        } catch (IOException e) {
            System.err.println("Error getting current user avatar: " + e.getMessage());
            return java.util.Optional.empty();
        }
    }
}
