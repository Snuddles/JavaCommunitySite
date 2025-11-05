package com.jcs.javacommunitysite.pages.searchpage;

import com.jcs.javacommunitysite.util.TimeUtil;
import org.jooq.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import com.jcs.javacommunitysite.atproto.service.AtprotoSessionService;
import dev.mccue.json.Json;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import org.jooq.SelectLimitStep;
import org.jooq.SelectConditionStep;

import static com.jcs.javacommunitysite.jooq.tables.Post.POST;
import static com.jcs.javacommunitysite.jooq.tables.Reply.REPLY;
import static com.jcs.javacommunitysite.jooq.tables.User.USER;
import static com.jcs.javacommunitysite.jooq.tables.Tags.TAGS;
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
            Model model
    ) {
        SearchForm searchForm = new SearchForm();

        var tags = dsl.select(TAGS.ATURI, TAGS.TAG_NAME)
                .from(TAGS)
                .orderBy(TAGS.TAG_NAME)
                .fetchMap(TAGS.ATURI, TAGS.TAG_NAME);

        model.addAttribute("searchForm", searchForm);
        model.addAttribute("tags", tags);
        getCurrentUserAvatarUrl().ifPresent(avatarUrl ->
            model.addAttribute("currentUserAvatarUrl", avatarUrl)
        );
        

        return "pages/search/search";
    }

    @PostMapping("/search")
    public String doSearch(
            Model model,
            @ModelAttribute SearchForm searchForm
    ) {
        var query = searchForm.getQuery().trim();
        var status = searchForm.getStatus();
        var sortBy = searchForm.getSortBy();
        var sortDir = searchForm.getSortDir();
        var tags = searchForm.getTags();

        List<SearchResult> searchResults = new ArrayList<>();
        if (!query.isEmpty() && !status.isEmpty() && !sortBy.isEmpty() && !sortDir.isEmpty()) {
            searchResults = performSearch(
                    query,
                    status,
                    sortBy,
                    sortDir,
                    tags
            );
        }

        model.addAttribute("searchResults", searchResults);
        return "pages/search/htmx/results";
    }

    private List<SearchResult> performSearch(String query, String status, String sortBy, String sortDir, List<String> tags) {
        try {
            final int LIMIT = 50;
            List<SearchResult> accumulated = new ArrayList<>();
            Set<String> seenAturis = new HashSet<>();

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

            var exactRows = applySorting(exactQuery, sortBy, sortDir, query).limit(LIMIT).fetch();

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

                    var allRows = applySorting(allWordsQuery, sortBy, sortDir, query).limit(LIMIT - accumulated.size()).fetch();

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

                    var anyRows = applySorting(broadQuery, sortBy, sortDir, query).limit(LIMIT - accumulated.size()).fetch();
                    for (var r : anyRows) {
                        String aturi = r.get(POST.ATURI);
                        if (seenAturis.add(aturi)) {
                            accumulated.add(mapRecordToSearchResult(r));
                        }
                    }
                }
            }

            // Filter out tags
            if (tags != null && !tags.isEmpty()) {
                var copy = new ArrayList<>(accumulated);
                for (var result : copy) {
                    boolean hasTag = false;
                    for (var tag : tags) {
                        if (result.getTags().contains(tag)) {
                            hasTag = true;
                            break;
                        }
                    }

                    if (!hasTag) {
                        accumulated.remove(result);
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

    private SelectLimitStep<?> applySorting(SelectConditionStep<?> query, String sortBy, String sortDir, String originalQuery) {
        List<Field<?>> sortFields = new ArrayList<>();

        switch (sortBy != null ? sortBy.toLowerCase() : "relevance") {
            case "time-posted" -> sortFields = Arrays.asList(POST.CREATED_AT);
            case "num-replies" -> sortFields = Arrays.asList(
                org.jooq.impl.DSL.field("reply_count"),
                POST.CREATED_AT
            );
            case "relevance" -> sortFields = Arrays.asList(
                // Title matches ranked higher than content matches
                org.jooq.impl.DSL.case_()
                    .when(POST.TITLE.likeIgnoreCase("%" + originalQuery + "%"), 1)
                    .else_(2),
                POST.CREATED_AT
            );
            default -> sortFields = Arrays.asList(POST.CREATED_AT);
        }

        List<SortField<?>> sortedFields = new ArrayList<>();
        for (Field<?> f : sortFields) {
            if (sortDir.equals("desc")) {
                sortedFields.add(f.desc());
            } else if (sortDir.equals("asc")) {
                sortedFields.add(f.asc());
            }
        }

        return query.orderBy(sortedFields.toArray(new SortField<?>[0]));
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

        searchResult.setTimeText(TimeUtil.calculateTimeText(record.get(POST.CREATED_AT)));

        return searchResult;
    }

    private Optional<String> getCurrentUserAvatarUrl() {
        if (!sessionService.isAuthenticated()) {
            return Optional.empty();
        }

        var clientOpt = sessionService.getCurrentClient();
        if (clientOpt.isEmpty()) {
            return Optional.empty();
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
                return Optional.of(userRecord.getAvatarBloburl());
            }

            return Optional.empty();

        } catch (Exception e) {
            System.err.println("Error getting current user avatar: " + e.getMessage());
            return Optional.empty();
        }
    }
}
