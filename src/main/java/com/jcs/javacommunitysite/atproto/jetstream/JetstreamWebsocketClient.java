package com.jcs.javacommunitysite.atproto.jetstream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jcs.javacommunitysite.atproto.AtUri;
import com.jcs.javacommunitysite.atproto.exceptions.AtprotoJetstreamException;
import com.jcs.javacommunitysite.atproto.records.AtprotoRecord;
import com.jcs.javacommunitysite.atproto.records.PostRecord;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class JetstreamWebsocketClient extends WebSocketClient {
    private Map<String, JetstreamHandler> handlers = new HashMap<>();

    public JetstreamWebsocketClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("Websocket opened!");
    }

    @Override
    public void onMessage(String s) {
        // Attempt to convert message to JsonObject
        JsonElement messageElement = JsonParser.parseString(s);
        JsonObject responseObject = messageElement.getAsJsonObject();

        // Get some base info
        String userDid = responseObject.get("did").getAsString();
        String kind = responseObject.get("kind").getAsString();

        // Right now we're only looking at commits. It'll prolly be useful to look at the other types later (identity & account)
        if (!kind.equals("commit")) return;

        // Get commit and determine commit operation and other record info
        JsonObject commit = responseObject.get("commit").getAsJsonObject();
        String commitOperation = commit.get("operation").getAsString();
        String recordCollection = commit.get("collection").getAsString();
        String recordKey = commit.get("rkey").getAsString();
        AtUri atUri = new AtUri(userDid, recordCollection, recordKey);

        // Get handler for this collection. Throw if handler not found
        JetstreamHandler handler = handlers.get(recordCollection);
        if (handler == null) {
            throw new AtprotoJetstreamException("Unexpected collection found: " + recordCollection);
        }

        switch (commitOperation) {
            case "create":
                JsonObject recordJson = commit.get("record").getAsJsonObject();
                handler.handleCreated(atUri, recordJson);
                break;
            case "update":
                JsonObject updatedFields = commit.get("record").getAsJsonObject();
                handler.handleUpdated(atUri, updatedFields);
                break;
            case "delete":
                handler.handleDeleted(atUri);
                break;
            default:
                throw new AtprotoJetstreamException("Unknown commit operation: " + commitOperation);
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        System.out.println("Websocket closed! " + i + " " + s + " " + b);
    }

    @Override
    public void onError(Exception e) {
        System.out.println("Websocket error:");
        e.printStackTrace();
    }

    public <T extends AtprotoRecord> void registerJetstreamHandler(String recordCollection, JetstreamHandler handler) {
        handlers.put(recordCollection, handler);
    }
}
