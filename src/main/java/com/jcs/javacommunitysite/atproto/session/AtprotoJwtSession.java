package com.jcs.javacommunitysite.atproto.session;

import com.google.gson.JsonObject;
import com.jcs.javacommunitysite.atproto.HttpUtil;
import com.jcs.javacommunitysite.atproto.exceptions.AtprotoUnauthorized;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AtprotoJwtSession implements AtprotoAuthSession {
    private String jwt;
    private String refreshJwt;
    private String pdsHost;
    private String handle;

    // Factory methods to create sessions
    public static AtprotoJwtSession fromCredentials(String pdsHost, String handle, String password) throws IOException, AtprotoUnauthorized {
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

        return new AtprotoJwtSession(pdsHost, handle, accessJWT, refreshJWT);
    }

    public AtprotoJwtSession(String pdsHost, String handle, String jwt, String refreshJwt) {
        this.jwt = jwt;
        this.refreshJwt = refreshJwt;
        this.pdsHost = pdsHost;
        this.handle = handle;
    }

    @Override
    public String getHandle() {
        return handle;
    }

    @Override
    public String getPdsHost() {
        return pdsHost;
    }

    @Override
    public Map<String, String> getAuthHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + jwt);
        headers.put("Content-Type", "application/json");
        return headers;
    }
}
