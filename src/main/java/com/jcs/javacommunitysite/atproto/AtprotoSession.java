package com.jcs.javacommunitysite.atproto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.jcs.javacommunitysite.atproto.exceptions.AtprotoInvalidRecord;
import com.jcs.javacommunitysite.atproto.exceptions.AtprotoUnauthorized;
import com.jcs.javacommunitysite.atproto.records.AtprotoRecord;
import com.jcs.javacommunitysite.atproto.typeadapters.AtprotoColorAdapter;
import com.jcs.javacommunitysite.atproto.typeadapters.AtprotoDatetimeAdapter;
import lombok.Getter;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class AtprotoSession {
    @Getter private String jwt;
    @Getter private String refreshJwt;
    @Getter private String pdsHost;
    @Getter private String handle;

    private Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(Instant.class, new AtprotoDatetimeAdapter())
            .registerTypeAdapter(Color.class, new AtprotoColorAdapter())
            .create();

    // Factory methods to create sessions
    public static AtprotoSession fromCredentials(String pdsHost, String handle, String password) throws IOException, AtprotoUnauthorized {
        // Create JSON payload
        JsonObject payload = new JsonObject();
        payload.addProperty("identifier", handle);
        payload.addProperty("password", password);

        // Create URL and headers
        URL url = new URL(new URL(pdsHost), "/xrpc/com.atproto.server.createSession/");
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        JsonObject response = HttpUtil.post(url, payload, headers);

        if (!response.has("accessJwt")) throw new AtprotoUnauthorized();
        if (!response.has("refreshJwt")) throw new AtprotoUnauthorized();

        String accessJWT = response.get("accessJwt").getAsString();
        String refreshJWT = response.get("refreshJwt").getAsString();

        return new AtprotoSession(pdsHost, handle, accessJWT, refreshJWT);
    }

    // TODO create an oauth-based session factory

    public AtprotoSession(String pdsHost, String handle, String jwt, String refreshJwt) {
        this.jwt = jwt;
        this.refreshJwt = refreshJwt;
        this.pdsHost = pdsHost;
        this.handle = handle;
    }

    public JsonObject createRecord(AtprotoRecord record) throws AtprotoInvalidRecord, AtprotoUnauthorized, IOException {
        JsonObject payload = new JsonObject();
        payload.add("record", gson.toJsonTree(record));
        payload.addProperty("repo", handle);
        payload.addProperty("collection", record.getRecordCollection());

        URL url = new URL(new URL(pdsHost), "/xrpc/com.atproto.repo.createRecord");

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + jwt);
        headers.put("Content-Type", "application/json");

        JsonObject response = HttpUtil.post(url, payload, headers);

        // Get AtUri and provide it to the record
        String atUri = response.get("uri").getAsString();
        AtUri atUriObj = new AtUri(atUri);
        record.setOwnerDid(atUriObj.getDid());
        record.setRecordKey(atUriObj.getRecordKey());

        return response;
    }

    public JsonObject updateRecord(AtprotoRecord record) throws AtprotoInvalidRecord, AtprotoUnauthorized, IOException {
        if (record.getOwnerDid().isPresent() && !record.getOwnerDid().orElseThrow().equals(handle)) throw new AtprotoUnauthorized();
        JsonObject payload = new JsonObject();
        payload.add("record", gson.toJsonTree(record));
        payload.addProperty("repo", handle);
        payload.addProperty("collection", record.getRecordCollection());
        payload.addProperty("rkey", record.getRecordKey().orElseThrow());

        URL url = new URL(new URL(pdsHost), "/xrpc/com.atproto.repo.putRecord");

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + jwt);
        headers.put("Content-Type", "application/json");

        JsonObject response = HttpUtil.post(url, payload, headers);
        return response;
    }

    public JsonObject deleteRecord(AtprotoRecord record) throws AtprotoInvalidRecord, AtprotoUnauthorized, IOException {
        if (record.getOwnerDid().isPresent() && !record.getOwnerDid().orElseThrow().equals(handle)) throw new AtprotoUnauthorized();
        JsonObject payload = new JsonObject();
        payload.addProperty("repo", handle);
        payload.addProperty("collection", record.getRecordCollection());
        payload.addProperty("rkey", record.getRecordKey().orElseThrow());

        URL url = new URL(new URL(pdsHost), "/xrpc/com.atproto.repo.deleteRecord");

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + jwt);
        headers.put("Content-Type", "application/json");

        JsonObject response = HttpUtil.post(url, payload, headers);
        return response;
    }
}
