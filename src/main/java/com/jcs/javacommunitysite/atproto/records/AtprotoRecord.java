package com.jcs.javacommunitysite.atproto.records;

import com.google.gson.JsonObject;
import com.jcs.javacommunitysite.atproto.exceptions.AtprotoInvalidRecord;

import java.util.Optional;

public abstract class AtprotoRecord {
    private String did = null;

    public AtprotoRecord(JsonObject json) { }
    public AtprotoRecord() { }

    public Optional<String> getDid() {
        return Optional.of(did);
    }

    public abstract boolean isValid();  // are the contents of this record valid?
    public abstract JsonObject getAsJson() throws AtprotoInvalidRecord;  // get the lexicon-compliant JSON for this record. must be valid, or will throw
    public abstract String getRecordCollection();
}
