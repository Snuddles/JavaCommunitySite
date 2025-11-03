package com.jcs.javacommunitysite.atproto.jetstream.handlers;

import com.jcs.javacommunitysite.atproto.AtUri;
import com.jcs.javacommunitysite.atproto.jetstream.JetstreamHandler;
import com.jcs.javacommunitysite.atproto.records.HideReplyRecord;
import dev.mccue.json.Json;

public class JetstreamHideReplyHandler implements JetstreamHandler {
    @Override
    public void handleCreated(AtUri atUri, Json recordJson) {
        HideReplyRecord record = new HideReplyRecord(atUri, recordJson);
        System.out.println("HideReply record created:");
        System.out.println(" - AtUri: " + record.getAtUri());
        System.out.println(" - Target: " + record.getTarget());
        System.out.println(" - Created At: " + record.getCreatedAt());

        // Check if record.getOwnerDid() == either JCS_FORUM_DID or someone with an admin role
        // if so, create a tag record in the DB
        // otherwise, don't do anything. ignore this request and return.
    }

    @Override
    public void handleUpdated(AtUri atUri, Json recordJson) {
        HideReplyRecord record = new HideReplyRecord(atUri, recordJson);
        System.out.println("HideReply record updated:");
        System.out.println(" - AtUri: " + record.getAtUri());
        System.out.println(" - Target: " + record.getTarget());
        System.out.println(" - Created At: " + record.getCreatedAt());
    }

    @Override
    public void handleDeleted(AtUri atUri) {
        System.out.println("HideReply record deleted: " + atUri);
    }
}
