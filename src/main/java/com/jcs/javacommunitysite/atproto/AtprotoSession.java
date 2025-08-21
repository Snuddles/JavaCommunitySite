package com.jcs.javacommunitysite.atproto;

import com.google.gson.JsonObject;
import com.jcs.javacommunitysite.atproto.exceptions.AtprotoInvalidRecord;
import com.jcs.javacommunitysite.atproto.exceptions.AtprotoUnauthorized;
import com.jcs.javacommunitysite.atproto.records.AtprotoRecord;
import lombok.Getter;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AtprotoSession {
    @Getter private String jwt;
    @Getter private String refreshJwt;
    @Getter private String pdsHost;
    @Getter private String handle;

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
        JsonObject recordJson = record.getAsJson();
        JsonObject payload = new JsonObject();
        payload.add("record", recordJson);
        payload.addProperty("repo", handle);
        payload.addProperty("collection", record.getRecordCollection());
        System.out.println(payload.toString());

        URL url = new URL(new URL(pdsHost), "/xrpc/com.atproto.repo.createRecord");

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + jwt);
        headers.put("Content-Type", "application/json");

        JsonObject response = HttpUtil.post(url, payload, headers);
        return response;
    }
}
