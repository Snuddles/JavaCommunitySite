package com.jcs.javacommunitysite.atproto.jetstream.handlers;

import com.jcs.javacommunitysite.atproto.AtUri;
import com.jcs.javacommunitysite.atproto.jetstream.JetstreamHandler;
import com.jcs.javacommunitysite.atproto.records.HideUserRecord;
import dev.mccue.json.Json;
import org.jooq.DSLContext;
import static com.jcs.javacommunitysite.JavaCommunitySiteApplication.JCS_FORUM_DID;
import static com.jcs.javacommunitysite.jooq.tables.UserRole.USER_ROLE;
import static com.jcs.javacommunitysite.jooq.tables.HiddenUser.HIDDEN_USER;

public class JetstreamHideUserHandler implements JetstreamHandler {

    private final DSLContext dsl;

    public JetstreamHideUserHandler(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void handleCreated(AtUri atUri, Json recordJson) {
        HideUserRecord record = new HideUserRecord(atUri, recordJson);

        String recordOwnerDid = record.getOwnerDid().orElseThrow();

        if(!(recordOwnerDid.equals(JCS_FORUM_DID) || dsl.fetchExists(USER_ROLE, USER_ROLE.USER_DID.eq(recordOwnerDid).and(USER_ROLE.ROLE_ID.eq(2))))) {
            System.out.println("Ignoring HideUser record creation from non-admin: " + recordOwnerDid);
            return;
        }

        try {
            int inserted = dsl.insertInto(HIDDEN_USER)
                .set(HIDDEN_USER.ATURI, record.getAtUri().toString())
                .set(HIDDEN_USER.TARGET_DID, record.getTarget())
                .set(HIDDEN_USER.HIDDEN_BY, recordOwnerDid)
                .set(HIDDEN_USER.CREATED_AT, record.getCreatedAt().atOffset(java.time.ZoneOffset.UTC))
                .set(HIDDEN_USER.REASON, record.getReason())
                .onConflictDoNothing()
                .execute();

            if(inserted == 0){
                System.out.println("Hidden User record already exists in database, skipping insert.");
                return;
            }

            System.out.println("HideUser record created:");
            System.out.println(" - AtUri: " + record.getAtUri());
            System.out.println(" - Target DID: " + record.getTarget());
            System.out.println(" - Created At: " + record.getCreatedAt());
        } catch (Exception e) {
            System.out.println("Error inserting hidden user record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void handleUpdated(AtUri atUri, Json recordJson) {
        HideUserRecord record = new HideUserRecord(atUri, recordJson);

        if(!dsl.fetchExists(HIDDEN_USER, HIDDEN_USER.ATURI.eq(atUri.toString()))) {
            System.out.println("Hidden User record does not exist in database, skipping update.");
            return;
        }

        String recordOwnerDid = record.getOwnerDid().orElseThrow();

        if(!(recordOwnerDid.equals(JCS_FORUM_DID) || dsl.fetchExists(USER_ROLE, USER_ROLE.USER_DID.eq(recordOwnerDid).and(USER_ROLE.ROLE_ID.eq(2))))) {
            System.out.println("Ignoring HideUser record update from non-admin: " + recordOwnerDid);
            return;
        }

        try {
            dsl.update(HIDDEN_USER)
                .set(HIDDEN_USER.HIDDEN_BY, recordOwnerDid)
                .set(HIDDEN_USER.REASON, record.getReason())
                .where(HIDDEN_USER.ATURI.eq(record.getAtUri().toString()))
                .execute();

            System.out.println("HideUser record updated:");
            System.out.println(" - AtUri: " + record.getAtUri());
            System.out.println(" - Target DID: " + record.getTarget());
            System.out.println(" - Created At: " + record.getCreatedAt());
        } catch (Exception e) {
            System.out.println("Error updating hidden user record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void handleDeleted(AtUri atUri) {
        HideUserRecord record = new HideUserRecord(atUri);

        if(!dsl.fetchExists(HIDDEN_USER, HIDDEN_USER.ATURI.eq(atUri.toString()))) {
            System.out.println("Hidden User record does not exist in database, skipping delete.");
            return;
        }

        String recordOwnerDid = record.getOwnerDid().orElseThrow();

        if(!(recordOwnerDid.equals(JCS_FORUM_DID) || dsl.fetchExists(USER_ROLE, USER_ROLE.USER_DID.eq(recordOwnerDid).and(USER_ROLE.ROLE_ID.eq(2))))) {
            System.out.println("Ignoring HideUser record deletion from non-admin: " + recordOwnerDid);
            return;
        }

        try {
            dsl.deleteFrom(HIDDEN_USER)
                .where(HIDDEN_USER.ATURI.eq(atUri.toString()))
                .execute();
            
            System.out.println("HideUser record deleted: " + atUri);
        } catch (Exception e) {
            System.out.println("Error deleting hidden user record: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
