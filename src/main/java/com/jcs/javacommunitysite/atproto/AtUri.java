package com.jcs.javacommunitysite.atproto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.jcs.javacommunitysite.atproto.exceptions.AtprotoInvalidUri;
import dev.mccue.json.Json;
import dev.mccue.json.JsonDecoder;
import dev.mccue.json.JsonEncodable;

public class AtUri implements JsonEncodable {

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

    @Override
    public Json toJson() {
        return Json.of(toString());
    }

    @JsonCreator
    public static AtUri fromJson(Json json) {
        return new AtUri(JsonDecoder.string(json));
    }
}
