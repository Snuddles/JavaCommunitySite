package com.jcs.javacommunitysite.pages;

import com.jcs.javacommunitysite.atproto.AtUri;
import com.jcs.javacommunitysite.atproto.exceptions.AtprotoUnauthorized;
import com.jcs.javacommunitysite.atproto.records.PostRecord;
import com.jcs.javacommunitysite.atproto.records.ReplyRecord;
import com.jcs.javacommunitysite.atproto.service.AtprotoSessionService;
import com.jcs.javacommunitysite.forms.NewReplyForm;
import jakarta.servlet.http.HttpServletResponse;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Node;
import org.commonmark.node.Heading;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jooq.DSLContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import static com.jcs.javacommunitysite.jooq.tables.Post.POST;
import static com.jcs.javacommunitysite.jooq.tables.Reply.REPLY;

@Controller
public class PostController {

    private final DSLContext dsl;
    private final AtprotoSessionService sessionService;

    //Constructors

    public PostController(DSLContext dsl, AtprotoSessionService sessionService) {
        this.dsl = dsl;
        this.sessionService = sessionService;
    }

    @GetMapping("/post/{user}/{rkey}")
    public String post(Model model, @PathVariable("user") String user, @PathVariable("rkey") String rkey) {
        AtUri aturi = new AtUri(user, PostRecord.recordCollection, rkey);

        // Get post
        Map<String, Object> post;
        try {
            post = dsl.selectFrom(POST)
                    .where(POST.ATURI.eq(aturi.toString()))
                    .fetchOneMap();

            if (post == null) {
                // TODO return 404 page
            }
        } catch (Exception e) {
            return ""; // TODO
        }

        // Get replies
        com.jcs.javacommunitysite.jooq.tables.records.ReplyRecord[] postReplies = null;
        try {
            postReplies = dsl.selectFrom(REPLY)
                    .where(REPLY.ROOT.eq(aturi.toString()))
                    .orderBy(REPLY.CREATED_AT.asc())
                    .fetchArray();
        } catch (Exception e) {
            // TODO
            return "";
        }

        Parser parser = Parser.builder().build();
        Node document = parser.parse((String) post.get("content"));

        document.accept(new AbstractVisitor() {
            @Override
            public void visit(Heading heading) {
                int newLevel = Math.min(heading.getLevel() + 2, 6);
                heading.setLevel(newLevel);
                visitChildren(heading);
            }
        });


        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String contentHtml = renderer.render(document);

        model.addAttribute("did", user);
        model.addAttribute("rkey", rkey);
        model.addAttribute("title", post.get("title"));
        model.addAttribute("postContent", contentHtml);
        model.addAttribute("postTimestamp", ((OffsetDateTime) post.get("created_at")).toString());
        model.addAttribute("newReplyForm", new NewReplyForm());

        model.addAttribute("postReplies", postReplies);

        return "pages/post/page";
    }

    @PostMapping("/post/{user}/{rkey}/htmx/reply")
    public String makeReply(
            @ModelAttribute NewReplyForm newReplyForm,
            Model model,
            HttpServletResponse response,
            @PathVariable("user") String user,
            @PathVariable("rkey") String rkey
    ) {
        AtUri aturi = new AtUri(user, PostRecord.recordCollection, rkey);

        var atprotoSessionOpt = sessionService.getCurrentClient();
        if (atprotoSessionOpt.isEmpty() || !sessionService.isAuthenticated()) {
            response.setHeader("HX-Redirect", "/login?next=/post/" + user + "/" + rkey + "&msg=To reply to a post, please log in.");
            return "";
        }
        var atprotoSession = atprotoSessionOpt.get();

        ReplyRecord reply = null;
        try {
            reply = new ReplyRecord(
                newReplyForm.getContent(),
                aturi
            );
            atprotoSession.createRecord(reply);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "pages/post/htmx/replyError";
            // TODO make the errors better
        }

        // Create a fake reply record to insert into the browser
        var fakeReply = new com.jcs.javacommunitysite.jooq.tables.records.ReplyRecord();
        fakeReply.setContent(reply.getContent());
        fakeReply.setCreatedAt(reply.getCreatedAt().atOffset(ZoneOffset.UTC));
        model.addAttribute("reply", fakeReply);

        return "pages/post/htmx/reply";
    }
}
