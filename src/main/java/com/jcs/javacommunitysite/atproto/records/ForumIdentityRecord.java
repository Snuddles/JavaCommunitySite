package com.jcs.javacommunitysite.atproto.records;

import com.jcs.javacommunitysite.atproto.AtUri;
import dev.mccue.color.Color;
import dev.mccue.json.Json;


import static com.jcs.javacommunitysite.JavaCommunitySiteApplication.addLexiconPrefix;
import static dev.mccue.json.JsonDecoder.*;

public class ForumIdentityRecord extends AtprotoRecord {
    private String name;
    private String description;
    // @Expose private String logo = null;
    private Color accent = null;

    public ForumIdentityRecord(AtUri atUri, Json json) {
        super(atUri, json);
        this.name = field(json, "name", string());
        this.description = optionalNullableField(json, "description", string(), null, null);
        this.accent = optionalNullableField(json, "accent", accent -> Color.hex(string(accent)), null);
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

    @Override
    public Json toJson() {
        return Json.objectBuilder()
                .put("name", name)
                .put("description", description)
                .put("accent", accent.hex())
                .build();
    }
}
