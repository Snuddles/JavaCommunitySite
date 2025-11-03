package com.jcs.javacommunitysite.atproto;

import com.jcs.javacommunitysite.atproto.exceptions.AtprotoInvalidRecord;
import com.jcs.javacommunitysite.atproto.exceptions.AtprotoUnauthorized;
import com.jcs.javacommunitysite.atproto.records.AtprotoRecord;
import com.jcs.javacommunitysite.atproto.session.AtprotoAuthSession;
import dev.mccue.json.Json;
import dev.mccue.json.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
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

        URI uri = URI.create(session.getPdsHost()).resolve("/xrpc/com.atproto.repo.createRecord");
        URL url = uri.toURL();

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

        String handle = session.getHandle(); 
        String handleDid = resolveDidFromHandle(handle);

        if ((record.getOwnerDid().isPresent() && record.getOwnerDid().orElseThrow().equals(handleDid)) == false) {
            throw new AtprotoUnauthorized();
        }

        JsonObject.Builder payload = JsonObject.builder();
        payload.put("record", record.toJson());
        payload.put("repo", session.getHandle());
        payload.put("collection", record.getRecordCollection());
        payload.put("rkey", record.getRecordKey().orElseThrow());

        URI uri = URI.create(session.getPdsHost()).resolve("/xrpc/com.atproto.repo.putRecord");
        URL url = uri.toURL();

        Map<String, String> headers = new HashMap<>();
        headers.putAll(session.getAuthHeaders());

        Json response = HttpUtil.post(url, payload.build(), headers);
        return response;
    }

    public Json deleteRecord(AtprotoRecord record) throws AtprotoInvalidRecord, AtprotoUnauthorized, IOException {
        String handle = session.getHandle(); 
        String handleDid = resolveDidFromHandle(handle);

        if ((record.getOwnerDid().isPresent() && record.getOwnerDid().orElseThrow().equals(handleDid)) == false) {
            throw new AtprotoUnauthorized();
        }

        JsonObject.Builder payload = JsonObject.builder();
        payload.put("repo", session.getHandle());
        payload.put("collection", record.getRecordCollection());
        payload.put("rkey", record.getRecordKey().orElseThrow());

        URI uri = URI.create(session.getPdsHost()).resolve("/xrpc/com.atproto.repo.deleteRecord");
        URL url = uri.toURL();

        Map<String, String> headers = new HashMap<>();
        headers.putAll(session.getAuthHeaders());

        Json response = HttpUtil.post(url, payload.build(), headers);
        return response;
    }

    public Json deleteRecord(AtUri atUri) throws AtprotoInvalidRecord, AtprotoUnauthorized, IOException {
        String handle = session.getHandle();
        String handleDid = resolveDidFromHandle(handle);

        if (!atUri.getDid().equals(handleDid)) {
            throw new AtprotoUnauthorized();
        }

        JsonObject.Builder payload = JsonObject.builder();
        payload.put("repo", session.getHandle());
        payload.put("collection", atUri.getCollection());
        payload.put("rkey", atUri.getRecordKey());

        URI uri = URI.create(session.getPdsHost()).resolve("/xrpc/com.atproto.repo.deleteRecord");
        URL url = uri.toURL();

        Map<String, String> headers = new HashMap<>();
        headers.putAll(session.getAuthHeaders());

        Json response = HttpUtil.post(url, payload.build(), headers);
        return response;
    }

    String resolveDidFromHandle(String handle) throws IOException {
        URI uri = URI.create("https://bsky.social").resolve("/xrpc/com.atproto.identity.resolveHandle?handle=" + URLEncoder.encode(handle, StandardCharsets.UTF_8));

        // Open connection
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("GET");

        // Read response
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String response = reader.lines().collect(Collectors.joining());
            JsonObject json = (JsonObject) Json.readString(response);
            return field(json, "did", string());
        }
    }

    public AtprotoAuthSession getSession() {
        return session;
    }

    public boolean isSameUser(String didOrHandle) throws IOException {
        String did = didOrHandle;
        if (!didOrHandle.startsWith("did:")) {
            resolveDidFromHandle(didOrHandle);
        }

        String sessionDid = resolveDidFromHandle(session.getHandle());

        return did.equals(sessionDid);

    }
}
