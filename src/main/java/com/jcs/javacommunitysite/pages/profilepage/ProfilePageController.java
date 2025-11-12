package com.jcs.javacommunitysite.pages.profilepage;

import com.jcs.javacommunitysite.atproto.service.AtprotoSessionService;
import com.jcs.javacommunitysite.pages.askpage.NewPostForm;
import com.jcs.javacommunitysite.util.TimeUtil;
import com.jcs.javacommunitysite.util.UserInfo;
import dev.mccue.json.Json;
import org.jooq.DSLContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jcs.javacommunitysite.jooq.tables.Post.POST;
import static com.jcs.javacommunitysite.jooq.tables.Reply.REPLY;
import static dev.mccue.json.JsonDecoder.*;


@Controller
public class ProfilePageController {
    private final AtprotoSessionService sessionService;
    private final DSLContext dsl;

    public ProfilePageController(AtprotoSessionService sessionService, DSLContext dsl) {
        this.sessionService = sessionService;
        this.dsl = dsl;
    }

    @GetMapping("/pfp/tab/questions")
    public String q(@RequestParam String did, @RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        model.addAttribute("postForm", new NewPostForm());
        int pageSize = 20;
        try {
            // Total count
            int totalPosts = dsl.selectCount()
                    .from(POST)
                    .where(POST.OWNER_DID.eq(did))
                    .and(POST.IS_DELETED.eq(false))
                    .fetchOne(0, int.class);

            // Query paged user's posts from database
            var userPosts = dsl.selectFrom(POST)
                    .where(POST.OWNER_DID.eq(did))
                    .and(POST.IS_DELETED.eq(false))
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
                timeTextsMap.put(post.getAturi(), TimeUtil.calculateTimeText(post.getCreatedAt()));

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

            boolean hasMore = totalPosts > (page * pageSize);

            model.addAttribute("userPosts", userPosts);
            model.addAttribute("replyCountsMap", replyCountsMap);
            model.addAttribute("timeTextsMap", timeTextsMap);
            model.addAttribute("tagsMap", tagsMap);
            model.addAttribute("hasMoreUserPosts", hasMore);
            model.addAttribute("nextPageUserPosts", page + 1);
            model.addAttribute("did", did);
        } catch (Exception e) {
            System.err.println("Error fetching user posts: " + e.getMessage());
        }

        // Use a single template for both initial and subsequent loads
        return "pages/pfp/tab/questions";
    }

    @GetMapping("/pfp/tab/answers")
    public String a(@RequestParam String did, @RequestParam(name = "page", defaultValue = "1") int page, Model m) {
        int pageSize = 20;
        try {
            int totalReplies = dsl.selectCount()
                    .from(REPLY)
                    .where(REPLY.OWNER_DID.eq(did))
                    .fetchOne(0, int.class);

            var userReplies = dsl.selectFrom(REPLY)
                .where(REPLY.OWNER_DID.eq(did))
                .orderBy(REPLY.CREATED_AT.desc())
                .limit(pageSize)
                .offset((page - 1) * pageSize)
                .fetch();

            var replies = new ArrayList<String>();
            var replyData = new HashMap<String, Map<String, String>>();
            var postData = new HashMap<String, Map<String, String>>();

            for (var reply : userReplies) {
                replies.add(reply.getAturi());

                var replyMap = new HashMap<String, String>();
                replyMap.put("aturi", reply.getAturi());
                replyMap.put("content", reply.getContent());
                replyMap.put("rootPostAturi", reply.getRootPostAturi());
                replyMap.put("createdAt", reply.getCreatedAt().toString());
                replyMap.put("ownerDid", reply.getOwnerDid());
                replyData.put(reply.getAturi(), replyMap);

                var originalPost = dsl.selectFrom(POST)
                        .where(POST.ATURI.eq(reply.getRootPostAturi()))
                        .fetchOne();

                if (originalPost != null) {
                    var postMap = new HashMap<String, String>();
                    postMap.put("aturi", originalPost.getAturi());
                    postMap.put("title", originalPost.getTitle());
                    postMap.put("content", originalPost.getContent());
                    postMap.put("createdAt", originalPost.getCreatedAt().toString());
                    postMap.put("ownerDid", originalPost.getOwnerDid());
                    postMap.put("status", originalPost.getStatus());
                    postMap.put("isOpen", originalPost.getIsOpen().toString());
                    postMap.put("isDeleted", originalPost.getIsDeleted().toString());

                    if (originalPost.getTags() != null) {
                        postMap.put("tags", originalPost.getTags().data());
                    } else {
                        postMap.put("tags", "[]");
                    }

                    postData.put(reply.getAturi(), postMap);
                }
            }

            boolean hasMore = totalReplies > (page * pageSize);

            m.addAttribute("replies", replies);
            m.addAttribute("replyData", replyData);
            m.addAttribute("postData", postData);
            m.addAttribute("hasMoreUserAnswers", hasMore);
            m.addAttribute("nextPageUserAnswers", page + 1);
            m.addAttribute("did", did);

            return "pages/pfp/tab/answers";
        } catch (Exception e) {
            System.err.println("Error fetching user replies: " + e.getMessage());
            return "";
        }
    }


    @GetMapping("/pfp")
    public String pfpPage(Model model) {
        // Login check
        if (!sessionService.isAuthenticated()) {
            return "redirect:/login?next=/pfp&msg=To view your profile, please log in.";
        }

        // Fetch self user info
        var user = UserInfo.getSelfFromDb(dsl, sessionService);
        model.addAttribute("loggedInUser", user);
        model.addAttribute("user", user);

        return "pages/pfp";
    }

    @GetMapping("/pfp/{did}")
    public String pfpPageOtherUser(Model model, @PathVariable String did) {
        var user = UserInfo.getFromDb(dsl, sessionService, did);
        var loggedInUser = UserInfo.getSelfFromDb(dsl, sessionService);
        model.addAttribute("loggedInUser", loggedInUser);
        model.addAttribute("user", user);
        return "pages/pfp";
    }

}
