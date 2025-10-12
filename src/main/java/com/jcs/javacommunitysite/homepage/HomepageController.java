package com.jcs.javacommunitysite.homepage;

import com.jcs.javacommunitysite.JavaCommunitySiteApplication;
import com.jcs.javacommunitysite.atproto.AtprotoClient;
import com.jcs.javacommunitysite.atproto.service.AtprotoSessionService;
import com.jcs.javacommunitysite.atproto.records.PostRecord;
import dev.mccue.json.Json;
import org.jooq.DSLContext;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.jcs.javacommunitysite.jooq.tables.Category.CATEGORY;
import static com.jcs.javacommunitysite.jooq.tables.Group.GROUP;
import static com.jcs.javacommunitysite.JavaCommunitySiteApplication.JCS_FORUM_ATURI;

@RestController
@RequestMapping("/homepage")
public class HomepageController {

    private final AtprotoSessionService sessionService;
    private final DSLContext dsl;

    public HomepageController(AtprotoSessionService sessionService, DSLContext dsl) {
        this.sessionService = sessionService;
        this.dsl = dsl;
    }

    @PostMapping("/posts")
    public String createPost(Model model) {
        try {
            Optional<AtprotoClient> clientOpt = sessionService.getCurrentClient();

            if (clientOpt.isEmpty()) {
                return "error not authenticated";
            }

            AtprotoClient client = clientOpt.get();

            /*
                logic to fetch data from form into Json variable
            */

            Instant createdAt = Instant.now();
            Instant updatedAt = createdAt;

            Json postDataJson = Json.objectBuilder()
                    .put("title", "")
                    .put("content", "")
                    .put("createdAt", createdAt.toString())
                    .put("updatedAt", updatedAt.toString())
                    .put("category", "")
                    .put("forum", JCS_FORUM_ATURI)
                    .put("tags", "")
                    .toJson();

            PostRecord post = new PostRecord(postDataJson);
            client.createRecord(post);

            return "success new post";

        } catch (Exception e) {
            return "error";
        }
    }

    @GetMapping("/posts")
    public String getGroupsCategories(Model model) {

        try {
            Optional<AtprotoClient> clientOpt = sessionService.getCurrentClient();
            if (clientOpt.isEmpty()) {
                return "error not authenticated";
            }

            // Fetch all groups with their categories
            var groupsWithCategories = dsl.select(
                            GROUP.NAME.as("group_name"),
                            GROUP.DESCRIPTION.as("group_description"),
                            GROUP.ATURI.as("group_aturi"),
                            CATEGORY.NAME.as("category_name"),
                            CATEGORY.ATURI.as("category_aturi"),
                            CATEGORY.CATEGORY_TYPE.as("category_type"),
                            CATEGORY.DESCRIPTION.as("category_description")
                    ).from(GROUP)
                    .leftJoin(CATEGORY).on(CATEGORY.GROUP.eq(GROUP.ATURI))
                    .orderBy(GROUP.NAME.asc(), CATEGORY.NAME.asc())
                    .fetch();

            // Group the results by group
            var groupedData = groupsWithCategories.stream()
                    .collect(Collectors.groupingBy(
                            record -> Map.of(
                                    "name", Objects.requireNonNull(record.get("group_name")),
                                    "description", Objects.requireNonNull(record.get("group_description")),
                                    "aturi", Objects.requireNonNull(record.get("group_aturi"))
                            ),
                            Collectors.mapping(
                                    record -> {
                                        var categoryName = record.get("category_name");
                                        if (categoryName != null) {
                                            return Map.of(
                                                    "name", Objects.requireNonNull(record.get("category_name")),
                                                    "aturi", Objects.requireNonNull(record.get("category_aturi")),
                                                    "category_type", record.get("category_type") != null ? record.get("category_type") : "",
                                                    "description", record.get("category_description") != null ? record.get("category_description") : ""
                                            );
                                        }
                                        return null;
                                    },
                                    Collectors.filtering(
                                            Objects::nonNull,
                                            Collectors.toList()
                                    )
                            )
                    ));

            // Convert to a more structured format
            var result = groupedData.entrySet().stream()
                    .map(entry -> {
                        var group = entry.getKey();
                        var categories = entry.getValue();
                        return Map.of(
                                "group", group,
                                "categories", categories
                        );
                    })
                    .toList();

            return "group/category template";

        } catch (Exception e){
            return "error";
        }
    }
}