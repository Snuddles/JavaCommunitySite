package com.jcs.javacommunitysite.controller;

import com.google.gson.JsonObject;
import com.jcs.javacommunitysite.atproto.AtprotoSession;
import com.jcs.javacommunitysite.atproto.records.PostRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HeartbeatController {

    @GetMapping("/heartbeat")
    public ResponseEntity<?> heartbeat() {
        try {
            AtprotoSession session = AtprotoSession.fromCredentials("https://bsky.social/", "jcstest2.bsky.social", "PASS");
            PostRecord post = new PostRecord("Hello! This is a test from Java!");
            JsonObject resp = session.createRecord(post);

            return ResponseEntity.status(200).body("OK - " + session.getJwt());
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(500).body("BAD");
        }
    }
}



