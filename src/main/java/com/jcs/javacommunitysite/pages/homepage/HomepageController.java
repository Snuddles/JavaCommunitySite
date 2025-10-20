package com.jcs.javacommunitysite.pages.homepage;

import com.jcs.javacommunitysite.atproto.AtprotoClient;
import com.jcs.javacommunitysite.atproto.service.AtprotoSessionService;
import org.jooq.DSLContext;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.jcs.javacommunitysite.jooq.tables.Category.CATEGORY;
import static com.jcs.javacommunitysite.jooq.tables.Group.GROUP;

@RestController
public class HomepageController {

    private final AtprotoSessionService sessionService;
    private final DSLContext dsl;

    public HomepageController(AtprotoSessionService sessionService, DSLContext dsl) {
        this.sessionService = sessionService;
        this.dsl = dsl;
    }

    @GetMapping("/groups/categories")
    public List<Map<String, Object>> getGroupsCategories() {

        try {
            Optional<AtprotoClient> clientOpt = sessionService.getCurrentClient();
            if (clientOpt.isEmpty()) {
                return Collections.emptyList();
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
            return groupedData.entrySet().stream()
                    .map(entry -> {
                        var group = entry.getKey();
                        var categories = entry.getValue();
                        return Map.of(
                                "group", group,
                                "categories", categories
                        );
                    })
                    .toList();

        } catch (Exception e){
            return Collections.emptyList();
        }
    }
}