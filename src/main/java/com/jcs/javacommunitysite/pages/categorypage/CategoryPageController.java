package com.jcs.javacommunitysite.pages.categorypage;

import com.jcs.javacommunitysite.atproto.AtUri;
import com.jcs.javacommunitysite.atproto.records.ForumCategoryRecord;
import com.jcs.javacommunitysite.atproto.service.AtprotoSessionService;
import com.jcs.javacommunitysite.jooq.tables.records.PostRecord;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Objects;

import static com.jcs.javacommunitysite.jooq.tables.Post.POST;
import static com.jcs.javacommunitysite.jooq.tables.Category.CATEGORY;
import static com.jcs.javacommunitysite.JavaCommunitySiteApplication.JCS_FORUM_DID;

@Controller
public class CategoryPageController {

    private final AtprotoSessionService sessionService;
    private final DSLContext dsl;

    public CategoryPageController(AtprotoSessionService sessionService, DSLContext dsl) {
        this.sessionService = sessionService;
        this.dsl = dsl;
    }

    @GetMapping ("/{group}/{categoryRKey}")
    public String getCategoryPosts(@PathVariable String group, @PathVariable String categoryRKey, Model model) {

        try {
            var clientOpt = sessionService.getCurrentClient();

            if (!sessionService.isAuthenticated() || clientOpt.isEmpty()) {
                return "redirect:/";
            }

            AtUri categoryAtUri = new AtUri(JCS_FORUM_DID, ForumCategoryRecord.recordCollection, categoryRKey);

            String categoryName = dsl.select(CATEGORY.NAME)
                    .from(CATEGORY)
                    .where(CATEGORY.ATURI.eq(categoryAtUri.toString()))
                    .fetchOne(CATEGORY.NAME);

            List<PostRecord> allPostsInCategory = dsl.selectFrom(POST).where(POST.CATEGORY_ATURI.eq(categoryAtUri.toString())).fetch();

            model.addAttribute("allPostsInCategory", allPostsInCategory);

            group = group.replaceAll("_", " ");
            group = toTitleCase(group);
            model.addAttribute("groupName", group);

            categoryName = Objects.requireNonNull(categoryName).replaceAll("_", " ");
            categoryName = toTitleCase(categoryName);
            model.addAttribute("categoryName", categoryName);

            return "pages/categoryPosts";
        } catch (DataAccessException e) {
            System.out.println(e.getMessage());
            return "redirect:/";
        }
    }

    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder titleCase = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : input.toCharArray()) {
            if (Character.isWhitespace(c)) {
                titleCase.append(c);
                capitalizeNext = true;
            } else if (capitalizeNext) {
                titleCase.append(Character.toTitleCase(c));
                capitalizeNext = false;
            } else {
                titleCase.append(Character.toLowerCase(c));
            }
        }
        return titleCase.toString();
    }
}
