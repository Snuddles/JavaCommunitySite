package com.jcs.javacommunitysite.atproto.records;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.jcs.javacommunitysite.atproto.AtUri;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.jcs.javacommunitysite.JavaCommunitySiteApplication.addLexiconPrefix;

public class PostRecord extends AtprotoRecord {

    @Expose private String title;
    @Expose private String content;
    @Expose private Instant createdAt;
    @Expose private Instant updatedAt = null;
    @Expose private AtUri category;
    @Expose private String forum;  // should be a DID
    @Expose private List<String> tags;
    @Expose private AtUri solution = null;

    public PostRecord(AtUri atUri){
        super(atUri);
    }

    public PostRecord(AtUri atUri, JsonObject json) {
        super(atUri, json);
        this.title = json.get("title").getAsString();
        this.content = json.get("content").getAsString();
        this.createdAt = Instant.parse(json.get("createdAt").getAsString());
        if (json.has("updatedAt"))
            this.updatedAt = Instant.parse(json.get("updatedAt").getAsString());
        this.category = new AtUri(json.get("category").getAsString());
        this.forum = json.get("forum").getAsString();
        this.tags = new ArrayList<>();
        json.get("tags").getAsJsonArray().forEach(tag -> this.tags.add(tag.getAsString()));
        this.solution = json.has("solution") ? new AtUri(json.get("solution").getAsString()) : null;
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
