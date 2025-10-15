package com.jcs.javacommunitysite.atproto.jetstream.handlers;

import org.jooq.DSLContext;
import com.jcs.javacommunitysite.atproto.AtUri;
import com.jcs.javacommunitysite.atproto.jetstream.JetstreamHandler;
import com.jcs.javacommunitysite.atproto.records.ForumCategoryRecord;
import dev.mccue.json.Json;

public class JetstreamForumCategoryHandler implements JetstreamHandler {

    private final DSLContext dsl;
    
    public JetstreamForumCategoryHandler(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void handleCreated(AtUri atUri, Json recordJson) {
        ForumCategoryRecord record = new ForumCategoryRecord(atUri, recordJson);

    }

    @Override
    public void handleUpdated(AtUri atUri, Json recordJson) {
        ForumCategoryRecord record = new ForumCategoryRecord(atUri, recordJson);

    }

    @Override
    public void handleDeleted(AtUri atUri) {

    }
}
