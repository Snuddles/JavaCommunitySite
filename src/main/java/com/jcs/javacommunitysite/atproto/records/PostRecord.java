package com.jcs.javacommunitysite.atproto.records;

import com.jcs.javacommunitysite.atproto.AtUri;
import dev.mccue.json.Json;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.jooq.DSLContext;
import static com.jcs.javacommunitysite.JavaCommunitySiteApplication.addLexiconPrefix;
import static dev.mccue.json.JsonDecoder.*;

import static com.jcs.javacommunitysite.jooq.tables.Post.POST;

public class PostRecord extends AtprotoRecord {

    private String title;
    private String content;
    private Instant createdAt;
    private Instant updatedAt = null;
    private AtUri category;
    private String forum;  // should be a DID
    private List<String> tags;
    private AtUri solution = null;

    private DSLContext dsl;

    @Override
    public Json toJson() {
        return Json.objectBuilder()
                .put("title", title)
                .put("content", content)
                .put("createdAt", createdAt.toString())
                .put("updatedAt", updatedAt == null ? null : updatedAt.toString())
                .put("category", category)
                .put("forum", forum)
                .put("tags", Json.of(tags, Json::of))
                .put("solution", solution)
                .build();
    }

     public PostRecord(AtUri atUri) {
        super(atUri);
    }

    public PostRecord(AtUri atUri, DSLContext dsl) {
        super(atUri);
        this.dsl = dsl;
        fetchFromDB(atUri);
    }

    private void fetchFromDB(AtUri atUri){

        var record = dsl.select()
                .from(POST)
                .where(POST.ATURI.eq(atUri.toString()))
                .fetchOne();
        
        if(record != null){
            this.title = record.get(POST.TITLE);
            this.content = record.get(POST.CONTENT);
            this.createdAt = record.get(POST.CREATED_AT).toInstant();
            this.updatedAt = record.get(POST.UPDATED_AT) == null ? null : record.get(POST.UPDATED_AT).toInstant();
            this.category = new AtUri(record.get(POST.CATEGORY_ATURI));
            this.forum = record.get(POST.FORUM);
            
            var tagsJsonb = record.get(POST.TAGS);
            if (tagsJsonb != null) {
                var tagsJson = Json.read(tagsJsonb.data());
                this.tags = array(string()).decode(tagsJson);
            } else {
                this.tags = new ArrayList<>();
            }
            
            this.solution = record.get(POST.SOLUTION) == null ? null : new AtUri(record.get(POST.SOLUTION));
        }
    }

    public PostRecord(AtUri atUri, Json json) {
        super(atUri, json);
        this.title = field(json, "title", string());
        this.content = field(json, "content", string());
        this.createdAt = Instant.parse(field(json, "createdAt", string()));
        this.updatedAt = optionalNullableField(json, "updatedAt", string())
                .map(Instant::parse)
                .orElse(null);
        this.category = field(json, "category", AtUri::fromJson);
        this.forum = field(json, "forum", string());
        this.tags = field(json, "tags", array(string()));
        this.solution = optionalNullableField(json, "solution", AtUri::fromJson, null);
    }

    public PostRecord(Json json) {
        super();
        this.title = field(json, "title", string());
        this.content = field(json, "content", string());
        this.createdAt = Instant.parse(field(json, "createdAt", string()));
        this.updatedAt = optionalNullableField(json, "updatedAt", string())
                .map(Instant::parse)
                .orElse(null);
        this.category = field(json, "category", AtUri::fromJson);
        this.forum = field(json, "forum", string());
        this.tags = field(json, "tags", array(string()));
        this.solution = optionalNullableField(json, "solution", AtUri::fromJson, null);
    }

    public PostRecord(String title, String content, AtUri category, String forum) {
        this.title = title;
        this.content = content;
        this.createdAt = Instant.now();
        this.category = category;
        this.forum = forum;
        this.tags = new ArrayList<>();
    }

    public PostRecord(String title, String content, AtUri category, String forum, List<String> tags) {
        this.title = title;
        this.content = content;
        this.createdAt = Instant.now();
        this.category = category;
        this.forum = forum;
        this.tags = tags;
    }

    @Override
    public boolean isValid() {
        if (title == null || title.isEmpty() || title.length() > 100) return false;
        if (content == null || content.isEmpty() || content.length() > 10000) return false;
        if (createdAt == null) return false;
        if (category == null) return false;
        if (forum == null) return false;
        if (tags == null || tags.stream().anyMatch(t -> t.length() > 25)) return false;
        return true;
    }

    @Override
    public String getRecordCollection() {
        return addLexiconPrefix("feed.post");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public AtUri getCategory() {
        return category;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCategory(AtUri category) {
        this.category = category;
    }

    public String getForum() {
        return forum;
    }

    public void setForum(String forum) {
        this.forum = forum;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public AtUri getSolution() {
        return solution;
    }

    public void setSolution(AtUri solution) {
        this.solution = solution;
    }
}
