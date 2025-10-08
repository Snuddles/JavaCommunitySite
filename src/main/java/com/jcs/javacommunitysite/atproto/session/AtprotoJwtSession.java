package com.jcs.javacommunitysite.atproto.session;

import com.jcs.javacommunitysite.atproto.HttpUtil;
import com.jcs.javacommunitysite.atproto.exceptions.AtprotoUnauthorized;
import dev.mccue.json.JsonObject;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static dev.mccue.json.JsonDecoder.field;
import static dev.mccue.json.JsonDecoder.string;

public class AtprotoJwtSession implements AtprotoAuthSession {
    private final String jwt;
    private final String pdsHost;
    private final String handle;

    // Factory methods to create sessions
    public static AtprotoJwtSession fromCredentials(String pdsHost, String handle, String password) throws IOException, AtprotoUnauthorized {
        // Create JSON payload
        JsonObject.Builder payload = JsonObject.builder();
        payload.put("identifier", handle);
        payload.put("password", password);

        // Create URL and headers
        URL url = new URL(new URL(pdsHost), "/xrpc/com.atproto.server.createSession/");
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        JsonObject response = HttpUtil.post(url, payload.build(), headers);

        if (!response.containsKey("accessJwt")) throw new AtprotoUnauthorized();
        if (!response.containsKey("refreshJwt")) throw new AtprotoUnauthorized();

        String accessJWT = field(response, "accessJwt", string());
        String refreshJWT = field(response, "refreshJwt", string());

        return new AtprotoJwtSession(pdsHost, handle, accessJWT, refreshJWT);
    }

    public AtprotoJwtSession(String pdsHost, String handle, String jwt, String refreshJwt) {
        this.jwt = jwt;
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
