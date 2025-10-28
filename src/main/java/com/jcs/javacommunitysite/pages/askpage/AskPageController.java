package com.jcs.javacommunitysite.pages.askpage;

import org.jooq.DSLContext;
import org.jooq.JSON;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import com.jcs.javacommunitysite.atproto.service.AtprotoSessionService;
import com.jcs.javacommunitysite.atproto.AtprotoClient;
import com.jcs.javacommunitysite.atproto.AtUri;
import com.jcs.javacommunitysite.atproto.AtprotoUtil;
import dev.mccue.json.Json;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static com.jcs.javacommunitysite.jooq.tables.Post.POST;
import static com.jcs.javacommunitysite.jooq.tables.User.USER;
import static dev.mccue.json.JsonDecoder.field;
import static dev.mccue.json.JsonDecoder.string;

@Controller
public class AskPageController {

    private final AtprotoSessionService sessionService;
    private final DSLContext dsl;

    public AskPageController(AtprotoSessionService sessionService, DSLContext dsl) {
        this.sessionService = sessionService;
        this.dsl = dsl;
    }

    @GetMapping("/ask")
    public String ask(
            Model model
    ) {
        model.addAttribute("postForm", new NewPostForm());
        
        // Add current user's avatar URL to model for pageHeader
        getCurrentUserAvatarUrl().ifPresent(avatarUrl -> 
            model.addAttribute("currentUserAvatarUrl", avatarUrl)
        );
        
        return "pages/ask";
    }

    @PostMapping("/ask")
    public String createPost(@ModelAttribute NewPostForm postForm, Model model) {
        // Check if user's session is active and logged in
        if (!sessionService.isAuthenticated()) {
            return "redirect:/login?next=/ask&msg=To ask a question, please log in.";
        }

        var clientOpt = sessionService.getCurrentClient();
        if (clientOpt.isEmpty()) {
            return "redirect:/login?next=/ask&msg=To ask a question, please log in.";
        }

        try {
            AtprotoClient client = clientOpt.get();
            
            // Get the DID from the authenticated session handle
            String handle = client.getSession().getHandle();
            var profile = AtprotoUtil.getBskyProfile(handle);
            String userDid = profile.get("did").toString().replace("\"", ""); // Remove quotes from JSON string
            
            // Generate a unique record key for the post
            String recordKey = UUID.randomUUID().toString().replace("-", "");
            
            // Create AtUri for the post
            AtUri postAtUri = new AtUri(userDid, "dev.fudgeu.experimental.atforumv1.feed.post", recordKey);
            
            // Prepare tags list
            var tags = postForm.getTags() != null ? postForm.getTags() : new ArrayList<String>();
            
            // Insert post into database table
            dsl.insertInto(POST)
                .set(POST.ATURI, postAtUri.toString())
                .set(POST.TITLE, postForm.getTitle())
                .set(POST.CONTENT, postForm.getContent())
                .set(POST.TAGS, JSON.valueOf(Json.of(tags, Json::of).toString()))
                .set(POST.STATUS, "new")
                .set(POST.OWNER_DID, userDid)
                .set(POST.CREATED_AT, OffsetDateTime.now())
                .set(POST.IS_OPEN, true)
                .set(POST.IS_DELETED, false)
                .execute();
            
            // Redirect to the newly created post
            return "redirect:/ask";
            
        } catch (IOException e) {
            System.out.println("IOException while creating post: " + e.getMessage());
            model.addAttribute("error", "Failed to create post. Please try again.");
            model.addAttribute("postForm", postForm);
            
            // Add current user's avatar URL to model for pageHeader
            getCurrentUserAvatarUrl().ifPresent(avatarUrl -> 
                model.addAttribute("currentUserAvatarUrl", avatarUrl)
            );
            
            return "pages/ask";
        } catch (Exception e) {
            System.out.println("IOException while creating post: " + e.getMessage());
            model.addAttribute("error", "An unexpected error occurred. Please try again.");
            model.addAttribute("postForm", postForm);
            
            // Add current user's avatar URL to model for pageHeader
            getCurrentUserAvatarUrl().ifPresent(avatarUrl -> 
                model.addAttribute("currentUserAvatarUrl", avatarUrl)
            );
            
            return "pages/ask";
        }
    }
    
    /**
     * Helper method to get the current authenticated user's avatar URL
     */
    private java.util.Optional<String> getCurrentUserAvatarUrl() {
        if (!sessionService.isAuthenticated()) {
            return java.util.Optional.empty();
        }
        
        var clientOpt = sessionService.getCurrentClient();
        if (clientOpt.isEmpty()) {
            return java.util.Optional.empty();
        }
        
        try {
            AtprotoClient client = clientOpt.get();
            String handle = client.getSession().getHandle();
            
            // Get the DID from the profile
            var profile = AtprotoUtil.getBskyProfile(handle);
            String userDid = field(profile, "did", string());
            
            // Query the database for the user's avatar URL
            var userRecord = dsl.selectFrom(USER)
                    .where(USER.DID.eq(userDid))
                    .fetchOne();
            
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
