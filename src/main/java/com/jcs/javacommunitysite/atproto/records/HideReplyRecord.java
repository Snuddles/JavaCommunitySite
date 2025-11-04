package com.jcs.javacommunitysite.atproto.records;

import com.jcs.javacommunitysite.atproto.AtUri;
import dev.mccue.json.Json;

import java.time.Instant;

import static com.jcs.javacommunitysite.JavaCommunitySiteApplication.addLexiconPrefix;
import static dev.mccue.json.JsonDecoder.*;

public class HideReplyRecord extends AtprotoRecord {
    public static final String recordCollection = addLexiconPrefix("admin.hidereply");

    private AtUri target;
    private Instant createdAt;
    private String reason = null;

    public HideReplyRecord() { }

    public HideReplyRecord(AtUri atUri) {
        super(atUri);
    }

    public HideReplyRecord(AtUri atUri, Json json) {
        super(atUri, json);
        this.target = field(json, "target", AtUri::fromJson);
        this.createdAt = Instant.parse(field(json, "createdAt", string()));
        this.reason = optionalNullableField(json, "reason", string())
                .orElse(null);
    }

    public HideReplyRecord(Json json) {
        super();
        this.target = field(json, "target", AtUri::fromJson);
        this.createdAt = Instant.parse(field(json, "createdAt", string()));
        this.reason = optionalNullableField(json, "reason", string())
                .orElse(null);
    }

    public HideReplyRecord(AtUri target, Instant createdAt) {
        this.target = target;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public HideReplyRecord(AtUri target, String reason) {
        this.target = target;
        this.createdAt = Instant.now();
        this.reason = reason;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
