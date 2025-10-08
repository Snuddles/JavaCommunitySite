package com.jcs.javacommunitysite.atproto.jetstream.handlers;

import com.jcs.javacommunitysite.atproto.AtUri;
import com.jcs.javacommunitysite.atproto.jetstream.JetstreamHandler;
import com.jcs.javacommunitysite.atproto.records.ForumAnnouncementRecord;
import dev.mccue.json.Json;

public class JetstreamForumAnnouncementHandler implements JetstreamHandler {
    @Override
    public void handleCreated(AtUri atUri, Json recordJson) {
        ForumAnnouncementRecord record = new ForumAnnouncementRecord(atUri, recordJson);
    }

    @Override
    public void handleUpdated(AtUri atUri, Json recordJson) {
        ForumAnnouncementRecord record = new ForumAnnouncementRecord(atUri, recordJson);
    }

    @Override
    public void handleDeleted(AtUri atUri) {

    }
}
