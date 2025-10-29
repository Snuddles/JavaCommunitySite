package com.jcs.javacommunitysite.pages.searchpage;

import com.jcs.javacommunitysite.atproto.AtUri;
import java.time.OffsetDateTime;
import java.util.List;

public class SearchResult {
    private String aturi;
    private String title;
    private String content;
    private List<String> tags;
    private OffsetDateTime createdAt;
    private String ownerDid;
    private String status;
    private boolean isDeleted;
    private boolean isOpen;
    private int replyCount;
    private String timeText;
    
    // Constructors
    public SearchResult() {}
    
    public SearchResult(String aturi, String title, String content, List<String> tags, 
                       OffsetDateTime createdAt, String ownerDid, String status, 
                       boolean isDeleted, boolean isOpen, int replyCount, String timeText) {
        this.aturi = aturi;
        this.title = title;
        this.content = content;
        this.tags = tags;
        this.createdAt = createdAt;
        this.ownerDid = ownerDid;
        this.status = status;
        this.isDeleted = isDeleted;
        this.isOpen = isOpen;
        this.replyCount = replyCount;
        this.timeText = timeText;
    }
    
    // Getters and setters
    public String getAturi() {
        return aturi;
    }

    public void setAturi(String aturi) {
        this.aturi = aturi;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

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

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getOwnerDid() {
        return ownerDid;
    }

    public void setOwnerDid(String ownerDid) {
        this.ownerDid = ownerDid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public int getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }

    public String getTimeText() {
        return timeText;
    }

    public void setTimeText(String timeText) {
        this.timeText = timeText;
    }

    public AtUri getAtUri() {
        return new AtUri(this.aturi);
    }
}