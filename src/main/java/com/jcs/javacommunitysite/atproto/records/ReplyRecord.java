package com.jcs.javacommunitysite.atproto.records;

import com.jcs.javacommunitysite.atproto.AtUri;
import dev.mccue.json.Json;
import org.jooq.DSLContext;

import java.time.Instant;

import static com.jcs.javacommunitysite.JavaCommunitySiteApplication.addLexiconPrefix;
import static com.jcs.javacommunitysite.jooq.tables.Reply.REPLY;
import static dev.mccue.json.JsonDecoder.*;

public class ReplyRecord extends AtprotoRecord {
    private String content;
    private Instant createdAt;
    private Instant updatedAt = null;
    private AtUri root;

    private DSLContext dsl;

    public ReplyRecord(AtUri atUri) {
        super(atUri);
    }

    public ReplyRecord(AtUri atUri, DSLContext dsl) {
        super(atUri);
        this.dsl = dsl;
        fetchFromDB(atUri);
    }

    private void fetchFromDB(AtUri atUri) {
        var record = dsl.select()
                .from(REPLY)
                .where(REPLY.ATURI.eq(atUri.toString()))
                .fetchOne();
        
        if(record != null){
            this.content = record.get(REPLY.CONTENT);
            this.createdAt = record.get(REPLY.CREATED_AT).toInstant();
            this.updatedAt = record.get(REPLY.UPDATED_AT) == null ? null : record.get(REPLY.UPDATED_AT).toInstant();
            this.root = new AtUri(record.get(REPLY.ROOT));
        }
    }

    public ReplyRecord(AtUri atUri, Json json) {
        super(atUri, json);

        this.content = field(json, "content", string());
        this.createdAt = Instant.parse(field(json, "createdAt", string()));
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

    public ReplyRecord(Json json) {
        super();
        this.content = field(json, "content", string());
        this.createdAt = Instant.parse(field(json, "createdAt", string()));
        this.updatedAt = optionalNullableField(json, "updatedAt", string())
                .map(Instant::parse)
                .orElse(null);
        this.root = field(json, "root", AtUri::fromJson);
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
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
