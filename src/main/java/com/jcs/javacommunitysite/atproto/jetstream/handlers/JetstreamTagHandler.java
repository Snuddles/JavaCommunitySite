package com.jcs.javacommunitysite.atproto.jetstream.handlers;

import com.jcs.javacommunitysite.atproto.AtUri;
import com.jcs.javacommunitysite.atproto.jetstream.JetstreamHandler;
import com.jcs.javacommunitysite.atproto.records.TagRecord;
import dev.mccue.json.Json;
import org.jooq.DSLContext;
import static com.jcs.javacommunitysite.JavaCommunitySiteApplication.JCS_FORUM_DID;
import static com.jcs.javacommunitysite.jooq.tables.UserRole.USER_ROLE;
import static com.jcs.javacommunitysite.jooq.tables.Tags.TAGS;

public class JetstreamTagHandler implements JetstreamHandler {

    private final DSLContext dsl;

    public JetstreamTagHandler(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void handleCreated(AtUri atUri, Json recordJson) {
        TagRecord record = new TagRecord(atUri, recordJson);

        String recordOwnerDid = record.getOwnerDid().orElseThrow();

        if(!(recordOwnerDid.equals(JCS_FORUM_DID) || dsl.fetchExists(USER_ROLE, USER_ROLE.USER_DID.eq(recordOwnerDid).and(USER_ROLE.ROLE_ID.eq(2))))) {
            System.out.println("Ignoring Tag record creation from non-admin: " + recordOwnerDid);
            return;
        }

        try {
            int inserted = dsl.insertInto(TAGS)
                .set(TAGS.ATURI, record.getAtUri().toString())
                .set(TAGS.TAG_NAME, record.getName())
                .set(TAGS.CREATED_BY, recordOwnerDid)
                .set(TAGS.CREATED_AT, record.getCreatedAt().atOffset(java.time.ZoneOffset.UTC))
                .onConflictDoNothing()
                .execute();

            if(inserted == 0){
                System.out.println("Tag record already exists in database, skipping insert.");
                return;
            }

            System.out.println("Tag record created:");
            System.out.println(" - AtUri: " + record.getAtUri());
            System.out.println(" - Name: " + record.getName());
            System.out.println(" - Created At: " + record.getCreatedAt());
        } catch (Exception e) {
            System.out.println("Error inserting tag record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void handleUpdated(AtUri atUri, Json recordJson) {
        TagRecord record = new TagRecord(atUri, recordJson);

        if(!dsl.fetchExists(TAGS, TAGS.ATURI.eq(atUri.toString()))) {
            System.out.println("Tag record does not exist in database, skipping update.");
            return;
        }

        String recordOwnerDid = record.getOwnerDid().orElseThrow();

        if(!(recordOwnerDid.equals(JCS_FORUM_DID) || dsl.fetchExists(USER_ROLE, USER_ROLE.USER_DID.eq(recordOwnerDid).and(USER_ROLE.ROLE_ID.eq(2))))) {
            System.out.println("Ignoring Tag record update from non-admin: " + recordOwnerDid);
            return;
        }

        try {
            dsl.update(TAGS)
                .set(TAGS.TAG_NAME, record.getName())
                .where(TAGS.ATURI.eq(record.getAtUri().toString()))
                .execute();

            System.out.println("Tag record updated:");
            System.out.println(" - AtUri: " + record.getAtUri());
            System.out.println(" - Name: " + record.getName());
            System.out.println(" - Created At: " + record.getCreatedAt());
        } catch (Exception e) {
            System.out.println("Error updating tag record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void handleDeleted(AtUri atUri) {
        TagRecord record = new TagRecord(atUri);

        if(!dsl.fetchExists(TAGS, TAGS.ATURI.eq(atUri.toString()))) {
            System.out.println("Tag record does not exist in database, skipping delete.");
            return;
        }

        String recordOwnerDid = record.getOwnerDid().orElseThrow();

        if(!(recordOwnerDid.equals(JCS_FORUM_DID) || dsl.fetchExists(USER_ROLE, USER_ROLE.USER_DID.eq(recordOwnerDid).and(USER_ROLE.ROLE_ID.eq(2))))) {
            System.out.println("Ignoring Tag record deletion from non-admin: " + recordOwnerDid);
            return;
        }

        try {
            dsl.deleteFrom(TAGS)
                .where(TAGS.ATURI.eq(atUri.toString()))
                .execute();
            
            System.out.println("Tag record deleted: " + atUri);
        } catch (Exception e) {
            System.out.println("Error deleting tag record: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
