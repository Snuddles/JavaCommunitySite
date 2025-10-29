package com.jcs.javacommunitysite.pages;
import com.jcs.javacommunitysite.atproto.AtprotoClient;
import com.jcs.javacommunitysite.atproto.AtprotoUtil;
import com.jcs.javacommunitysite.atproto.service.AtprotoSessionService;
import com.jcs.javacommunitysite.pages.askpage.NewPostForm;
import dev.mccue.json.Json;
import org.jooq.DSLContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;

import static com.jcs.javacommunitysite.jooq.tables.Post.POST;
import static com.jcs.javacommunitysite.jooq.tables.Reply.REPLY;
import static com.jcs.javacommunitysite.jooq.tables.User.USER;
import static dev.mccue.json.JsonDecoder.*;
import com.jcs.javacommunitysite.atproto.records.QuestionRecord;

@Controller
public class PageController {
    private final AtprotoSessionService sessionService;
    private final DSLContext dsl;

    public PageController(AtprotoSessionService sessionService, DSLContext dsl) {
        this.sessionService = sessionService;
        this.dsl = dsl;
    }

    @GetMapping("/groups/{groupName}")
    public String groupPage(@PathVariable String groupName, Model model) {
        model.addAttribute("groupName", groupName);
        return "pages/groupCategories";
    }
    @GetMapping("/pfp/tab/questions") public String q(Model model){
        // Fetch user's posts if authenticated
        model.addAttribute("postForm", new NewPostForm());

        // Add current user's avatar URL to model for pageHeader
        getCurrentUserAvatarUrl().ifPresent(avatarUrl ->
                model.addAttribute("currentUserAvatarUrl", avatarUrl)
        );

        // Fetch user's posts if authenticated
        if (sessionService.isAuthenticated()) {
            var clientOpt = sessionService.getCurrentClient();
            if (clientOpt.isPresent()) {
                try {
                    AtprotoClient client = clientOpt.get();
                    String handle = client.getSession().getHandle();
                    var profile = AtprotoUtil.getBskyProfile(handle);
                    String userDid = profile.get("did").toString().replace("\"", "");

                    // Query user's posts from database
                    var userPosts = dsl.selectFrom(POST)
                            .where(POST.OWNER_DID.eq(userDid))
                            .and(POST.IS_DELETED.eq(false))
                            .orderBy(POST.CREATED_AT.desc())
                            .fetch();

                    // Create a map of post ATURI to reply count
                    var replyCountsMap = new java.util.HashMap<String, Integer>();
                    var timeTextsMap = new java.util.HashMap<String, String>();
                    var tagsMap = new java.util.HashMap<String, java.util.List<String>>();

                    for (var post : userPosts) {
                        // Calculate reply count
                        int replyCount = dsl.selectCount()
                                .from(REPLY)
                                .where(REPLY.ROOT_POST_ATURI.eq(post.getAturi()))
                                .fetchOne(0, int.class);
                        replyCountsMap.put(post.getAturi(), replyCount);

                        // Calculate time text
                        var now = OffsetDateTime.now();
                        var createdAt = post.getCreatedAt();
                        var yearsBetween = java.time.temporal.ChronoUnit.YEARS.between(createdAt, now);
                        var daysBetween = java.time.temporal.ChronoUnit.DAYS.between(createdAt, now);
                        var hoursBetween = java.time.temporal.ChronoUnit.HOURS.between(createdAt, now);
                        var minutesBetween = java.time.temporal.ChronoUnit.MINUTES.between(createdAt, now);

                        String timeText;
                        if (yearsBetween > 0) {
                            timeText = yearsBetween + (yearsBetween == 1 ? " year ago" : " years ago");
                        } else if (daysBetween > 0) {
                            timeText = daysBetween + (daysBetween == 1 ? " day ago" : " days ago");
                        } else if (hoursBetween > 0) {
                            timeText = hoursBetween + (hoursBetween == 1 ? " hour ago" : " hours ago");
                        } else if (minutesBetween > 0) {
                            timeText = minutesBetween + (minutesBetween == 1 ? " minute ago" : " minutes ago");
                        } else {
                            timeText = "Just now";
                        }
                        timeTextsMap.put(post.getAturi(), timeText);

                        // Extract tags from JSON
                        var tagsList = new ArrayList<String>();
                        if (post.getTags() != null) {
                            try {
                                // Parse JSON array using dev.mccue.json
                                var tagsJson = Json.read(post.getTags().data());
                                var tagsArray = array(string()).decode(tagsJson);
                                tagsList.addAll(tagsArray);
                            } catch (Exception e) {
                                System.err.println("Error parsing tags JSON for post " + post.getAturi() + ": " + e.getMessage());
                            }
                        }
                        tagsMap.put(post.getAturi(), tagsList);
                    }

                    model.addAttribute("userPosts", userPosts);
                    model.addAttribute("replyCountsMap", replyCountsMap);
                    model.addAttribute("timeTextsMap", timeTextsMap);
                    model.addAttribute("tagsMap", tagsMap);
                } catch (IOException e) {
                    System.err.println("Error fetching user posts: " + e.getMessage());
                }
            }
        }
        return "components/userQuestions";
    }

    @GetMapping("/pfp/tab/answers")   public String a(Model m){
        return "components/userAnswers";
    }


    @GetMapping("/pfp")
    public String pfpPage(Model model) {
        model.addAttribute("postForm", new NewPostForm());

        // Add current user's avatar URL to model for pageHeader
        getCurrentUserAvatarUrl().ifPresent(avatarUrl ->
                model.addAttribute("currentUserAvatarUrl", avatarUrl)
        );

        // Fetch user's posts if authenticated
        if (sessionService.isAuthenticated()) {
            var clientOpt = sessionService.getCurrentClient();
            if (clientOpt.isPresent()) {
                try {
                    AtprotoClient client = clientOpt.get();
                    String handle = client.getSession().getHandle();
                    var profile = AtprotoUtil.getBskyProfile(handle);

                    model.addAttribute("currentUserHandle", handle);
                    model.addAttribute("displayName", profile.get("displayName").toString());
                    model.addAttribute("description", profile.get("description").toString());
                } catch (IOException e) {
                    System.err.println("Error fetching user posts: " + e.getMessage());
                }
            }
        }

        return "pages/pfp";
    }

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
