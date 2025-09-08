package com.jcs.javacommunitysite.atproto.records;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.jcs.javacommunitysite.atproto.AtUri;

import java.time.Instant;
import java.time.OffsetDateTime;

import static com.jcs.javacommunitysite.JavaCommunitySiteApplication.addLexiconPrefix;

public class ForumAnnouncementRecord extends AtprotoRecord {
    @Expose private String title;
    @Expose private String body;
    @Expose private Instant createdAt;
    @Expose private Instant expiresAt;

    public ForumAnnouncementRecord(AtUri atUri, JsonObject json) {
        super(atUri, json);
        this.title = json.get("title").getAsString();
        this.body = json.get("body").getAsString();
        this.createdAt = Instant.parse(json.get("createdAt").getAsString());
        this.expiresAt = Instant.parse(json.get("expiresAt").getAsString());
    }

    public ForumAnnouncementRecord(String title, String body, Instant createdAt, Instant expiresAt) {
        this.title = title;
        this.body = body;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public ForumAnnouncementRecord(String title, String body, OffsetDateTime createdAt, OffsetDateTime expiresAt) {
        this.title = title;
        this.body = body;
        this.createdAt = createdAt.toInstant();
        this.expiresAt = expiresAt.toInstant();
    }

    @Override
    public boolean isValid() {
        if (title == null || title.isEmpty() || title.length() > 100) return false;
        if (body == null || body.isEmpty() || body.length() > 10000) return false;
        if (createdAt == null) return false;
        if (expiresAt == null) return false;
        return true;
    }

    @Override
    public String getRecordCollection() {
        return addLexiconPrefix("forum.announcement");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
