package com.jcs.javacommunitysite.atproto.jetstream.handlers;

import com.jcs.javacommunitysite.atproto.AtUri;
import com.jcs.javacommunitysite.atproto.jetstream.JetstreamHandler;
import com.jcs.javacommunitysite.atproto.records.TagRecord;
import dev.mccue.json.Json;

public class JetstreamTagHandler implements JetstreamHandler {
    @Override
    public void handleCreated(AtUri atUri, Json recordJson) {
        TagRecord record = new TagRecord(atUri, recordJson);
        System.out.println("Tag record created:");
        System.out.println(" - AtUri: " + record.getAtUri());
        System.out.println(" - Name: " + record.getName());
        System.out.println(" - Created At: " + record.getCreatedAt());

        // Check if record.getOwnerDid() == either JCS_FORUM_DID or someone with an admin role
        // if so, create a tag record in the DB
        // otherwise, don't do anything. ignore this request and return.
    }

    @Override
    public void handleUpdated(AtUri atUri, Json recordJson) {
        TagRecord record = new TagRecord(atUri, recordJson);
        System.out.println("Tag record updated:");
        System.out.println(" - AtUri: " + record.getAtUri());
        System.out.println(" - Name: " + record.getName());
        System.out.println(" - Created At: " + record.getCreatedAt());
    }

    @Override
    public void handleDeleted(AtUri atUri) {
        System.out.println("Tag record deleted: " + atUri);
    }
}
