package com.jcs.javacommunitysite;

import com.jcs.javacommunitysite.atproto.jetstream.JetstreamWebsocketClient;
import com.jcs.javacommunitysite.atproto.jetstream.handlers.*;
import jakarta.annotation.PostConstruct;
import org.jooq.DSLContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.URISyntaxException;

import static com.jcs.javacommunitysite.JavaCommunitySiteApplication.addLexiconPrefix;

@SpringBootApplication
@RestController
public class JavaCommunitySiteApplication {

    public static final String JCS_FORUM_ATURI = "at://did:plc:bwh2fxasbh3ieuxjyym7bmeh";

    public static void main(String[] args) {
        SpringApplication.run(JavaCommunitySiteApplication.class, args);
    }

    public static String addLexiconPrefix(String postfix) {
        return "dev.fudgeu.experimental.atforumv1." + postfix;
    }

    @GetMapping("/api/heartbeat")
    public ResponseEntity<String> heartbeat() {
        return ResponseEntity.ok("OK");
    }
}

@Component
class JetstreamStartupComponent {
    private static JetstreamWebsocketClient jetstreamClient;

    private final DSLContext dsl;
    
    public JetstreamStartupComponent(DSLContext dsl) {
        this.dsl = dsl;
    }

    @PostConstruct
    public void init() {
        try {
            if (jetstreamClient != null) {
                jetstreamClient.close();
            }
            jetstreamClient = new JetstreamWebsocketClient(new URI("wss://jetstream2.us-east.bsky.network/subscribe"
                    + "?wantedCollections=dev.fudgeu.experimental.atforumv1.forum.identity"
                    + "&wantedCollections=dev.fudgeu.experimental.atforumv1.forum.group"
                    + "&wantedCollections=dev.fudgeu.experimental.atforumv1.forum.category"
                    + "&wantedCollections=dev.fudgeu.experimental.atforumv1.feed.post"
                    + "&wantedCollections=dev.fudgeu.experimental.atforumv1.feed.vote"
                    + "&wantedCollections=dev.fudgeu.experimental.atforumv1.feed.reply"
            ));

            jetstreamClient.registerJetstreamHandler(addLexiconPrefix("forum.identity"), new JetstreamForumIdentityHandler(dsl));
            jetstreamClient.registerJetstreamHandler(addLexiconPrefix("forum.group"), new JetstreamForumGroupHandler(dsl));
            jetstreamClient.registerJetstreamHandler(addLexiconPrefix("forum.category"), new JetstreamForumCategoryHandler(dsl));
            jetstreamClient.registerJetstreamHandler(addLexiconPrefix("feed.post"), new JetstreamPostHandler(dsl));
            jetstreamClient.registerJetstreamHandler(addLexiconPrefix("feed.vote"), new JetstreamVoteHandler(dsl));
            jetstreamClient.registerJetstreamHandler(addLexiconPrefix("feed.reply"), new JetstreamReplyHandler(dsl));

            jetstreamClient.connect();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}