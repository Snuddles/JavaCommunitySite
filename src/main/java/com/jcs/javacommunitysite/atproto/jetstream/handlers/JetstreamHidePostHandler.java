package com.jcs.javacommunitysite.atproto.jetstream.handlers;

import com.jcs.javacommunitysite.atproto.AtUri;
import com.jcs.javacommunitysite.atproto.jetstream.JetstreamHandler;
import com.jcs.javacommunitysite.atproto.records.HidePostRecord;
import com.jcs.javacommunitysite.atproto.records.QuestionRecord;
import dev.mccue.json.Json;
import static com.jcs.javacommunitysite.JavaCommunitySiteApplication.JCS_FORUM_DID;
import static com.jcs.javacommunitysite.jooq.tables.UserRole.USER_ROLE;
import static com.jcs.javacommunitysite.jooq.tables.HiddenPost.HIDDEN_POST;
import java.time.ZoneOffset;
import org.jooq.DSLContext;

public class JetstreamHidePostHandler implements JetstreamHandler {

    private final DSLContext dsl;

    public JetstreamHidePostHandler(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void handleCreated(AtUri atUri, Json recordJson) {
        HidePostRecord record = new HidePostRecord(atUri, recordJson);

        if(dsl.fetchExists(HIDDEN_POST, HIDDEN_POST.ATURI.eq(atUri.toString()))) {
            System.out.println("Hidden Post record exists in database, skipping insert.");
            return;
        }

        String recordOwnerDid = record.getOwnerDid().orElseThrow();

        if(!(recordOwnerDid.equals(JCS_FORUM_DID) || dsl.fetchExists(USER_ROLE, USER_ROLE.USER_DID.eq(recordOwnerDid).and(USER_ROLE.ROLE_ID.eq(2))))) {
            System.out.println("Ignoring HidePost record creation from non-admin: " + recordOwnerDid);
            return;
        }

        QuestionRecord questionRecord = new QuestionRecord(record.getTarget(), dsl);
        String targetOwnerDid = questionRecord.getOwnerDid().orElseThrow();

        try {
            dsl.insertInto(HIDDEN_POST)
                .set(HIDDEN_POST.ATURI, record.getAtUri().toString())
                .set(HIDDEN_POST.POST_ATURI, record.getTarget().toString())
                .set(HIDDEN_POST.TARGET_OWNER_DID, targetOwnerDid)
                .set(HIDDEN_POST.HIDDEN_BY, recordOwnerDid)
                .set(HIDDEN_POST.CREATED_AT, record.getCreatedAt().atOffset(ZoneOffset.UTC))
                .set(HIDDEN_POST.REASON, record.getReason())
                .execute();

            System.out.println("HidePost record created:");
            System.out.println(" - AtUri: " + record.getAtUri());
            System.out.println(" - Target: " + record.getTarget());
            System.out.println(" - Created At: " + record.getCreatedAt());
        } catch(Exception e){
             System.out.println("Error inserting hidden post record: " + e.getMessage());
             e.printStackTrace();
        }
    }

    @Override
    public void handleUpdated(AtUri atUri, Json recordJson) {
        HidePostRecord record = new HidePostRecord(atUri, recordJson);

        if(!dsl.fetchExists(HIDDEN_POST, HIDDEN_POST.ATURI.eq(atUri.toString()))) {
            System.out.println("Hidden Post record does not exist in database, skipping update.");
            return;
        }

        String recordOwnerDid = record.getOwnerDid().orElseThrow();

        if(!(recordOwnerDid.equals(JCS_FORUM_DID) || dsl.fetchExists(USER_ROLE, USER_ROLE.USER_DID.eq(recordOwnerDid).and(USER_ROLE.ROLE_ID.eq(2))))) {
            System.out.println("Ignoring HidePost record update from non-admin: " + recordOwnerDid);
            return;
        }

        try {
            dsl.update(HIDDEN_POST)
                .set(HIDDEN_POST.HIDDEN_BY, recordOwnerDid)
                .set(HIDDEN_POST.REASON, record.getReason())
                .where(HIDDEN_POST.ATURI.eq(record.getAtUri().toString()))
                .execute();

            System.out.println("HidePost record updated:");
            System.out.println(" - AtUri: " + record.getAtUri());
            System.out.println(" - Target: " + record.getTarget());
            System.out.println(" - Created At: " + record.getCreatedAt());
        } catch (Exception e) {
            System.out.println("Error updating hidden post record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void handleDeleted(AtUri atUri) {
        HidePostRecord record = new HidePostRecord(atUri);

        if(!dsl.fetchExists(HIDDEN_POST, HIDDEN_POST.ATURI.eq(atUri.toString()))) {
            System.out.println("Hidden Post record does not exist in database, skipping delete.");
            return;
        }

        String recordOwnerDid = record.getOwnerDid().orElseThrow();

        if(!(recordOwnerDid.equals(JCS_FORUM_DID) || dsl.fetchExists(USER_ROLE, USER_ROLE.USER_DID.eq(recordOwnerDid).and(USER_ROLE.ROLE_ID.eq(2))))) {
            System.out.println("Ignoring HidePost record deletion from non-admin: " + recordOwnerDid);
            return;
        }

        try {
            dsl.deleteFrom(HIDDEN_POST)
                .where(HIDDEN_POST.ATURI.eq(atUri.toString()))
                .execute();
            
            System.out.println("HidePost record deleted: " + atUri);
        } catch (Exception e) {
            System.out.println("Error deleting hidden post record: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
