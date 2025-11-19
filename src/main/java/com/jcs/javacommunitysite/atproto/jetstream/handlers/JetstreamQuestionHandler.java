package com.jcs.javacommunitysite.atproto.jetstream.handlers;

import com.jcs.javacommunitysite.atproto.AtUri;
import com.jcs.javacommunitysite.atproto.jetstream.JetstreamHandler;
import com.jcs.javacommunitysite.atproto.records.QuestionRecord;
import dev.mccue.json.Json;
import org.jooq.DSLContext;
import org.jooq.JSON;

import java.time.ZoneOffset;

import static com.jcs.javacommunitysite.jooq.tables.Post.POST;

public class JetstreamQuestionHandler implements JetstreamHandler {

    private final DSLContext dsl;

    public JetstreamQuestionHandler(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void handleCreated(AtUri atUri, Json recordJson) {
         QuestionRecord record = new QuestionRecord(atUri, recordJson);

         System.out.println("Post record received from AtProto!");
         System.out.println(" - AtUri: " + record.getAtUri());
         System.out.println(" - Title: " + record.getTitle());
         System.out.println(" - Content: " + record.getContent());
         System.out.println(" - Forum: " + record.getForum());
         System.out.println(" - Tags: " + record.getTags());
         System.out.println(" - Created At: " + record.getCreatedAt());
         System.out.println(" - Updated At: " + record.getUpdatedAt());

         try{
             int inserted = dsl.insertInto(POST)
             .set(POST.TITLE, record.getTitle())
             .set(POST.CONTENT, record.getContent())
             .set(POST.CREATED_AT, record.getCreatedAt().atOffset(ZoneOffset.UTC))
             .set(POST.UPDATED_AT, record.getUpdatedAt() != null ? record.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)
             .set(POST.TAGS, JSON.valueOf(Json.of(record.getTags(), Json::of).toString()))
             .set(POST.ATURI, atUri.toString())
             .set(POST.IS_OPEN, record.isOpen())
             .set(POST.IS_DELETED, false)
             .set(POST.OWNER_DID, record.getAtUri().getDid())
             .onConflictDoNothing()
             .execute();
             
             if(inserted == 0){
                 System.out.println("Post record already exists in database, skipping insert.");
             }
         } catch(Exception e){
             System.out.println("Error inserting post record: " + e.getMessage());
             e.printStackTrace();
         }
    }

    @Override
    public void handleUpdated(AtUri atUri, Json recordJson) {
         QuestionRecord record = new QuestionRecord(atUri, recordJson);

         if(!dsl.fetchExists(POST, POST.ATURI.eq(record.getAtUri().toString()))){
             System.out.println("Post record does not exist in database, skipping update.");
             return;
         }

         System.out.println("Post record received from AtProto!");
         System.out.println(" - AtUri: " + record.getAtUri());
         System.out.println(" - Title: " + record.getTitle());
         System.out.println(" - Content: " + record.getContent());
         System.out.println(" - Forum: " + record.getForum());
         System.out.println(" - Tags: " + record.getTags());
         System.out.println(" - Created At: " + record.getCreatedAt());
         System.out.println(" - Updated At: " + record.getUpdatedAt());

         try{
             dsl.update(POST)
                 .set(POST.TITLE, record.getTitle())
                 .set(POST.CONTENT, record.getContent())
                 .set(POST.UPDATED_AT, record.getUpdatedAt().atOffset(ZoneOffset.UTC))
                 .set(POST.IS_OPEN, record.isOpen())
                 .set(POST.TAGS, JSON.valueOf(Json.of(record.getTags(), Json::of).toString()))
                 .where(POST.ATURI.eq(atUri.toString()))
                 .execute();
         } catch(Exception e){
             System.out.println("Error updating post record: " + e.getMessage());
             e.printStackTrace();
         }
    }

    @Override
    public void handleDeleted(AtUri atUri) {
        QuestionRecord record = new QuestionRecord(atUri);

        if(!dsl.fetchExists(POST, POST.ATURI.eq(record.getAtUri().toString()))){
            System.out.println("Post record does not exist in database, skipping delete.");
            return;
        }

        try{
            dsl.update(POST)
                .set(POST.IS_DELETED, true)
                .where(POST.ATURI.eq(atUri.toString()))
                .execute();
        } catch(Exception e){
            System.out.println("Error deleting post record: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
