package com.jcs.javacommunitysite.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HeartbeatController {

    @GetMapping("/heartbeat")
    public ResponseEntity<?> heartbeat() {
        return ResponseEntity.status(200).body("OK");
    }
}



