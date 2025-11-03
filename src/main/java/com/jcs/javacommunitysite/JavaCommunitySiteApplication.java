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
    public static final String JCS_FORUM_DID = "did:plc:bwh2fxasbh3ieuxjyym7bmeh";

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
                System.out.println("Re-opening jetstream websocket...");
                jetstreamClient.close();
            }
            jetstreamClient = new JetstreamWebsocketClient(new URI("wss://jetstream1.us-east.bsky.network/subscribe"
                    + "?wantedCollections=dev.fudgeu.experimental.atforumv1.forum.identity"
                    + "&wantedCollections=dev.fudgeu.experimental.atforumv1.feed.question"
                    + "&wantedCollections=dev.fudgeu.experimental.atforumv1.feed.reply"
                    + "&wantedCollections=dev.fudgeu.experimental.atforumv1.admin.tag"
                    + "&wantedCollections=dev.fudgeu.experimental.atforumv1.admin.admin_grant"
                    + "&wantedCollections=dev.fudgeu.experimental.atforumv1.admin.hide_user"
                    + "&wantedCollections=dev.fudgeu.experimental.atforumv1.admin.hide_post"
                    + "&wantedCollections=dev.fudgeu.experimental.atforumv1.admin.hide_reply"
            ));

            jetstreamClient.registerJetstreamHandler(addLexiconPrefix("forum.identity"), new JetstreamForumIdentityHandler(dsl));
            jetstreamClient.registerJetstreamHandler(addLexiconPrefix("feed.reply"), new JetstreamReplyHandler(dsl));
            jetstreamClient.registerJetstreamHandler(addLexiconPrefix("feed.question"), new JetstreamQuestionHandler(dsl));
            jetstreamClient.registerJetstreamHandler(addLexiconPrefix("admin.tag"), new JetstreamTagHandler());
            jetstreamClient.registerJetstreamHandler(addLexiconPrefix("admin.admin_grant"), new JetstreamAdminGrantHandler());
            jetstreamClient.registerJetstreamHandler(addLexiconPrefix("admin.hide_user"), new JetstreamHideUserHandler());
            jetstreamClient.registerJetstreamHandler(addLexiconPrefix("admin.hide_post"), new JetstreamHidePostHandler());
            jetstreamClient.registerJetstreamHandler(addLexiconPrefix("admin.hide_reply"), new JetstreamHideReplyHandler());

            jetstreamClient.connect();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}