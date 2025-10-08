package com.jcs.javacommunitysite.atproto.records;

import com.jcs.javacommunitysite.atproto.AtUri;
import dev.mccue.json.Json;

import static com.jcs.javacommunitysite.JavaCommunitySiteApplication.addLexiconPrefix;
import static dev.mccue.json.JsonDecoder.*;

public class ForumGroupRecord extends AtprotoRecord {
    private String name;
    private String description;

    public ForumGroupRecord(AtUri atUri, Json json) {
        super(atUri, json);
        this.name = field(json, "name", string());
        this.description = optionalNullableField(
                json, "description", string(), null, null
        );
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
    public String getRecordCollection() {
        return addLexiconPrefix("forum.group");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Json toJson() {
        return Json.objectBuilder()
                .put("name", name)
                .put("description", description)
                .build();
    }
}
