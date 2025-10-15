package com.jcs.javacommunitysite.atproto.records;

import com.jcs.javacommunitysite.atproto.AtUri;
import com.jcs.javacommunitysite.atproto.exceptions.AtprotoInvalidUri;
import dev.mccue.json.Json;
import dev.mccue.json.JsonEncodable;

import java.util.Optional;

public abstract class AtprotoRecord implements JsonEncodable {
    private String ownerDid = null;
    private String recordKey = null;

    public AtprotoRecord(AtUri atUri) {
        this.setAtUri(atUri);
    }

    public AtprotoRecord(AtUri atUri, Json json) {
        this.setAtUri(atUri);
    }

    public AtprotoRecord() { }

    public Optional<String> getRecordKey() {
        return Optional.ofNullable(recordKey);
    }

    public void setRecordKey(String recordKey) {
        this.recordKey = recordKey;
    }

    public Optional<String> getOwnerDid() {
        return Optional.ofNullable(ownerDid);
    }

    public void setOwnerDid(String ownerDid) {
        this.ownerDid = ownerDid;
    }

    public AtUri getAtUri() {
        if (ownerDid == null || recordKey == null) return null;
        return new AtUri(ownerDid, getRecordCollection(), recordKey);
    }

    public void setAtUri(AtUri atUri) {
        this.ownerDid = atUri.getDid();
        if (!this.getRecordCollection().equals(atUri.getCollection()))
            throw new AtprotoInvalidUri("Collection type does not match record's own collection type");
        this.recordKey = atUri.getRecordKey();
    }

    public abstract boolean isValid();  // are the contents of this record valid?
    public abstract String getRecordCollection();
}
