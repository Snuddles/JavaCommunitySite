package com.jcs.javacommunitysite.homepage;

import com.jcs.javacommunitysite.atproto.AtprotoClient;
import com.jcs.javacommunitysite.atproto.service.AtprotoSessionService;
import com.jcs.javacommunitysite.atproto.records.PostRecord;
import dev.mccue.json.Json;
import org.jooq.DSLContext;

import static com.jcs.javacommunitysite.jooq.tables.Category.CATEGORY;
import static com.jcs.javacommunitysite.jooq.tables.Group.GROUP;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/homepagebe")
public class HomepageBEController {

    private final AtprotoSessionService sessionService;
    private final DSLContext dsl;

    public HomepageBEController(AtprotoSessionService sessionService, DSLContext dsl) {
        this.sessionService = sessionService;
        this.dsl = dsl;
    }

    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@RequestBody Json postData) {
        try {
            Optional<AtprotoClient> clientOpt = sessionService.getCurrentClient();
            if (clientOpt.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Not authenticated"
                ));
            }

            AtprotoClient client = clientOpt.get();

            PostRecord post = new PostRecord(postData);

            client.createRecord(post);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Post created successfully",
                    "atUri", Objects.requireNonNull(post.getAtUri().toString())
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error creating post: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/posts")
    public ResponseEntity<?> getGroupsCategories() {

        try {
            Optional<AtprotoClient> clientOpt = sessionService.getCurrentClient();
            if (clientOpt.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Not authenticated"
                ));
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

            return ResponseEntity.status(200).body(Map.of(
                    "success", true,
                    "data", result));

        } catch (Exception e){
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error fetching data: " + e.getMessage()
            ));
        }
    }
}