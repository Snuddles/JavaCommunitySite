package com.jcs.javacommunitysite.atproto.records;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.jcs.javacommunitysite.atproto.AtUri;
import dev.mccue.json.Json;

import java.time.Instant;
import java.time.OffsetDateTime;

import static com.jcs.javacommunitysite.JavaCommunitySiteApplication.addLexiconPrefix;
import static dev.mccue.json.JsonDecoder.field;
import static dev.mccue.json.JsonDecoder.string;

public class ForumAnnouncementRecord extends AtprotoRecord {
    private String title;
    private String body;
    private Instant createdAt;
    private Instant expiresAt;

    @JsonCreator
    public ForumAnnouncementRecord(AtUri atUri, Json json) {
        super(atUri, json);
        this.title = field(json, "title", string());
        this.body = field(json, "body", string());
        this.createdAt = Instant.parse(field(json, "createdAt", string()));
        this.expiresAt = Instant.parse(field(json, "expiresAt", string()));
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

    @Override
    public Json toJson() {
        return Json.objectBuilder()
                .put("title", title)
                .put("body", body)
                .put("createdAt", createdAt.toString())
                .put("expiresAt", expiresAt.toString())
                .build();
    }
}
