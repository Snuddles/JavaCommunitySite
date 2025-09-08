package com.jcs.javacommunitysite.atproto.records;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.jcs.javacommunitysite.atproto.AtUri;

import java.time.Instant;

import static com.jcs.javacommunitysite.JavaCommunitySiteApplication.addLexiconPrefix;

public class ReplyRecord extends AtprotoRecord {
    @Expose private String text;
    @Expose private Instant createdAt;
    @Expose private AtUri root;

    public ReplyRecord(AtUri atUri, JsonObject json) {
        super(atUri, json);
        this.text = json.get("text").getAsString();
        this.createdAt = Instant.parse(json.get("createdAt").getAsString());
        this.root = new AtUri(json.get("root").getAsString());
    }

    public ReplyRecord(String text, AtUri root) {
        this.text = text;
        this.createdAt = Instant.now();
        this.root = root;
    }

    @Override
    public boolean isValid() {
        if (text == null || text.isEmpty() || text.length() > 10000) return false;
        if (createdAt == null) return false;
        if (root == null) return false;
        return true;
    }

    @Override
    public String getRecordCollection() {
        return addLexiconPrefix("feed.reply");
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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
}
