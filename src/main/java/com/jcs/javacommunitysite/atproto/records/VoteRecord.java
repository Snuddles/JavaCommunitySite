package com.jcs.javacommunitysite.atproto.records;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.jcs.javacommunitysite.atproto.AtUri;

import java.time.Instant;

import static com.jcs.javacommunitysite.JavaCommunitySiteApplication.addLexiconPrefix;

public class VoteRecord extends AtprotoRecord {
    @Expose private AtUri root;
    @Expose private Instant createdAt;
    @Expose private int value;

    public VoteRecord(AtUri atUri, JsonObject json) {
        super(atUri, json);
        this.root = new AtUri(json.get("root").getAsString());
        this.createdAt = Instant.parse(json.get("createdAt").getAsString());
        this.value = json.get("value").getAsInt();
    }

    public VoteRecord(AtUri root, int value) {
        this.root = root;
        this.createdAt = Instant.now();
        this.value = value;
    }

    @Override
    public boolean isValid() {
        if (root == null) return false;
        if (createdAt == null) return false;
        if (value != -1 && value != 1) return false;
        return true;
    }

    @Override
    public String getRecordCollection() {
        return addLexiconPrefix("feed.vote");
    }

    public AtUri getRoot() {
        return root;
    }

    public void setRoot(AtUri root) {
        this.root = root;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
