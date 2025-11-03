package com.jcs.javacommunitysite.atproto.jetstream.handlers;

import com.jcs.javacommunitysite.atproto.AtUri;
import com.jcs.javacommunitysite.atproto.jetstream.JetstreamHandler;
import com.jcs.javacommunitysite.atproto.records.AdminGrantRecord;
import dev.mccue.json.Json;

public class JetstreamAdminGrantHandler implements JetstreamHandler {
    @Override
    public void handleCreated(AtUri atUri, Json recordJson) {
        AdminGrantRecord record = new AdminGrantRecord(atUri, recordJson);
        System.out.println("AdminGrant record created:");
        System.out.println(" - AtUri: " + record.getAtUri());
        System.out.println(" - Target DID: " + record.getTarget());
        System.out.println(" - Created At: " + record.getCreatedAt());

        // Check if record.getOwnerDid() == JCS_FORUM_DID. Do not check if it is some user with an admin role. Only JCS_FORUM_DID is allowed to add admins.
        // if so, create a tag record in the DB
        // otherwise, don't do anything. ignore this request and return.
    }

    @Override
    public void handleUpdated(AtUri atUri, Json recordJson) {
        AdminGrantRecord record = new AdminGrantRecord(atUri, recordJson);
        System.out.println("AdminGrant record updated:");
        System.out.println(" - AtUri: " + record.getAtUri());
        System.out.println(" - Target DID: " + record.getTarget());
        System.out.println(" - Created At: " + record.getCreatedAt());
    }

    @Override
    public void handleDeleted(AtUri atUri) {
        System.out.println("AdminGrant record deleted: " + atUri);
    }
}
