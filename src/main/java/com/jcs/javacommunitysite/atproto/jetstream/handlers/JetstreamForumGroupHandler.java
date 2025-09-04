package com.jcs.javacommunitysite.atproto.jetstream.handlers;

import com.google.gson.JsonObject;
import com.jcs.javacommunitysite.atproto.AtUri;
import com.jcs.javacommunitysite.atproto.jetstream.JetstreamHandler;
import com.jcs.javacommunitysite.atproto.records.AtprotoRecord;
import com.jcs.javacommunitysite.atproto.records.ForumGroupRecord;

public class JetstreamForumGroupHandler implements JetstreamHandler {
    @Override
    public void handleCreated(AtUri atUri, JsonObject recordJson) {
        ForumGroupRecord record = new ForumGroupRecord(atUri, recordJson);
    }

    @Override
    public void handleUpdated(AtUri atUri, JsonObject recordJson) {
        ForumGroupRecord record = new ForumGroupRecord(atUri, recordJson);
    }

    @Override
    public void handleDeleted(AtUri atUri) {

    }
}
