package com.jcs.javacommunitysite.forms;

import java.util.ArrayList;
import java.util.List;

public class UpdatePostForm {
    private String content;
    private List<String> tags = new ArrayList<>();

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
