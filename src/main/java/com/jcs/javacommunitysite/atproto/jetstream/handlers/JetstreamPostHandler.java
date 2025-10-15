package com.jcs.javacommunitysite.atproto.jetstream.handlers;

import org.jooq.DSLContext;
import org.jooq.JSONB;
import com.jcs.javacommunitysite.atproto.AtUri;
import com.jcs.javacommunitysite.atproto.jetstream.JetstreamHandler;
import com.jcs.javacommunitysite.atproto.records.PostRecord;
import dev.mccue.json.Json;
import static com.jcs.javacommunitysite.jooq.tables.Post.POST;
import static dev.mccue.json.JsonDecoder.field;
import static dev.mccue.json.JsonDecoder.optionalNullableField;
import static dev.mccue.json.JsonDecoder.string;
import java.time.ZoneOffset;

public class JetstreamPostHandler implements JetstreamHandler {

    private final DSLContext dsl;
    
    public JetstreamPostHandler(DSLContext dsl) {
        this.dsl = dsl;
    }

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

        if(dsl.fetchExists(POST, POST.ATURI.eq(record.getAtUri().toString()))){
            System.out.println("Post record already exists in database, skipping insert.");
            return;
        }

        try{
            Json postJson = record.toJson();

            dsl.insertInto(POST) 
            .set(POST.TITLE, field(postJson, "title", string()))
            .set(POST.CONTENT, field(postJson, "content", string()))
            .set(POST.CREATED_AT, record.getCreatedAt().atOffset(ZoneOffset.UTC))
            .set(POST.UPDATED_AT, record.getUpdatedAt().atOffset(ZoneOffset.UTC))
            .set(POST.CATEGORY_ATURI, field(postJson, "category", string()))
            .set(POST.FORUM, field(postJson, "forum", string()))
            .set(POST.TAGS, JSONB.valueOf(field(postJson, "tags", Json::of).toString()))
            .set(POST.SOLUTION, optionalNullableField(postJson, "solution", string(), null))
            .set(POST.ATURI, atUri.toString())
            .execute();
        } catch(Exception e){
            System.out.println("Error inserting post record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void handleUpdated(AtUri atUri, Json recordJson) {
        PostRecord record = new PostRecord(atUri, recordJson);

        if(!dsl.fetchExists(POST, POST.ATURI.eq(record.getAtUri().toString()))){
            System.out.println("Post record does not exist in database, skipping update.");
            return;
        }
        
        System.out.println("Post record received from AtProto!");
        System.out.println(" - Title: " + record.getTitle());
        System.out.println(" - Content: " + record.getContent());
        System.out.println(" - Category: " + record.getCategory());
        System.out.println(" - Forum: " + record.getForum());
        System.out.println(" - Created At: " + record.getCreatedAt());
        System.out.println(" - Updated At: " + record.getUpdatedAt());

        try{
            Json postJson = record.toJson();

            dsl.update(POST) 
                .set(POST.TITLE, field(postJson, "title", string()))
                .set(POST.CONTENT, field(postJson, "content", string()))
                .set(POST.UPDATED_AT, record.getUpdatedAt().atOffset(ZoneOffset.UTC))
                .set(POST.CATEGORY_ATURI, field(postJson, "category", AtUri::fromJson).toString()) // Convert AtUri to string
                .set(POST.TAGS, JSONB.valueOf(field(postJson, "tags", Json::of).toString()))
                .set(POST.SOLUTION, optionalNullableField(postJson, "solution", AtUri::fromJson, null) != null ? 
                     optionalNullableField(postJson, "solution", AtUri::fromJson, null).toString() : null)
                .where(POST.ATURI.eq(atUri.toString()))
                .execute();
        } catch(Exception e){
            System.out.println("Error updating post record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void handleDeleted(AtUri atUri) {
        PostRecord record = new PostRecord(atUri);

        if(!dsl.fetchExists(POST, POST.ATURI.eq(record.getAtUri().toString()))){
            System.out.println("Post record does not exist in database, skipping delete.");
            return;
        }

        try{
            dsl.deleteFrom(POST)
            .where(POST.ATURI.eq(atUri.toString()))
            .execute();
        } catch(Exception e){
            System.out.println("Error deleting post record: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
