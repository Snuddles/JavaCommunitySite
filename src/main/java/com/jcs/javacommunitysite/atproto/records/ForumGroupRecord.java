package com.jcs.javacommunitysite.atproto.records;

import com.google.gson.JsonObject;
import com.jcs.javacommunitysite.atproto.exceptions.AtprotoInvalidRecord;
import lombok.Getter;
import lombok.Setter;

public class ForumGroupRecord extends AtprotoRecord {
    @Getter @Setter private String name;
    @Getter @Setter private String description = null;

    public ForumGroupRecord(JsonObject json) {
        super(json);
    }

    public ForumGroupRecord(String name) {
        this.name = name;
    }

    public ForumGroupRecord(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public boolean isValid() {
        if (name == null) return false;
        return true;
    }

    @Override
    public JsonObject getAsJson() throws AtprotoInvalidRecord {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("description", description);
        return json;
    }

    @Override
    public String getRecordCollection() {
        return "dev.jcs.forum.group";
    }
}
