package com.jcs.javacommunitysite.atproto.records;

import com.google.gson.JsonObject;
import com.jcs.javacommunitysite.atproto.exceptions.AtprotoInvalidRecord;

import java.util.Optional;

public interface AtprotoRecord {
    public Optional<String> getDid();  // DID of record. will not exist if record is not in the atmosphere yet
    public boolean isValid();  // are the contents of this record valid?
    public JsonObject getAsJson() throws AtprotoInvalidRecord;  // get the lexicon-compliant JSON for this record. must be valid, or will throw
    public String getRecordCollection();
}
