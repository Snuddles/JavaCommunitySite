package com.jcs.javacommunitysite.pages.searchpage;

import org.jooq.DSLContext;
import org.jooq.Condition;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.jcs.javacommunitysite.atproto.service.AtprotoSessionService;
import dev.mccue.json.Json;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.jcs.javacommunitysite.jooq.tables.Post.POST;
import static com.jcs.javacommunitysite.jooq.tables.Reply.REPLY;
import static com.jcs.javacommunitysite.jooq.tables.User.USER;
import static dev.mccue.json.JsonDecoder.array;
import static dev.mccue.json.JsonDecoder.string;

@Controller
public class SearchPageController {

    private final AtprotoSessionService sessionService;
    private final DSLContext dsl;

    public SearchPageController(AtprotoSessionService sessionService, DSLContext dsl) {
        this.sessionService = sessionService;
        this.dsl = dsl;
    }

    @GetMapping("/search")
    public String search(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "sort", required = false) String sortBy,
            Model model
    ) {
        SearchForm searchForm = new SearchForm();
        searchForm.setQuery(query != null ? query : "");
        searchForm.setStatus(status != null ? status : "all");
        searchForm.setSortBy(sortBy != null ? sortBy : "relevance");

        model.addAttribute("searchForm", searchForm);

        getCurrentUserAvatarUrl().ifPresent(avatarUrl ->
            model.addAttribute("currentUserAvatarUrl", avatarUrl)
        );

        List<SearchResult> searchResults = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            searchResults = performSearch(query.trim(), status, sortBy);
        }

        model.addAttribute("searchResults", searchResults);
        model.addAttribute("hasResults", !searchResults.isEmpty());
        model.addAttribute("hasQuery", query != null && !query.trim().isEmpty());

        return "pages/search";
    }

    private List<SearchResult> performSearch(String query, String status, String sortBy) {
        try {
            final int LIMIT = 50;
            List<SearchResult> accumulated = new ArrayList<>();
            java.util.Set<String> seenAturis = new java.util.HashSet<>();

            Condition statusCondition = POST.IS_DELETED.eq(false);
            if (status != null && !status.equals("all")) {
                switch (status.toLowerCase()) {
                    case "open":
                        statusCondition = statusCondition.and(POST.IS_OPEN.eq(true));
                        break;
                    case "closed":
                        statusCondition = statusCondition.and(POST.IS_OPEN.eq(false));
                        break;
                }
            }

            String[] terms = query.toLowerCase().split("\\s+");

            // Progressive search: exact phrase → all words → any words (only if few results)
            Condition exact = POST.TITLE.likeIgnoreCase("%" + query + "%")
                    .or(POST.CONTENT.likeIgnoreCase("%" + query + "%"))
                    .or(POST.TAGS.cast(String.class).likeIgnoreCase("%" + query + "%"));

            var exactQuery = dsl.select(
                    POST.ATURI,
                    POST.TITLE,
                    POST.CONTENT,
                    POST.TAGS,
                    POST.CREATED_AT,
                    POST.OWNER_DID,
                    POST.STATUS,
                    POST.IS_DELETED,
                    POST.IS_OPEN,
                    dsl.selectCount().from(REPLY).where(REPLY.ROOT_POST_ATURI.eq(POST.ATURI)).asField("reply_count")
            ).from(POST)
            .where(statusCondition.and(exact));

            var exactRows = applySorting(exactQuery, sortBy, query).limit(LIMIT).fetch();

            for (var r : exactRows) {
                String aturi = r.get(POST.ATURI);
                if (seenAturis.add(aturi)) {
                    accumulated.add(mapRecordToSearchResult(r));
                }
            }

            if (accumulated.size() >= LIMIT) return accumulated.subList(0, LIMIT);

            if (terms.length > 0) {
                Condition allWords = null;
                for (String t : terms) {
                    Condition tc = POST.TITLE.likeIgnoreCase("%" + t + "%")
                            .or(POST.CONTENT.likeIgnoreCase("%" + t + "%"));
                    allWords = (allWords == null) ? tc : allWords.and(tc);
                }

                if (allWords != null) {
                    var allWordsQuery = dsl.select(
                            POST.ATURI,
                            POST.TITLE,
                            POST.CONTENT,
                            POST.TAGS,
                            POST.CREATED_AT,
                            POST.OWNER_DID,
                            POST.STATUS,
                            POST.IS_DELETED,
                            POST.IS_OPEN,
                            dsl.selectCount().from(REPLY).where(REPLY.ROOT_POST_ATURI.eq(POST.ATURI)).asField("reply_count")
                    ).from(POST)
                    .where(statusCondition.and(allWords));

                    var allRows = applySorting(allWordsQuery, sortBy, query).limit(LIMIT - accumulated.size()).fetch();

                    for (var r : allRows) {
                        String aturi = r.get(POST.ATURI);
                        if (seenAturis.add(aturi)) {
                            accumulated.add(mapRecordToSearchResult(r));
                        }
                    }
                }
            }

            if (accumulated.size() >= LIMIT) return accumulated.subList(0, Math.min(LIMIT, accumulated.size()));

            // Only broaden search if we have very few results to avoid noise
            if (terms.length > 0 && accumulated.size() < 5) {
                Condition anyWords = null;
                for (String t : terms) {
                    Condition tc = POST.TITLE.likeIgnoreCase("%" + t + "%")
                            .or(POST.CONTENT.likeIgnoreCase("%" + t + "%"));
                    anyWords = (anyWords == null) ? tc : anyWords.or(tc);
                }

                Condition tagMatch = null;
                for (String t : terms) {
                    Condition tt = POST.TAGS.cast(String.class).likeIgnoreCase("%" + t + "%");
                    tagMatch = (tagMatch == null) ? tt : tagMatch.or(tt);
                }

                Condition broaden = null;
                if (anyWords != null && tagMatch != null) broaden = anyWords.or(tagMatch);
                else if (anyWords != null) broaden = anyWords;
                else if (tagMatch != null) broaden = tagMatch;

                if (broaden != null) {
                    var broadQuery = dsl.select(
                            POST.ATURI,
                            POST.TITLE,
                            POST.CONTENT,
                            POST.TAGS,
                            POST.CREATED_AT,
                            POST.OWNER_DID,
                            POST.STATUS,
                            POST.IS_DELETED,
                            POST.IS_OPEN,
                            dsl.selectCount().from(REPLY).where(REPLY.ROOT_POST_ATURI.eq(POST.ATURI)).asField("reply_count")
                    ).from(POST)
                    .where(statusCondition.and(broaden));

                    if (!seenAturis.isEmpty()) {
                        broadQuery = broadQuery.and(POST.ATURI.notIn(seenAturis));
                    }

                    var anyRows = applySorting(broadQuery, sortBy, query).limit(LIMIT - accumulated.size()).fetch();
                    for (var r : anyRows) {
                        String aturi = r.get(POST.ATURI);
                        if (seenAturis.add(aturi)) {
                            accumulated.add(mapRecordToSearchResult(r));
                        }
                    }
                }
            }

            return accumulated.subList(0, Math.min(LIMIT, accumulated.size()));
        } catch (Exception e) {
            System.err.println("Error performing search: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private org.jooq.SelectLimitStep<?> applySorting(org.jooq.SelectConditionStep<?> query, String sortBy, String originalQuery) {
        return switch (sortBy != null ? sortBy.toLowerCase() : "relevance") {
            case "newest" -> query.orderBy(POST.CREATED_AT.desc());
            case "oldest" -> query.orderBy(POST.CREATED_AT.asc());
            case "most_replies" -> query.orderBy(
                org.jooq.impl.DSL.field("reply_count").desc(),
                POST.CREATED_AT.desc()
            );
            case "relevance" -> query.orderBy(
                // Title matches ranked higher than content matches
                org.jooq.impl.DSL.case_()
                    .when(POST.TITLE.likeIgnoreCase("%" + originalQuery + "%"), 1)
                    .else_(2),
                POST.CREATED_AT.desc()
            );
            default -> query.orderBy(POST.CREATED_AT.desc());
        };
    }

    private SearchResult mapRecordToSearchResult(org.jooq.Record record) {
        var searchResult = new SearchResult();
        searchResult.setAturi(record.get(POST.ATURI));
        searchResult.setTitle(record.get(POST.TITLE));
        searchResult.setContent(record.get(POST.CONTENT));
        searchResult.setCreatedAt(record.get(POST.CREATED_AT));
        searchResult.setOwnerDid(record.get(POST.OWNER_DID));
        searchResult.setStatus(record.get(POST.STATUS));
        searchResult.setDeleted(record.get(POST.IS_DELETED));
        searchResult.setOpen(record.get(POST.IS_OPEN));
        searchResult.setReplyCount(record.get("reply_count", Integer.class));

        var tagsJson = record.get(POST.TAGS);
        if (tagsJson != null) {
            try {
                var tagsJsonParsed = Json.read(tagsJson.data());
                searchResult.setTags(array(string()).decode(tagsJsonParsed));
            } catch (Exception e) {
                searchResult.setTags(new ArrayList<>());
            }
        } else {
            searchResult.setTags(new ArrayList<>());
        }

        searchResult.setTimeText(calculateTimeText(record.get(POST.CREATED_AT)));

        return searchResult;
    }

    private String calculateTimeText(OffsetDateTime createdAt) {
        var now = OffsetDateTime.now();
        var yearsBetween = java.time.temporal.ChronoUnit.YEARS.between(createdAt, now);
        var daysBetween = java.time.temporal.ChronoUnit.DAYS.between(createdAt, now);
        var hoursBetween = java.time.temporal.ChronoUnit.HOURS.between(createdAt, now);
        var minutesBetween = java.time.temporal.ChronoUnit.MINUTES.between(createdAt, now);

        if (yearsBetween > 0) {
            return yearsBetween == 1 ? "1 year ago" : yearsBetween + " years ago";
        } else if (daysBetween > 0) {
            return daysBetween == 1 ? "1 day ago" : daysBetween + " days ago";
        } else if (hoursBetween > 0) {
            return hoursBetween == 1 ? "1 hour ago" : hoursBetween + " hours ago";
        } else if (minutesBetween > 0) {
            return minutesBetween == 1 ? "1 minute ago" : minutesBetween + " minutes ago";
        } else {
            return "Just now";
        }
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
            var client = clientOpt.get();
            String handle = client.getSession().getHandle();

            var profile = com.jcs.javacommunitysite.atproto.AtprotoUtil.getBskyProfile(handle);
            String userDid = dev.mccue.json.JsonDecoder.field(profile, "did", string());

            var userRecord = dsl.selectFrom(USER)
                    .where(USER.DID.eq(userDid))
                    .fetchOne();

            if (userRecord != null && userRecord.getAvatarBloburl() != null && !userRecord.getAvatarBloburl().trim().isEmpty()) {
                return java.util.Optional.of(userRecord.getAvatarBloburl());
            }

            return java.util.Optional.empty();

        } catch (Exception e) {
            System.err.println("Error getting current user avatar: " + e.getMessage());
            return java.util.Optional.empty();
        }
    }
}
