package com.jcs.javacommunitysite.atproto.jetstream.handlers;

import java.time.ZoneOffset;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import com.jcs.javacommunitysite.atproto.AtUri;
import com.jcs.javacommunitysite.atproto.jetstream.JetstreamHandler;
import com.jcs.javacommunitysite.atproto.records.ReplyRecord;
import dev.mccue.json.Json;

import static com.jcs.javacommunitysite.jooq.tables.Reply.REPLY;
import static dev.mccue.json.JsonDecoder.field;
import static dev.mccue.json.JsonDecoder.string;

public class JetstreamReplyHandler implements JetstreamHandler {

    private final DSLContext dsl;
    
    public JetstreamReplyHandler(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void handleCreated(AtUri atUri, Json recordJson) {
        ReplyRecord record = new ReplyRecord(atUri, recordJson);

        System.out.println("Reply record received from AtProto!");
        System.out.println(" - Content: " + record.getContent());
        System.out.println(" - Created At: " + record.getCreatedAt().toString());
        System.out.println(" - Post root: " + record.getRoot().toString());

        if(dsl.fetchExists(REPLY, REPLY.ATURI.eq(record.getAtUri().toString()))){
            System.out.println("Reply record already exists in database, skipping insert.");
            return;
        }

        try{
            Json replyJson = record.toJson();

            dsl.insertInto(REPLY) 
            .set(REPLY.CONTENT, field(replyJson, "content", string()))
            .set(REPLY.CREATED_AT, record.getCreatedAt().atOffset(ZoneOffset.UTC))
            .set(REPLY.UPDATED_AT, record.getUpdatedAt().atOffset(ZoneOffset.UTC))
            .set(REPLY.ROOT, field(replyJson, "root", AtUri::fromJson).toString())
            .set(REPLY.ATURI, atUri.toString())
            .execute();
        } catch(Exception e){
            System.out.println("Error inserting post record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void handleUpdated(AtUri atUri, Json recordJson) {
        ReplyRecord record = new ReplyRecord(atUri, recordJson);

        if(!dsl.fetchExists(REPLY, REPLY.ATURI.eq(record.getAtUri().toString()))){
            System.out.println("Reply record does not exist in database, skipping update.");
            return;
        }

        System.out.println("Reply record received from AtProto!");
        System.out.println(" - Content: " + record.getContent());
        System.out.println(" - Created At: " + record.getCreatedAt());
        System.out.println(" - Post root: " + record.getRoot());

        try{
            Json replyJson = record.toJson();

            dsl.update(REPLY) 
            .set(REPLY.CONTENT, field(replyJson, "content", string()))
            .set(REPLY.UPDATED_AT, record.getUpdatedAt().atOffset(ZoneOffset.UTC))
            .set(REPLY.ROOT, field(replyJson, "root", AtUri::fromJson).toString())
            .where(REPLY.ATURI.eq(atUri.toString()))
            .execute();
        } catch(Exception e){
            System.out.println("Error updating reply record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void handleDeleted(AtUri atUri) {
        if(!dsl.fetchExists(REPLY, REPLY.ATURI.eq(atUri.toString()))){
            System.out.println("Reply record does not exist in database, skipping delete.");
            return;
        }

        try{
            dsl.deleteFrom(REPLY)
            .where(REPLY.ATURI.eq(atUri.toString()))
            .execute();
        } catch(Exception e){
            System.out.println("Error deleting reply record: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
