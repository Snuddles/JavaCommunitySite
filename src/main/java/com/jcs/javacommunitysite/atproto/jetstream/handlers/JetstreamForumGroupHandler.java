package com.jcs.javacommunitysite.atproto.jetstream.handlers;

import com.jcs.javacommunitysite.atproto.AtUri;
import com.jcs.javacommunitysite.atproto.jetstream.JetstreamHandler;
import com.jcs.javacommunitysite.atproto.records.ForumGroupRecord;
import dev.mccue.json.Json;

public class JetstreamForumGroupHandler implements JetstreamHandler {
    @Override
    public void handleCreated(AtUri atUri, Json recordJson) {
        ForumGroupRecord record = new ForumGroupRecord(atUri, recordJson);
    }

    @Override
    public void handleUpdated(AtUri atUri, Json recordJson) {
        ForumGroupRecord record = new ForumGroupRecord(atUri, recordJson);
    }

    @Override
    public void handleDeleted(AtUri atUri) {

    }
}
