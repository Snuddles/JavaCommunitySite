package com.jcs.javacommunitysite.atproto.records;

import com.jcs.javacommunitysite.atproto.AtUri;
import dev.mccue.json.Json;

import java.time.Instant;

import static com.jcs.javacommunitysite.JavaCommunitySiteApplication.addLexiconPrefix;
import static dev.mccue.json.JsonDecoder.*;

public class ReplyRecord extends AtprotoRecord {
    private String content;
    private Instant createdAt;
    private Instant updatedAt = null;
    private AtUri root;

    public ReplyRecord(AtUri atUri, Json json) {
        super(atUri, json);

        this.content = field(json, "content", string());
        this.createdAt = Instant.parse(field(json, "content", string()));
        this.updatedAt = optionalNullableField(json, "updatedAt", string())
                .map(Instant::parse)
                .orElse(null);
        this.root = field(json, "root", AtUri::fromJson);
    }

    public ReplyRecord(String content, AtUri root) {
        this.content = content;
        this.createdAt = Instant.now();
        this.root = root;
    }

    @Override
    public boolean isValid() {
        if (content == null || content.isEmpty() || content.length() > 10000) return false;
        if (createdAt == null) return false;
        if (root == null) return false;
        return true;
    }

    @Override
    public String getRecordCollection() {
        return addLexiconPrefix("feed.reply");
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

    public AtUri getRoot() {
        return root;
    }

    public void setRoot(AtUri root) {
        this.root = root;
    }

    @Override
    public Json toJson() {
        return Json.objectBuilder()
                .put("content", content)
                .put("createdAt", createdAt.toString())
                .put("updatedAt", updatedAt == null ? null : updatedAt.toString())
                .put("root", root)
                .build();
    }
}
