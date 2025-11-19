package com.jcs.javacommunitysite.atproto.jetstream.handlers;

import com.jcs.javacommunitysite.atproto.AtUri;
import com.jcs.javacommunitysite.atproto.jetstream.JetstreamHandler;
import com.jcs.javacommunitysite.atproto.records.HideReplyRecord;
import com.jcs.javacommunitysite.atproto.records.ReplyRecord;
import static com.jcs.javacommunitysite.JavaCommunitySiteApplication.JCS_FORUM_DID;
import static com.jcs.javacommunitysite.jooq.tables.UserRole.USER_ROLE;
import static com.jcs.javacommunitysite.jooq.tables.HiddenReply.HIDDEN_REPLY;
import dev.mccue.json.Json;
import java.time.ZoneOffset;
import org.jooq.DSLContext;

public class JetstreamHideReplyHandler implements JetstreamHandler {

    private final DSLContext dsl;

    public JetstreamHideReplyHandler(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void handleCreated(AtUri atUri, Json recordJson) {
        HideReplyRecord record = new HideReplyRecord(atUri, recordJson);

        String recordOwnerDid = record.getOwnerDid().orElseThrow();

        if(!(recordOwnerDid.equals(JCS_FORUM_DID) || dsl.fetchExists(USER_ROLE, USER_ROLE.USER_DID.eq(recordOwnerDid).and(USER_ROLE.ROLE_ID.eq(2))))) {
            System.out.println("Ignoring HideReply record creation from non-admin: " + recordOwnerDid);
            return;
        }

        ReplyRecord replyRecord = new ReplyRecord(record.getTarget(), dsl);
        String targetOwnerDid = replyRecord.getOwnerDid().orElseThrow();

        try {
            int inserted = dsl.insertInto(HIDDEN_REPLY)
                .set(HIDDEN_REPLY.ATURI, record.getAtUri().toString())
                .set(HIDDEN_REPLY.REPLY_ATURI, record.getTarget().toString())
                .set(HIDDEN_REPLY.TARGET_OWNER_DID, targetOwnerDid)
                .set(HIDDEN_REPLY.HIDDEN_BY, recordOwnerDid)
                .set(HIDDEN_REPLY.CREATED_AT, record.getCreatedAt().atOffset(ZoneOffset.UTC))
                .set(HIDDEN_REPLY.REASON, record.getReason())
                .onConflictDoNothing()
                .execute();

            if(inserted == 0){
                System.out.println("Hidden Reply record exists in database, skipping insert.");
                return;
            }

            System.out.println("HideReply record created:");
            System.out.println(" - AtUri: " + record.getAtUri());
            System.out.println(" - Target: " + record.getTarget());
            System.out.println(" - Created At: " + record.getCreatedAt());
        } catch(Exception e){
             System.out.println("Error inserting hidden reply record: " + e.getMessage());
             e.printStackTrace();
        }
    }

    @Override
    public void handleUpdated(AtUri atUri, Json recordJson) {
        HideReplyRecord record = new HideReplyRecord(atUri, recordJson);

        if(!dsl.fetchExists(HIDDEN_REPLY, HIDDEN_REPLY.ATURI.eq(atUri.toString()))) {
            System.out.println("Hidden Reply record does not exist in database, skipping update.");
            return;
        }

        String recordOwnerDid = record.getOwnerDid().orElseThrow();

        if(!(recordOwnerDid.equals(JCS_FORUM_DID) || dsl.fetchExists(USER_ROLE, USER_ROLE.USER_DID.eq(recordOwnerDid).and(USER_ROLE.ROLE_ID.eq(2))))) {
            System.out.println("Ignoring HideReply record update from non-admin: " + recordOwnerDid);
            return;
        }

        try {
            dsl.update(HIDDEN_REPLY)
                .set(HIDDEN_REPLY.HIDDEN_BY, recordOwnerDid)
                .set(HIDDEN_REPLY.REASON, record.getReason())
                .where(HIDDEN_REPLY.ATURI.eq(record.getAtUri().toString()))
                .execute();

            System.out.println("HideReply record updated:");
            System.out.println(" - AtUri: " + record.getAtUri());
            System.out.println(" - Target: " + record.getTarget());
            System.out.println(" - Created At: " + record.getCreatedAt());
        } catch (Exception e) {
            System.out.println("Error updating hidden reply record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void handleDeleted(AtUri atUri) {
        HideReplyRecord record = new HideReplyRecord(atUri);

        if(!dsl.fetchExists(HIDDEN_REPLY, HIDDEN_REPLY.ATURI.eq(atUri.toString()))) {
            System.out.println("Hidden Reply record does not exist in database, skipping delete.");
            return;
        }

        String recordOwnerDid = record.getOwnerDid().orElseThrow();

        if(!(recordOwnerDid.equals(JCS_FORUM_DID) || dsl.fetchExists(USER_ROLE, USER_ROLE.USER_DID.eq(recordOwnerDid).and(USER_ROLE.ROLE_ID.eq(2))))) {
            System.out.println("Ignoring HideReply record deletion from non-admin: " + recordOwnerDid);
            return;
        }

        try {
            dsl.deleteFrom(HIDDEN_REPLY)
                .where(HIDDEN_REPLY.ATURI.eq(atUri.toString()))
                .execute();

            System.out.println("HideReply record deleted: " + atUri);
        } catch (Exception e) {
            System.out.println("Error deleting hidden reply record: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
