package com.jcs.javacommunitysite.pages.askpage;

import com.jcs.javacommunitysite.JavaCommunitySiteApplication;
import com.jcs.javacommunitysite.atproto.AtUri;
import com.jcs.javacommunitysite.atproto.records.QuestionRecord;
import com.jcs.javacommunitysite.util.UserInfo;

import jakarta.servlet.http.HttpServletResponse;

import org.jooq.DSLContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jcs.javacommunitysite.atproto.service.AtprotoSessionService;
import com.jcs.javacommunitysite.atproto.AtprotoClient;
import com.jcs.javacommunitysite.atproto.AtprotoUtil;

import dev.mccue.json.Json;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Optional;
import java.time.temporal.ChronoUnit;

import static com.jcs.javacommunitysite.jooq.tables.HiddenPost.HIDDEN_POST;
import static com.jcs.javacommunitysite.jooq.tables.Post.POST;
import static com.jcs.javacommunitysite.jooq.tables.Tags.TAGS;
import static com.jcs.javacommunitysite.jooq.tables.User.USER;
import static com.jcs.javacommunitysite.jooq.tables.Reply.REPLY;

import static dev.mccue.json.JsonDecoder.field;
import static dev.mccue.json.JsonDecoder.string;
import static dev.mccue.json.JsonDecoder.array;

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

        int pageSize = 20;
        int page = 1;
        
        // Fetch user's posts if authenticated
        if (sessionService.isAuthenticated()) {
            var clientOpt = sessionService.getCurrentClient();
            if (clientOpt.isPresent()) {
                try {
                    AtprotoClient client = clientOpt.get();
                    String handle = client.getSession().getHandle();
                    var profile = AtprotoUtil.getBskyProfile(handle);
                    String userDid = profile.get("did").toString().replace("\"", "");
                    
                    // Total count
                    int totalPosts = dsl.selectCount()
                            .from(POST)
                            .where(POST.OWNER_DID.eq(userDid))
                            .and(POST.IS_DELETED.eq(false))
                            .andNotExists(
                                dsl.selectOne()
                                    .from(HIDDEN_POST)
                                    .where(HIDDEN_POST.POST_ATURI.eq(POST.ATURI))
                            )
                            .fetchOne(0, int.class);

                    // Query first page of user's posts from database
                    var userPosts = dsl.selectFrom(POST)
                            .where(POST.OWNER_DID.eq(userDid))
                            .and(POST.IS_DELETED.eq(false))
                            .andNotExists(
                                dsl.selectOne()
                                    .from(HIDDEN_POST)
                                    .where(HIDDEN_POST.POST_ATURI.eq(POST.ATURI))
                            )
                            .orderBy(POST.CREATED_AT.desc())
                            .limit(pageSize)
                            .offset((page - 1) * pageSize)
                            .fetch();
                    
                    // Create a map of post ATURI to reply count
                    var replyCountsMap = new HashMap<String, Integer>();
                    var timeTextsMap = new HashMap<String, String>();
                    var tagsMap = new HashMap<String, List<String>>();

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
                        var yearsBetween = ChronoUnit.YEARS.between(createdAt, now);
                        var daysBetween = ChronoUnit.DAYS.between(createdAt, now);
                        var hoursBetween = ChronoUnit.HOURS.between(createdAt, now);
                        var minutesBetween = ChronoUnit.MINUTES.between(createdAt, now);
                        
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

                    // Get all tags
                    var tags = dsl.select(TAGS.ATURI, TAGS.TAG_NAME)
                            .from(TAGS)
                            .fetchMap(TAGS.ATURI, TAGS.TAG_NAME);

                    boolean hasMore = totalPosts > pageSize;

                    model.addAttribute("userPosts", userPosts);
                    model.addAttribute("tags", tags);
                    model.addAttribute("replyCountsMap", replyCountsMap);
                    model.addAttribute("timeTextsMap", timeTextsMap);
                    model.addAttribute("postTags", tagsMap);
                    model.addAttribute("hasMoreUserPosts", hasMore);
                    model.addAttribute("nextPageUserPosts", 2);
                    model.addAttribute("loggedIn", true);
                    model.addAttribute("user", UserInfo.getSelfFromDb(dsl, sessionService));
                } catch (IOException e) {
                    System.err.println("Error fetching user posts: " + e.getMessage());
                }
            }
        } else {
            model.addAttribute("loggedIn", false);
        }
        
        return "pages/ask";
    }

    @GetMapping("/ask/htmx/myPosts")
    public String getMoreMyPosts(Model model,
                                 HttpServletResponse response,
                                 @RequestParam int page) {
        if (!sessionService.isAuthenticated()) {
            return ""; // not logged in, nothing to load
        }
        var clientOpt = sessionService.getCurrentClient();
        if (clientOpt.isEmpty()) {
            return "";
        }
        try {
            int pageSize = 20;
            AtprotoClient client = clientOpt.get();
            String handle = client.getSession().getHandle();
            var profile = AtprotoUtil.getBskyProfile(handle);
            String userDid = profile.get("did").toString().replace("\"", "");

            var totalPosts = dsl.selectCount()
                    .from(POST)
                    .where(POST.OWNER_DID.eq(userDid))
                    .and(POST.IS_DELETED.eq(false))
                    .andNotExists(
                            dsl.selectOne()
                                    .from(HIDDEN_POST)
                                    .where(HIDDEN_POST.POST_ATURI.eq(POST.ATURI))
                    )
                    .fetchOne(0, int.class);

            var userPosts = dsl.selectFrom(POST)
                    .where(POST.OWNER_DID.eq(userDid))
                    .and(POST.IS_DELETED.eq(false))
                    .andNotExists(
                            dsl.selectOne()
                                    .from(HIDDEN_POST)
                                    .where(HIDDEN_POST.POST_ATURI.eq(POST.ATURI))
                    )
                    .orderBy(POST.CREATED_AT.desc())
                    .limit(pageSize)
                    .offset((page - 1) * pageSize)
                    .fetch();

            var replyCountsMap = new HashMap<String, Integer>();
            var timeTextsMap = new HashMap<String, String>();
            var tagsMap = new HashMap<String, List<String>>();

            for (var post : userPosts) {
                int replyCount = dsl.selectCount()
                        .from(REPLY)
                        .where(REPLY.ROOT_POST_ATURI.eq(post.getAturi()))
                        .fetchOne(0, int.class);
                replyCountsMap.put(post.getAturi(), replyCount);

                var now = OffsetDateTime.now();
                var createdAt = post.getCreatedAt();
                var yearsBetween = ChronoUnit.YEARS.between(createdAt, now);
                var daysBetween = ChronoUnit.DAYS.between(createdAt, now);
                var hoursBetween = ChronoUnit.HOURS.between(createdAt, now);
                var minutesBetween = ChronoUnit.MINUTES.between(createdAt, now);

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

                var tagsList = new ArrayList<String>();
                if (post.getTags() != null) {
                    try {
                        var tagsJson = Json.read(post.getTags().data());
                        var tagsArray = array(string()).decode(tagsJson);
                        tagsList.addAll(tagsArray);
                    } catch (Exception ignored) {}
                }
                tagsMap.put(post.getAturi(), tagsList);
            }

            boolean hasMore = totalPosts > (page * pageSize);

            model.addAttribute("userPosts", userPosts);
            model.addAttribute("replyCountsMap", replyCountsMap);
            model.addAttribute("timeTextsMap", timeTextsMap);
            model.addAttribute("postTags", tagsMap);
            model.addAttribute("hasMoreUserPosts", hasMore);
            model.addAttribute("nextPageUserPosts", page + 1);

            return "pages/ask/htmx/myPosts";
        } catch (Exception e) {
            return "";
        }
    }

    @PostMapping("/ask")
    public String createPost(@ModelAttribute NewPostForm postForm, HttpServletResponse response, Model model) {
        // Check if user's session is active and logged in
        var clientOpt = sessionService.getCurrentClient();
        if (!sessionService.isAuthenticated() || clientOpt.isEmpty()) {
            response.setHeader("HX-Redirect", "/login?next=/ask&msg=To ask a question, please log in.");
            return "empty";
        }

        try {
            AtprotoClient client = clientOpt.get();
            
            // Prepare tags list
            var tags = postForm.getTags() != null ? postForm.getTags() : new ArrayList<String>();

            // Create ATproto Question record
            var questionRecord = new QuestionRecord(
                    postForm.getTitle(),
                    postForm.getContent(),
                    JavaCommunitySiteApplication.JCS_FORUM_DID,
                    tags
            );

            // Send to ATproto
            var atProtoResp = client.createRecord(questionRecord);

            model.addAttribute("aturi", new AtUri(field(atProtoResp, "uri", string())));
            model.addAttribute("title", postForm.getTitle());
            model.addAttribute("content", postForm.getContent());
            model.addAttribute("amtReplies", 0);
            model.addAttribute("timeText", "Just now");
            model.addAttribute("tags", tags);

            return "components/post";
            
        } catch (IOException e) {
            System.out.println("IOException while creating post: " + e.getMessage());
            model.addAttribute("error", "Failed to create post. Please try again.");
            model.addAttribute("postForm", postForm);
            
            return "pages/ask";
        } catch (Exception e) {
            System.out.println("IOException while creating post: " + e.getMessage());
            model.addAttribute("error", "An unexpected error occurred. Please try again.");
            model.addAttribute("postForm", postForm);
            
            return "pages/ask";
        }
    }
}
