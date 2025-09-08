package com.jcs.javacommunitysite.atproto.records;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.jcs.javacommunitysite.atproto.AtUri;

import static com.jcs.javacommunitysite.JavaCommunitySiteApplication.addLexiconPrefix;

public class ForumCategoryRecord extends AtprotoRecord {
    public enum ForumCategoryType {
        @SerializedName("discussion")
        DISCUSSION,
        @SerializedName("question")
        QUESTION,
    }

    @Expose private String name;
    @Expose private AtUri group;

    @Expose private String description;

    @Expose private ForumCategoryType categoryType;
    public ForumCategoryRecord(AtUri atUri, JsonObject json) {
        super(atUri, json);
        this.name = json.get("name").getAsString();
        this.group = new AtUri(json.get("group").getAsString());
        this.description = json.has("description") ? json.get("description").getAsString() : null;
        this.categoryType = ForumCategoryType.valueOf(json.get("categoryType").getAsString().toUpperCase());
    }

    public ForumCategoryRecord(String name, AtUri group, ForumCategoryType category) {
        this.name = name;
        this.group = group;
        this.categoryType = category;
    }

    public ForumCategoryRecord(String name, AtUri group, String description, ForumCategoryType category) {
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
    public String getRecordCollection() {
        return addLexiconPrefix("forum.category");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AtUri getGroup() {
        return group;
    }

    public void setGroup(AtUri group) {
        this.group = group;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ForumCategoryType getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(ForumCategoryType categoryType) {
        this.categoryType = categoryType;
    }
}
