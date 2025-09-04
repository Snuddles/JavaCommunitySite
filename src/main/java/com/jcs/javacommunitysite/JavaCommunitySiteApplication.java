package com.jcs.javacommunitysite;

import com.jcs.javacommunitysite.atproto.jetstream.JetstreamWebsocketClient;
import com.jcs.javacommunitysite.atproto.jetstream.handlers.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URI;
import java.net.URISyntaxException;

@SpringBootApplication
public class JavaCommunitySiteApplication {

    public static void main(String[] args) {
        try {
            JetstreamWebsocketClient wsClient = new JetstreamWebsocketClient(new URI("wss://jetstream2.us-east.bsky.network/subscribe?wantedCollections=dev.fudgeu.experimental.atforumv1.forum.identity"));
            System.out.println("CREATED WS CLIENT");

            wsClient.registerJetstreamHandler("dev.fudgeu.experimental.atforumv1.forum.identity", new JetstreamForumIdentityHandler());
            wsClient.registerJetstreamHandler("dev.fudgeu.experimental.atforumv1.forum.group", new JetstreamForumGroupHandler());
            wsClient.registerJetstreamHandler("dev.fudgeu.experimental.atforumv1.forum.category", new JetstreamForumCategoryHandler());
            wsClient.registerJetstreamHandler("dev.fudgeu.experimental.atforumv1.feed.post", new JetstreamPostHandler());
            wsClient.registerJetstreamHandler("dev.fudgeu.experimental.atforumv1.feed.vote", new JetstreamVoteHandler());
            wsClient.registerJetstreamHandler("dev.fudgeu.experimental.atforumv1.feed.reply", new JetstreamReplyHandler());

            wsClient.connect();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        SpringApplication.run(JavaCommunitySiteApplication.class, args);
    }
}