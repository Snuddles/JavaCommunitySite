package com.jcs.javacommunitysite.atproto.jetstream.handlers;

import com.google.gson.JsonObject;
import com.jcs.javacommunitysite.atproto.AtUri;
import com.jcs.javacommunitysite.atproto.jetstream.JetstreamHandler;
import com.jcs.javacommunitysite.atproto.records.AtprotoRecord;
import com.jcs.javacommunitysite.atproto.records.VoteRecord;

public class JetstreamVoteHandler implements JetstreamHandler {
    @Override
    public void handleCreated(AtUri atUri, JsonObject recordJson) {
        VoteRecord record = new VoteRecord(atUri, recordJson);
    }

    @Override
    public void handleUpdated(AtUri atUri, JsonObject recordJson) {
        VoteRecord record = new VoteRecord(atUri, recordJson);
    }

    @Override
    public void handleDeleted(AtUri atUri) {

    }
}
