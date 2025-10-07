package com.jcs.javacommunitysite.atproto.records;

import com.jcs.javacommunitysite.atproto.AtUri;
import dev.mccue.json.Json;

import java.time.Instant;

import static com.jcs.javacommunitysite.JavaCommunitySiteApplication.addLexiconPrefix;
import static dev.mccue.json.JsonDecoder.*;

public class VoteRecord extends AtprotoRecord {
    private AtUri root;
    private Instant createdAt;
    private int value;

    public VoteRecord(AtUri atUri, Json json) {
        super(atUri, json);
        this.root = field(json, "root", AtUri::fromJson);
        this.createdAt = Instant.parse(field(json, "createdAt", string()));
        this.value = field(json, "value", int_());
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

    @Override
    public Json toJson() {
        return Json.objectBuilder()
                .put("root", root)
                .put("createdAt", createdAt.toString())
                .put("value", value)
                .build();
    }
}
