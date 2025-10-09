package com.jcs.javacommunitysite.homepage;

import com.jcs.javacommunitysite.atproto.AtprotoClient;
import com.jcs.javacommunitysite.atproto.service.AtprotoSessionService;
import com.jcs.javacommunitysite.atproto.records.PostRecord;
import dev.mccue.json.Json;
import org.jooq.DSLContext;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.jcs.javacommunitysite.jooq.tables.CategoryGroup.CATEGORY_GROUP;
import static com.jcs.javacommunitysite.jooq.tables.Category.CATEGORY;

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

            Json postDataJson = Json.objectBuilder()
                    .put("", "")
                    .put("", "")
                    .put("", "")
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

            // Fetch all category groups with their categories
            var groupsWithCategories = dsl.select(
                            CATEGORY_GROUP.ID.as("group_id"),
                            CATEGORY_GROUP.NAME.as("group_name"),
                            CATEGORY_GROUP.DESCRIPTION.as("group_description"),
                            CATEGORY_GROUP.ATURI.as("group_aturi"),
                            CATEGORY_GROUP.CREATED_AT.as("group_created_at"),
                            CATEGORY_GROUP.UPDATED_AT.as("group_updated_at"),
                            CATEGORY.ID.as("category_id"),
                            CATEGORY.NAME.as("category_name"),
                            CATEGORY.ATURI.as("category_aturi")
                    ).from(CATEGORY_GROUP)
                    .leftJoin(CATEGORY).on(CATEGORY.CATEGORY_GROUP_ID.eq(CATEGORY_GROUP.ID))
                    .orderBy(CATEGORY_GROUP.NAME.asc(), CATEGORY.NAME.asc())
                    .fetch();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

            // Group the results by category group
            var groupedData = groupsWithCategories.stream()
                    .collect(Collectors.groupingBy(
                            record -> {
                                OffsetDateTime createdAt = record.get("group_created_at", OffsetDateTime.class);
                                OffsetDateTime updatedAt = record.get("group_updated_at", OffsetDateTime.class);

                                return Map.of(
                                        "name", Objects.requireNonNull(record.get("group_name")),
                                        "description", Objects.requireNonNull(record.get("group_description")),
                                        "aturi", Objects.requireNonNull(record.get("group_aturi")),
                                        "created_at", createdAt.format(formatter),
                                        "updated_at", updatedAt.format(formatter)
                                );
                            },
                            Collectors.mapping(
                                    record -> {
                                        var categoryId = record.get("category_id");
                                        if (categoryId != null) {
                                            return Map.of(
                                                    "name", Objects.requireNonNull(record.get("category_name")),
                                                    "aturi", Objects.requireNonNull(record.get("category_aturi"))
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