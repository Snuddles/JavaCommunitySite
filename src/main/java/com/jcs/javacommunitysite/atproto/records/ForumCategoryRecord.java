package com.jcs.javacommunitysite.atproto.records;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.jcs.javacommunitysite.atproto.exceptions.AtprotoInvalidRecord;
import lombok.Getter;
import lombok.Setter;

public class ForumCategoryRecord extends AtprotoRecord {
    public enum ForumCategoryType {
        @SerializedName("discussion")
        DISCUSSION,
        @SerializedName("question")
        QUESTION,
    }

    @Getter @Setter private String name;
    @Getter @Setter private String group;
    @Getter @Setter private String description = null;
    @Getter @Setter private ForumCategoryType categoryType;

    public ForumCategoryRecord(JsonObject json) {
        super(json);
    }

    public ForumCategoryRecord(String name, String group, ForumCategoryType category) {
        this.name = name;
        this.group = group;
        this.categoryType = category;
    }

    public ForumCategoryRecord(String name, String group, String description, ForumCategoryType category) {
        this.name = name;
        this.group = group;
        this.description = description;
        this.categoryType = category;
    }

    @Override
    public boolean isValid() {
        if (name == null) return false;
        if (group == null) return false;
        if (categoryType == null) return false;
        return true;
    }

    @Override
    public JsonObject getAsJson() throws AtprotoInvalidRecord {
        if (!isValid()) throw new AtprotoInvalidRecord();

        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("group", group);
        json.addProperty("description", description);
        json.addProperty("categoryType", categoryType);
        return json;
    }

    @Override
    public String getRecordCollection() {
        return "dev.jcs.forum.category";
    }
}
