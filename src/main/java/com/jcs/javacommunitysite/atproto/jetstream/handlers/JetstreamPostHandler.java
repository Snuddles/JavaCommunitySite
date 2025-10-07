package com.jcs.javacommunitysite.atproto.jetstream.handlers;

import com.jcs.javacommunitysite.atproto.AtUri;
import com.jcs.javacommunitysite.atproto.jetstream.JetstreamHandler;
import com.jcs.javacommunitysite.atproto.records.PostRecord;
import dev.mccue.json.Json;

public class JetstreamPostHandler implements JetstreamHandler {
    @Override
    public void handleCreated(AtUri atUri, Json recordJson) {
        PostRecord record = new PostRecord(atUri, recordJson);
        System.out.println("Post record received from AtProto!");
        System.out.println(" - Title: " + record.getTitle());
        System.out.println(" - Content: " + record.getContent());
        System.out.println(" - Category: " + record.getCategory());
        System.out.println(" - Forum: " + record.getForum());
        System.out.println(" - Created At: " + record.getCreatedAt());
        System.out.println(" - Updated At: " + record.getUpdatedAt());
    }

    @Override
    public void handleUpdated(AtUri atUri, Json recordJson) {
        PostRecord record = new PostRecord(atUri, recordJson);
    }

    @Override
    public void handleDeleted(AtUri atUri) {

    }
}
