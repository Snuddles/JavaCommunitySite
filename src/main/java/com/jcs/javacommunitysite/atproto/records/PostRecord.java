package com.jcs.javacommunitysite.atproto.records;

import com.google.gson.JsonObject;
import com.jcs.javacommunitysite.atproto.exceptions.AtprotoInvalidRecord;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Optional;

public class PostRecord extends AtprotoRecord {
    @Getter @Setter private String text = null;
    @Getter @Setter private Instant createdAt = null;

    public PostRecord(JsonObject json) {
        super(json);
    }

    public PostRecord(String text) {
        this.text = text;
        this.createdAt = Instant.now();
    }

    public PostRecord(String text, Instant createdAt) {
        this.text = text;
        this.createdAt = createdAt;
    }

    @Override
    public boolean isValid() {
        if (text == null) return false;
        if (createdAt == null) return false;
        return true;
    }

    @Override
    public JsonObject getAsJson() {
        if (!isValid()) throw new AtprotoInvalidRecord();

        JsonObject json = new JsonObject();
        json.addProperty("text", text);
        json.addProperty("createdAt", createdAt.toString());
        return json;
    }

    @Override
    public String getRecordCollection() {
        return "app.bsky.feed.post";
    }
}
