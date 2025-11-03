package com.jcs.javacommunitysite.atproto.records;

import com.jcs.javacommunitysite.atproto.AtUri;
import dev.mccue.json.Json;

import java.time.Instant;

import static com.jcs.javacommunitysite.JavaCommunitySiteApplication.addLexiconPrefix;
import static dev.mccue.json.JsonDecoder.*;

public class HidePostRecord extends AtprotoRecord {
    public static final String recordCollection = addLexiconPrefix("admin.hide_post");

    private AtUri target;
    private Instant createdAt;

    public HidePostRecord(AtUri atUri) {
        super(atUri);
    }

    public HidePostRecord(AtUri atUri, Json json) {
        super(atUri, json);
        this.target = field(json, "target", AtUri::fromJson);
        this.createdAt = Instant.parse(field(json, "createdAt", string()));
    }

    public HidePostRecord(Json json) {
        super();
        this.target = field(json, "target", AtUri::fromJson);
        this.createdAt = Instant.parse(field(json, "createdAt", string()));
    }

    public HidePostRecord(AtUri target, Instant createdAt) {
        this.target = target;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    @Override
    public boolean isValid() {
        if (target == null) return false;
        if (createdAt == null) return false;
        return true;
    }

    @Override
    public String getRecordCollection() {
        return recordCollection;
    }

    public AtUri getTarget() {
        return target;
    }

    public void setTarget(AtUri target) {
        this.target = target;
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
                .put("target", target)
                .put("createdAt", createdAt.toString())
                .build();
    }
}
