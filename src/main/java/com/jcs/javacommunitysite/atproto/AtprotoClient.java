package com.jcs.javacommunitysite.atproto;

import com.jcs.javacommunitysite.atproto.exceptions.AtprotoInvalidRecord;
import com.jcs.javacommunitysite.atproto.exceptions.AtprotoUnauthorized;
import com.jcs.javacommunitysite.atproto.records.AtprotoRecord;
import com.jcs.javacommunitysite.atproto.session.AtprotoAuthSession;
import dev.mccue.json.Json;
import dev.mccue.json.JsonObject;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static dev.mccue.json.JsonDecoder.field;
import static dev.mccue.json.JsonDecoder.string;

public class AtprotoClient {
    private AtprotoAuthSession session;

    public AtprotoClient(AtprotoAuthSession session) {
        this.session = session;
    }

    public void setAuthenticatedSession(AtprotoAuthSession session) {
        this.session = session;
    }

    public Json createRecord(AtprotoRecord record) throws AtprotoInvalidRecord, AtprotoUnauthorized, IOException {
        JsonObject.Builder payload = JsonObject.builder();
        payload.put("record", record.toJson());
        payload.put("repo", session.getHandle());
        payload.put("collection", record.getRecordCollection());

        URL url = new URL(new URL(session.getPdsHost()), "/xrpc/com.atproto.repo.createRecord");

        Map<String, String> headers = new HashMap<>();
        headers.putAll(session.getAuthHeaders());

        Json response = HttpUtil.post(url, payload.build(), headers);

        // Get AtUri and provide it to the record
        String atUri = field(response, "uri", string());
        AtUri atUriObj = new AtUri(atUri);
        record.setOwnerDid(atUriObj.getDid());
        record.setRecordKey(atUriObj.getRecordKey());

        return response;
    }

    public Json updateRecord(AtprotoRecord record) throws AtprotoInvalidRecord, AtprotoUnauthorized, IOException {
        if (record.getOwnerDid().isPresent() && !record.getOwnerDid().orElseThrow().equals(session.getHandle())) throw new AtprotoUnauthorized();
        JsonObject.Builder payload = JsonObject.builder();
        payload.put("record", record);
        payload.put("repo", session.getHandle());
        payload.put("collection", record.getRecordCollection());
        payload.put("rkey", record.getRecordKey().orElseThrow());

        URL url = new URL(new URL(session.getPdsHost()), "/xrpc/com.atproto.repo.putRecord");

        Map<String, String> headers = new HashMap<>();
        headers.putAll(session.getAuthHeaders());

        Json response = HttpUtil.post(url, payload.build(), headers);
        return response;
    }

    public Json deleteRecord(AtprotoRecord record) throws AtprotoInvalidRecord, AtprotoUnauthorized, IOException {
        if (record.getOwnerDid().isPresent() && !record.getOwnerDid().orElseThrow().equals(session.getHandle())) throw new AtprotoUnauthorized();
        JsonObject.Builder payload = JsonObject.builder();
        payload.put("repo", session.getHandle());
        payload.put("collection", record.getRecordCollection());
        payload.put("rkey", record.getRecordKey().orElseThrow());

        URL url = new URL(new URL(session.getPdsHost()), "/xrpc/com.atproto.repo.deleteRecord");

        Map<String, String> headers = new HashMap<>();
        headers.putAll(session.getAuthHeaders());

        Json response = HttpUtil.post(url, payload.build(), headers);
        return response;
    }
}
