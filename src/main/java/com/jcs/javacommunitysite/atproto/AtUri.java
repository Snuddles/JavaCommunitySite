package com.jcs.javacommunitysite.atproto;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.jcs.javacommunitysite.atproto.exceptions.AtprotoInvalidUri;

import java.lang.reflect.Type;

public class AtUri implements JsonSerializer<AtUri> {

    private String did;
    private String collection;
    private String recordKey;

    public AtUri(String atUri) {
        // Parse whole AtUri
        if (!atUri.startsWith("at://")) {
            throw new AtprotoInvalidUri("AtUri must start with 'at://'");
        }
        String[] parts = atUri.substring(5).split("/");
        if (parts.length != 3) {
            throw new AtprotoInvalidUri("AtUri must be in the format 'at://<did>/<collection>/<recordKey>'");
        }
        this.did = parts[0];
        this.collection = parts[1];
        this.recordKey = parts[2];
    }

    public AtUri(String did, String collection, String record) {
        this.did = did;
        this.collection = collection;
        this.recordKey = record;
    }

    @Override
    public JsonElement serialize(AtUri tAtUri, Type type, JsonSerializationContext jsonSerializationContext) {
        return jsonSerializationContext.serialize(this.toString());
    }

    public String toString() {
        return "at://" +
                did + "/" +
                collection + "/" +
                recordKey;
    }

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getRecordKey() {
        return recordKey;
    }

    public void setRecordKey(String recordKey) {
        this.recordKey = recordKey;
    }
}
