package com.jcs.javacommunitysite.atproto.jetstream;

import com.jcs.javacommunitysite.atproto.AtUri;
import dev.mccue.json.Json;

public interface JetstreamHandler {
    public void handleCreated(AtUri atUri, Json recordJson);
    public void handleUpdated(AtUri atUri, Json recordJson);
    public void handleDeleted(AtUri atUri);
}
