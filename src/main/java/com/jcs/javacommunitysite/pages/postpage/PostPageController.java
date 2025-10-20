package com.jcs.javacommunitysite.pages.postpage;

import com.jcs.javacommunitysite.atproto.AtUri;
import com.jcs.javacommunitysite.atproto.AtprotoClient;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import static com.jcs.javacommunitysite.jooq.tables.Post.POST;
import static com.jcs.javacommunitysite.jooq.tables.Reply.REPLY;

@Controller
public class PostPageController {

    private final DSLContext dsl;
    private final AtprotoSessionService sessionService;

    public PostPageController(DSLContext dsl, AtprotoSessionService sessionService) {
        this.dsl = dsl;
        this.sessionService = sessionService;
    }

    @GetMapping("/post/{userDid}/{postRKey}")
    public String getPost(Model model, @PathVariable("userDid") String userDid, @PathVariable("postRKey") String postRKey) {

        AtUri aturi = new AtUri(userDid, PostRecord.recordCollection, postRKey);

        // Get post
        Map<String, Object> post;
        try {
            post = dsl.selectFrom(POST)
                    .where(POST.ATURI.eq(aturi.toString()))
                    .fetchOneMap();

            if (post == null) {
                return ""; // TODO return 404
            }
        } catch (Exception e) {
            return ""; // TODO
        }

        // Get replies
        com.jcs.javacommunitysite.jooq.tables.records.ReplyRecord[] postReplies;
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

        model.addAttribute("aturi", aturi);
        model.addAttribute("title", post.get("title"));
        model.addAttribute("postContent", contentHtml);
        model.addAttribute("postTimestamp", ((OffsetDateTime) post.get("created_at")).toString());
        model.addAttribute("newReplyForm", new NewReplyForm());

        model.addAttribute("postReplies", postReplies);

        return "pages/post/page";
    }

    @PostMapping("/post/{userDid}/{postRKey}/htmx/reply")
    public String createReply(
            @ModelAttribute NewReplyForm newReplyForm,
            Model model,
            HttpServletResponse response,
            @PathVariable("userDid") String userDid,
            @PathVariable("postRKey") String postRKey
    ) {
        var clientOpt = sessionService.getCurrentClient();
        if (clientOpt.isEmpty() || !sessionService.isAuthenticated()) {
            response.setHeader("HX-Redirect", "/login?next=/post/" + userDid + "/" + postRKey + "&msg=To reply to a post, please log in.");
            return "";
        }

        AtprotoClient client = clientOpt.get();

        AtUri rootAturi = new AtUri(userDid, PostRecord.recordCollection, postRKey);

        ReplyRecord reply;
        try {
            reply = new ReplyRecord(newReplyForm.getContent(), rootAturi);
            client.createRecord(reply);
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

    /*
    @PutMapping("/post/{userDid}/{replyRKey}/htmx/reply")
    public String updateReply(
            @ModelAttribute UpdateReplyForm updateReplyForm,
            Model model,
            HttpServletResponse response,
            @PathVariable("userDid") String userDid,
            @PathVariable("replyRKey") String replyRKey
    ) {
        try {
            var clientOpt = sessionService.getCurrentClient();
            if (clientOpt.isEmpty() || !sessionService.isAuthenticated()) {
                return "not logged in";
            }

            AtprotoClient client = clientOpt.get();

            AtUri replyAtUri = new AtUri(userDid, ReplyRecord.recordCollection, replyRKey);

            // Fetch current reply from database using the DSLContext constructor
            ReplyRecord currentReply = new ReplyRecord(replyAtUri, dsl);

            if (currentReply.getContent() == null) {
                return "reply not found";
            }

            // Build updated reply data
            Json replyDataUpdated = Json.objectBuilder()
                    .put("content", updateReplyForm.getContent())
                    .put("createdAt", currentReply.getCreatedAt().toString())
                    .put("updatedAt", Instant.now().toString())
                    .put("root", currentReply.getRoot())
                    .build();

            ReplyRecord updatedReply = new ReplyRecord(replyDataUpdated);
            updatedReply.setAtUri(replyAtUri);

            client.updateRecord(updatedReply);

            return "template";
        } catch (Exception e) {
            return "error";
        }
    }
    */

    @DeleteMapping("/post/{userDid}/{replyRKey}")
    public String deleteReply(Model model,
                              HttpServletResponse response,
                              @PathVariable("userDid") String userDid,
                              @PathVariable("replyRKey") String replyRKey) {
        try{
            var clientOpt = sessionService.getCurrentClient();
            if (clientOpt.isEmpty() || !sessionService.isAuthenticated()) {
                return "not logged in";
            }

            AtprotoClient client = clientOpt.get();
            AtUri replyAtUri = new AtUri(userDid, ReplyRecord.recordCollection, replyRKey);
            ReplyRecord reply = new ReplyRecord(replyAtUri);
            client.deleteRecord(reply);

            return "template";

        } catch (Exception e) {
            return "error";
        }
    }

    /*
    @PutMapping("/post/{userDid}/{postRKey}")
    public String updatePost(@ModelAttribute UpdatePostForm updatePostForm,
                                        Model model,
                                        HttpServletResponse response,
                                        @PathVariable("userDid") String userDid,
                                        @PathVariable("postRKey") String postRKey) {
        try {
            var clientOpt = sessionService.getCurrentClient();
            if (clientOpt.isEmpty() || !sessionService.isAuthenticated()) {
                return "not logged in";
            }

            AtprotoClient client = clientOpt.get();

            AtUri postAtUri = new AtUri(userDid, PostRecord.recordCollection, postRKey);

            PostRecord currentPost = new PostRecord(postAtUri, dsl);

            Json postDataJson = Json.objectBuilder()
                    .put("title", updatePostForm.getTitle())
                    .put("content", updatePostForm.getContent())
                    .put("category", updatePostForm.getCategory()) // If this is not present in the form, get from currentPost
                    .put("createdAt", currentPost.getCreatedAt().toString())
                    .put("updatedAt", Instant.now().toString())
                    .put("tags", updatePostForm.getTags()) // If this is not present in the form, get from currentPost
                    .put("solution", updatePostForm.getTags()) // If this is not present in the form, or it didn't change, get from currentPost
                    .put("forum", currentPost.getForum())
                    .build();

            PostRecord updatedPost = new PostRecord(postDataJson);
            updatedPost.setAtUri(postAtUri);

            client.updateRecord(updatedPost);

            return "";
        } catch (Exception e) {
            return "";
        }
    }
    */

    @DeleteMapping("/post/{userDid}/{postRKey}/htmx/deletePost")
    public String deletePost(Model model,
                             HttpServletResponse response,
                             @PathVariable("userDid") String userDid,
                             @PathVariable("postRKey") String postRKey) {
        try{
            var clientOpt = sessionService.getCurrentClient();
            if (clientOpt.isEmpty() || !sessionService.isAuthenticated()) {
                response.setHeader("HX-Redirect", "/login?next=/post/" + userDid + "/" + postRKey + "&msg=To delete a post, please log in.");
            }

            AtprotoClient client = clientOpt.get();
            AtUri postAtUri = new AtUri(userDid, PostRecord.recordCollection, postRKey);
            PostRecord post = new PostRecord(postAtUri);
            client.deleteRecord(post);

            response.setHeader("HX-Redirect", "/browse");
            return "";

        } catch (Exception e) {
            response.setStatus(500);
            model.addAttribute("toastMsg", "An error occurred while trying to delete the post. Please try again later.");
            return "components/errorToast";
        }
    }

    @GetMapping("/post/{userDid}/{postRKey}/htmx/postMenu")
    public String getPostMenu(Model model,
                              HttpServletResponse response,
                              @PathVariable("userDid") String userDid,
                              @PathVariable("postRKey") String postRKey) throws IOException {
        var clientOpt = sessionService.getCurrentClient();
        if (clientOpt.isEmpty() || !sessionService.isAuthenticated()) {
            response.setHeader("HX-Redirect", "/login?next=/post/" + userDid + "/" + postRKey + "&msg=To edit a post, please log in.");
            return "";
        }
        var client = clientOpt.get();

        boolean ownsThisPost = client.isSameUser(userDid);

        model.addAttribute("ownsThisPost", ownsThisPost);
        model.addAttribute("aturi", new AtUri(userDid, PostRecord.recordCollection, postRKey));

        return "pages/post/htmx/postPopupMenu";
    }

    @GetMapping("/post/{userDid}/{postRKey}/htmx/confirmDeletePost")
    public String getConfirmDeletePost(Model model,
                                       HttpServletResponse response,
                                       @PathVariable("userDid") String userDid,
                                       @PathVariable("postRKey") String postRKey) {
        var clientOpt = sessionService.getCurrentClient();
        if (clientOpt.isEmpty() || !sessionService.isAuthenticated()) {
            response.setHeader("HX-Redirect", "/login?next=/post/" + userDid + "/" + postRKey + "&msg=To delete a post, please log in.");
            return "";
        }

        model.addAttribute("aturi", new AtUri(userDid, PostRecord.recordCollection, postRKey));

        return "pages/post/htmx/confirmDeletePostModal";
    }
}
