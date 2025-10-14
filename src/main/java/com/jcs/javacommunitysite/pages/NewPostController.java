package com.jcs.javacommunitysite.pages;

import com.jcs.javacommunitysite.atproto.AtUri;
import com.jcs.javacommunitysite.atproto.AtprotoClient;
import com.jcs.javacommunitysite.atproto.exceptions.AtprotoInvalidUri;
import com.jcs.javacommunitysite.atproto.records.PostRecord;
import com.jcs.javacommunitysite.atproto.service.AtprotoSessionService;
import com.jcs.javacommunitysite.forms.NewPostForm;
import org.jooq.DSLContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.jcs.javacommunitysite.JavaCommunitySiteApplication.JCS_FORUM_DID;
import static com.jcs.javacommunitysite.jooq.tables.Category.CATEGORY;
import static com.jcs.javacommunitysite.jooq.tables.Group.GROUP;

@Controller
public class NewPostController {

    private final AtprotoSessionService sessionService;
    private final DSLContext dsl;

    public NewPostController(AtprotoSessionService sessionService, DSLContext dsl) {
        this.sessionService = sessionService;
        this.dsl = dsl;
    }


    @GetMapping("/newPost")
    public String newPost(Model model) {
        // Kick to login screen if not logged in
        if (!sessionService.isAuthenticated()) {
            return "redirect:/login?next=/newPost&msg=To create a post, please log in.";
        }

        model.addAttribute("groups", getGroups());
        model.addAttribute("postForm", new NewPostForm());
        return "newpost";
    }

    @PostMapping("/newPost")
    public String newPost(@ModelAttribute NewPostForm newPostForm, Model model) {
        try {
            Optional<AtprotoClient> clientOpt = sessionService.getCurrentClient();

            if (clientOpt.isEmpty() || !sessionService.isAuthenticated()) {
                return "redirect:/login?next=/newPost";
            }

            // Attempt to make aturi from category
            AtUri category = null;
            try {
                category = new AtUri(newPostForm.getCategory());
            } catch (AtprotoInvalidUri e) {
                System.out.println(e);
                model.addAttribute("error", "Invalid category - please select another one"); // TODO implement errors
                model.addAttribute("groups", getGroups());
                model.addAttribute("postForm", newPostForm);
                return "newpost";
            }

            AtprotoClient client = clientOpt.get();

            PostRecord post = new PostRecord(
                newPostForm.getTitle(),
                newPostForm.getContent(),
                category,
                JCS_FORUM_DID
            );
            client.createRecord(post);

            return "redirect:/browse";

        } catch (Exception e) {
            return "error";
        }
    }

    @GetMapping("/newPost/htmx/getCategories")
    public String getCategories(Model model, @RequestParam String group) {
        var categories = dsl.select(
                    CATEGORY.NAME.as("category_name"),
                    CATEGORY.ATURI.as("category_aturi")
            ).from(CATEGORY).where(CATEGORY.GROUP.eq(group))
            .orderBy(CATEGORY.NAME.asc())
            .fetch();

        var categoryData = categories.stream()
            .map(record -> Map.of(
                    "name", record.get("category_name"),
                    "aturi", record.get("category_aturi")
            ))
            .toList();

        model.addAttribute("categories", categoryData);
        return "newpost_categoryoptions";
    }

    private List<Map<String, String>> getGroups() {
        var groups = dsl.select(
                        GROUP.NAME.as("group_name"),
                        GROUP.ATURI.as("group_aturi")
                ).from(GROUP)
                .orderBy(GROUP.NAME.asc())
                .fetch();


        var categoryGroups = groups.stream()
                .map(record -> Map.of(
                        "name", record.get("group_name").toString(),
                        "id", record.get("group_aturi").toString()
                ))
                .toList();

        return categoryGroups;
    }
}
