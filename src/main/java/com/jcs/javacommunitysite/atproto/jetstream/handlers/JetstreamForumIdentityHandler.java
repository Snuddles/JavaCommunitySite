package com.jcs.javacommunitysite.atproto.jetstream.handlers;

import org.jooq.DSLContext;
import com.jcs.javacommunitysite.atproto.AtUri;
import com.jcs.javacommunitysite.atproto.jetstream.JetstreamHandler;
import com.jcs.javacommunitysite.atproto.records.ForumIdentityRecord;
import dev.mccue.json.Json;

public class JetstreamForumIdentityHandler implements JetstreamHandler {

    private final DSLContext dsl;
    
    public JetstreamForumIdentityHandler(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void handleCreated(AtUri atUri, Json recordJson) {
        ForumIdentityRecord record = new ForumIdentityRecord(atUri, recordJson);

        System.out.println("FORUM IDENTITY CREATED:");
        System.out.println(record.getName());
        System.out.println(record.getDescription());
        System.out.println(record.getAccent());
        System.out.println(record.getAtUri());
    }

    @Override
    public void handleUpdated(AtUri atUri, Json recordJson) {
        System.out.println("FORUM IDENTITY UPDATED");
        ForumIdentityRecord record = new ForumIdentityRecord(atUri, recordJson);
    }

    @Override
    public void handleDeleted(AtUri atUri) {

    }
}
