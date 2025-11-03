package com.jcs.javacommunitysite.atproto.records;

import com.jcs.javacommunitysite.atproto.AtUri;
import dev.mccue.json.Json;

import java.time.Instant;

import static com.jcs.javacommunitysite.JavaCommunitySiteApplication.addLexiconPrefix;
import static dev.mccue.json.JsonDecoder.*;

public class TagRecord extends AtprotoRecord {
    public static final String recordCollection = addLexiconPrefix("admin.tag");

    private String name;
    private Instant createdAt;

    public TagRecord() { }

    public TagRecord(AtUri atUri) {
        super(atUri);
    }

    public TagRecord(AtUri atUri, Json json) {
        super(atUri, json);
        this.name = field(json, "name", string());
        this.createdAt = Instant.parse(field(json, "createdAt", string()));
    }

    public TagRecord(Json json) {
        super();
        this.name = field(json, "name", string());
        this.createdAt = Instant.parse(field(json, "createdAt", string()));
    }

    public TagRecord(String name) {
        this.name = name;
        this.createdAt = Instant.now();
    }

    @Override
    public boolean isValid() {
        if (name == null || name.length() < 2 || name.length() > 32) return false;
        if (createdAt == null) return false;
        return true;
    }

    @Override
    public String getRecordCollection() {
        return recordCollection;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public Json toJson() {
        return Json.objectBuilder()
                .put("name", name)
                .put("createdAt", createdAt.toString())
                .build();
    }
}
