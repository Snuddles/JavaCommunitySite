package com.jcs.javacommunitysite.atproto.jetstream.handlers;

import com.google.gson.JsonObject;
import com.jcs.javacommunitysite.atproto.AtUri;
import com.jcs.javacommunitysite.atproto.jetstream.JetstreamHandler;
import com.jcs.javacommunitysite.atproto.records.AtprotoRecord;
import com.jcs.javacommunitysite.atproto.records.ForumIdentityRecord;

public class JetstreamForumIdentityHandler implements JetstreamHandler {
    @Override
    public void handleCreated(AtUri atUri, JsonObject recordJson) {
        ForumIdentityRecord record = new ForumIdentityRecord(atUri, recordJson);

        System.out.println("FORUM IDENTITY CREATED:");
        System.out.println(record.getName());
        System.out.println(record.getDescription());
        System.out.println(record.getAccent());
        System.out.println(record.getAtUri());
    }

    @Override
    public void handleUpdated(AtUri atUri, JsonObject recordJson) {
        System.out.println("FORUM IDENTITY UPDATED");
        ForumIdentityRecord record = new ForumIdentityRecord(atUri, recordJson);
    }

    @Override
    public void handleDeleted(AtUri atUri) {

    }
}
