package com.jcs.javacommunitysite.atproto.records;

import com.jcs.javacommunitysite.atproto.AtUri;
import dev.mccue.json.Json;
import dev.mccue.json.JsonDecoder;
import dev.mccue.json.JsonEncodable;

import static com.jcs.javacommunitysite.JavaCommunitySiteApplication.addLexiconPrefix;
import static dev.mccue.json.JsonDecoder.*;

public class ForumCategoryRecord extends AtprotoRecord implements JsonEncodable {


    public enum ForumCategoryType implements JsonEncodable {
        DISCUSSION,
        QUESTION;

        @Override
        public Json toJson() {
            return switch (this) {
                case DISCUSSION -> Json.of("discussion");
                case QUESTION -> Json.of("question");
            };
        }

        public static ForumCategoryType fromJson(Json json) {
            switch (JsonDecoder.string(json)) {
                case "discussion" -> {
                    return DISCUSSION;
                }
                case "question" -> {
                    return QUESTION;
                }
                default -> {
                    throw new IllegalArgumentException("Invalid ForumCategoryType: " + json);
                }
            }
        }
    }

    private String name;
    private AtUri group;
    private String description;
    private ForumCategoryType categoryType;

    public ForumCategoryRecord(AtUri atUri, Json json) {
        super(atUri, json);
        this.name = field(json, "name", string());
        this.group = field(json, "group", AtUri::fromJson);;
        this.description = optionalNullableField(json, "description", string(), null, null);
        this.categoryType = field(json, "categoryType", ForumCategoryType::fromJson);
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

    @Override
    public Json toJson() {
        return Json.objectBuilder()
                .put("name", name)
                .put("group", group)
                .put("description", description)
                .put("categoryType", categoryType)
                .build();
    }
}
