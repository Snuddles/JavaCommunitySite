package com.jcs.javacommunitysite.atproto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.jcs.javacommunitysite.atproto.exceptions.AtprotoInvalidRecord;
import com.jcs.javacommunitysite.atproto.exceptions.AtprotoUnauthorized;
import com.jcs.javacommunitysite.atproto.records.AtprotoRecord;
import com.jcs.javacommunitysite.atproto.session.AtprotoAuthSession;
import com.jcs.javacommunitysite.atproto.typeadapters.AtprotoColorAdapter;
import com.jcs.javacommunitysite.atproto.typeadapters.AtprotoDatetimeAdapter;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class AtprotoClient {
    private AtprotoAuthSession session;
    private Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(Instant.class, new AtprotoDatetimeAdapter())
            .registerTypeAdapter(Color.class, new AtprotoColorAdapter())
            .create();

    public AtprotoClient(AtprotoAuthSession session) {
        this.session = session;
    }

    public void setAuthenticatedSession(AtprotoAuthSession session) {
        this.session = session;
    }

    public JsonObject createRecord(AtprotoRecord record) throws AtprotoInvalidRecord, AtprotoUnauthorized, IOException {
        JsonObject payload = new JsonObject();
        payload.add("record", gson.toJsonTree(record));
        payload.addProperty("repo", session.getHandle());
        payload.addProperty("collection", record.getRecordCollection());

        URL url = new URL(new URL(session.getPdsHost()), "/xrpc/com.atproto.repo.createRecord");

        Map<String, String> headers = new HashMap<>();
        headers.putAll(session.getAuthHeaders());

        JsonObject response = HttpUtil.post(url, payload, headers);

        // Get AtUri and provide it to the record
        String atUri = response.get("uri").getAsString();
        AtUri atUriObj = new AtUri(atUri);
        record.setOwnerDid(atUriObj.getDid());
        record.setRecordKey(atUriObj.getRecordKey());

        return response;
    }

    public JsonObject updateRecord(AtprotoRecord record) throws AtprotoInvalidRecord, AtprotoUnauthorized, IOException {
        if (record.getOwnerDid().isPresent() && !record.getOwnerDid().orElseThrow().equals(session.getHandle())) throw new AtprotoUnauthorized();
        JsonObject payload = new JsonObject();
        payload.add("record", gson.toJsonTree(record));
        payload.addProperty("repo", session.getHandle());
        payload.addProperty("collection", record.getRecordCollection());
        payload.addProperty("rkey", record.getRecordKey().orElseThrow());

        URL url = new URL(new URL(session.getPdsHost()), "/xrpc/com.atproto.repo.putRecord");

        Map<String, String> headers = new HashMap<>();
        headers.putAll(session.getAuthHeaders());

        JsonObject response = HttpUtil.post(url, payload, headers);
        return response;
    }

    public JsonObject deleteRecord(AtprotoRecord record) throws AtprotoInvalidRecord, AtprotoUnauthorized, IOException {
        if (record.getOwnerDid().isPresent() && !record.getOwnerDid().orElseThrow().equals(session.getHandle())) throw new AtprotoUnauthorized();
        JsonObject payload = new JsonObject();
        payload.addProperty("repo", session.getHandle());
        payload.addProperty("collection", record.getRecordCollection());
        payload.addProperty("rkey", record.getRecordKey().orElseThrow());

        URL url = new URL(new URL(session.getPdsHost()), "/xrpc/com.atproto.repo.deleteRecord");

        Map<String, String> headers = new HashMap<>();
        headers.putAll(session.getAuthHeaders());

        JsonObject response = HttpUtil.post(url, payload, headers);
        return response;
    }
}
