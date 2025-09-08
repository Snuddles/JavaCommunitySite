package com.jcs.javacommunitysite.atproto.records;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.jcs.javacommunitysite.atproto.AtUri;

import java.awt.*;

import static com.jcs.javacommunitysite.JavaCommunitySiteApplication.addLexiconPrefix;

public class ForumIdentityRecord extends AtprotoRecord {
    @Expose private String name;
    @Expose private String description;
    // @Expose private String logo = null;
    @Expose private Color accent = null;

    public ForumIdentityRecord(AtUri atUri, JsonObject json) {
        super(atUri, json);
        this.name = json.get("name").getAsString();
        this.description = json.has("description") ? json.get("description").getAsString() : null;
        if (json.has("accent")) {
            String accentHex = json.get("accent").getAsString();
            this.accent = Color.decode(accentHex);
        }
    }

    public ForumIdentityRecord(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public ForumIdentityRecord(String name, String description, Color accent) {
        this.name = name;
        this.description = description;
        // this.logo = logo;
        this.accent = accent;
    }

    @Override
    public boolean isValid() {
        if (name == null) return false;
        if (description == null) return false;
        return true;
    }

    @Override
    public String getRecordCollection() {
        return addLexiconPrefix("forum.identity");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Color getAccent() {
        return accent;
    }

    public void setAccent(Color accent) {
        this.accent = accent;
    }
}
