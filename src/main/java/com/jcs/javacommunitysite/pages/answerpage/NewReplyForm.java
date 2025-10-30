package com.jcs.javacommunitysite.pages.answerpage;

public class NewReplyForm {
    private String content;  // Keep: replies have content
    private String ownerDid;  // Keep: replies have owners
    private Long parentPostId;  // ADD: which post is being replied to
    // NO title field - replies don't have titles
    // NO tags field - replies typically don't have separate tags

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getOwnerDid() {
        return ownerDid;
    }

    public void setOwnerDid(String ownerDid) {
        this.ownerDid = ownerDid;
    }

    public Long getParentPostId() {
        return parentPostId;
    }

    public void setParentPostId(Long parentPostId) {
        this.parentPostId = parentPostId;
    }
}