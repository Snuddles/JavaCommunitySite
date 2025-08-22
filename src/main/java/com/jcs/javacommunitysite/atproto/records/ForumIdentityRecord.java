package com.jcs.javacommunitysite.atproto.records;

import com.google.gson.JsonObject;
import com.jcs.javacommunitysite.atproto.exceptions.AtprotoInvalidRecord;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

public class ForumIdentityRecord extends AtprotoRecord {
    @Getter @Setter private String name;
    @Getter @Setter private String description;
    @Getter @Setter private String logo = null;
    @Getter @Setter private Color accent = null;

    public ForumIdentityRecord(JsonObject json) {
        super(json);
    }

    public ForumIdentityRecord(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public ForumIdentityRecord(String name, String description, String logo, Color accent) {
        this.name = name;
        this.description = description;
        this.logo = logo;
        this.accent = accent;
    }

    @Override
    public boolean isValid() {
        if (name == null) return false;
        if (description == null) return false;
        return true;
    }

    @Override
    public JsonObject getAsJson() throws AtprotoInvalidRecord {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("description", description);
        json.addProperty("logo", logo);
        json.addProperty("accent", accent.getRGB());
        return json;
    }

    @Override
    public String getRecordCollection() {
        return "dev.jcs.forum.identity";
    }
}
