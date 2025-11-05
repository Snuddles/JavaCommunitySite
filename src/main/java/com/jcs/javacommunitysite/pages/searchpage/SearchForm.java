package com.jcs.javacommunitysite.pages.searchpage;

import java.util.ArrayList;
import java.util.List;

public class SearchForm {
    private String query = "";
    private String status = "";
    private String sortBy = "";
    private String sortDir = "";
    private List<String> tags = new ArrayList<>();

    public SearchForm() { }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query != null ? query.trim() : "";
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDir() {
        return sortDir;
    }

    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}