package com.jcs.javacommunitysite.atproto.records;

import com.jcs.javacommunitysite.atproto.AtUri;
import dev.mccue.json.Json;

import java.time.Instant;

import static com.jcs.javacommunitysite.JavaCommunitySiteApplication.addLexiconPrefix;
import static dev.mccue.json.JsonDecoder.*;

public class HideUserRecord extends AtprotoRecord {
    public static final String recordCollection = addLexiconPrefix("admin.hideuser");

    private String target; // DID
    private Instant createdAt;
    private String reason = null;

    public HideUserRecord() { }

    public HideUserRecord(AtUri atUri) {
        super(atUri);
    }

    public HideUserRecord(AtUri atUri, Json json) {
        super(atUri, json);
        this.target = field(json, "target", string());
        this.createdAt = Instant.parse(field(json, "createdAt", string()));
        this.reason = optionalNullableField(json, "reason", string())
                .orElse(null);
    }

    public HideUserRecord(Json json) {
        super();
        this.target = field(json, "target", string());
        this.createdAt = Instant.parse(field(json, "createdAt", string()));
        this.reason = optionalNullableField(json, "reason", string())
                .orElse(null);
    }

    public HideUserRecord(String targetDid, Instant createdAt) {
        this.target = targetDid;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public HideUserRecord(String targetDid, String reason) {
        this.target = targetDid;
        this.createdAt = Instant.now();
        this.reason = reason;
    }

    @Override
    public boolean isValid() {
        if (target == null || target.isEmpty()) return false;
        if (createdAt == null) return false;
        return true;
    }

    @Override
    public String getRecordCollection() {
        return recordCollection;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
