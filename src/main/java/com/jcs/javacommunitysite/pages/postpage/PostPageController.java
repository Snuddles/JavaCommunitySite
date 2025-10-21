package com.jcs.javacommunitysite.pages.postpage;

import com.jcs.javacommunitysite.atproto.AtUri;
import com.jcs.javacommunitysite.atproto.AtprotoClient;
import com.jcs.javacommunitysite.forms.UpdatePostForm;
import com.jcs.javacommunitysite.atproto.exceptions.AtprotoUnauthorized;
import com.jcs.javacommunitysite.atproto.records.PostRecord;
import com.jcs.javacommunitysite.atproto.records.ReplyRecord;
import com.jcs.javacommunitysite.atproto.service.AtprotoSessionService;
import com.jcs.javacommunitysite.forms.NewReplyForm;
import dev.mccue.json.Json;
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
import java.time.Instant;
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
        com.jcs.javacommunitysite.jooq.tables.records.PostRecord post;
        try {
            post = dsl.selectFrom(POST)
                    .where(POST.ATURI.eq(aturi.toString()))
                    .fetchAny();

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

//        Parser parser = Parser.builder().build();
//        Node document = parser.parse((String) post.get("content"));
//
//        document.accept(new AbstractVisitor() {
//            @Override
//            public void visit(Heading heading) {
//                int newLevel = Math.min(heading.getLevel() + 2, 6);
//                heading.setLevel(newLevel);
//                visitChildren(heading);
//            }
//        });


//        HtmlRenderer renderer = HtmlRenderer.builder().build();
//        String contentHtml = renderer.render(document);

        model.addAttribute("post", post);
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
        fakeReply.setRoot(rootAturi.toString());
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

    @DeleteMapping("/post/{userDid}/{postRKey}/htmx/deleteReply")
    public String deleteReply(Model model,
                              HttpServletResponse response,
                              @PathVariable("userDid") String userDid,
                              @PathVariable("postRKey") String postRKey,
                              @RequestParam("reply") String reply) {
        try{
            var clientOpt = sessionService.getCurrentClient();
            if (clientOpt.isEmpty() || !sessionService.isAuthenticated()) {
                response.setHeader("HX-Redirect", "/login?next=/post/" + userDid + "/" + postRKey + "&msg=To delete a reply, please log in.");
            }

            AtprotoClient client = clientOpt.get();
            AtUri replyAtUri = new AtUri(reply);
            ReplyRecord replyRecord = new ReplyRecord(replyAtUri);
            client.deleteRecord(replyRecord);

            response.setHeader("HX-Reswap", "delete");
            response.setHeader("HX-Trigger", "success");
            return "empty";

        } catch (Exception e) {
            response.setStatus(500);
            model.addAttribute("toastMsg", "An error occurred while trying to delete the reply. Please try again later.");
            return "components/errorToast";
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

    @GetMapping("/post/{userDid}/{postRKey}/htmx/replyMenu")
    public String getReplyMenu(Model model,
                               HttpServletResponse response,
                               @PathVariable("userDid") String userDid,
                               @PathVariable("postRKey") String postRKey,
                               @RequestParam("reply") String reply) throws IOException {
        var clientOpt = sessionService.getCurrentClient();
        if (clientOpt.isEmpty() || !sessionService.isAuthenticated()) {
            response.setHeader("HX-Redirect", "/login?next=/post/" + userDid + "/" + postRKey + "&msg=To edit a reply, please log in.");
            return "";
        }
        var client = clientOpt.get();

        var postAtUri = new AtUri(userDid, PostRecord.recordCollection, postRKey);
        var replyAtUri = new AtUri(reply);

        boolean ownsThisReply = client.isSameUser(replyAtUri.getDid());

        model.addAttribute("ownsThisReply", ownsThisReply);
        model.addAttribute("post", postAtUri);
        model.addAttribute("reply", replyAtUri);

        return "pages/post/htmx/replyPopupMenu";
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

    @GetMapping("/post/{userDid}/{postRKey}/htmx/confirmDeleteReply")
    public String getConfirmDeleteReply(Model model,
                                        HttpServletResponse response,
                                        @PathVariable("userDid") String userDid,
                                        @PathVariable("postRKey") String postRKey,
                                        @RequestParam("reply") String reply) {
        var clientOpt = sessionService.getCurrentClient();
        if (clientOpt.isEmpty() || !sessionService.isAuthenticated()) {
            response.setHeader("HX-Redirect", "/login?next=/post/" + userDid + "/" + postRKey + "&msg=To delete a reply, please log in.");
            return "empty";
        }

        var postAtUri = new AtUri(userDid, PostRecord.recordCollection, postRKey);
        var replyAtUri = new AtUri(reply);

        model.addAttribute("post", postAtUri);
        model.addAttribute("reply", replyAtUri);

        return "pages/post/htmx/confirmDeleteReplyModal";
    }

    @GetMapping("/post/{userDid}/{postRKey}/htmx/openEditReply")
    public String getOpenEditReply(Model model,
                                   HttpServletResponse response,
                                   @PathVariable("userDid") String userDid,
                                   @PathVariable("postRKey") String postRKey,
                                   @RequestParam("reply") String reply) {
        var clientOpt = sessionService.getCurrentClient();
        if (clientOpt.isEmpty() || !sessionService.isAuthenticated()) {
            response.setHeader("HX-Redirect", "/login?next=/post/" + userDid + "/" + postRKey + "&msg=To edit a reply, please log in.");
            return "empty";
        }

        try {
            var replyRecord = dsl.selectFrom(REPLY)
                    .where(REPLY.ATURI.eq(reply))
                    .fetchAny();

            var form = new NewReplyForm();
            form.setContent(replyRecord.getContent());
            model.addAttribute("form", form);
            model.addAttribute("reply", replyRecord);

            return "pages/post/htmx/replyEditor";
        } catch (Exception e) {
            // TODO
            return "";
        }
    }

    @PutMapping("/post/{userDid}/{postRKey}/htmx/editReply")
    public String editReply(Model model,
                            HttpServletResponse response,
                            @ModelAttribute NewReplyForm newReplyForm,
                            @PathVariable("userDid") String userDid,
                            @PathVariable("postRKey") String postRKey,
                            @RequestParam("reply") String reply) throws AtprotoUnauthorized, IOException {
        var clientOpt = sessionService.getCurrentClient();
        if (clientOpt.isEmpty() || !sessionService.isAuthenticated()) {
            response.setHeader("HX-Redirect", "/login?next=/post/" + userDid + "/" + postRKey + "&msg=To edit a reply, please log in.");
            return "empty";
        }

        AtprotoClient client = clientOpt.get();

        AtUri replyAtUri = new AtUri(reply);

        // Fetch current reply from database using the DSLContext constructor
        ReplyRecord currentReply = new ReplyRecord(replyAtUri, dsl);

        if (currentReply.getContent() == null) {
            return "empty"; // TODO error
        }

        // Build updated reply data
        Json replyDataUpdated = Json.objectBuilder()
                .put("content", newReplyForm.getContent())
                .put("createdAt", currentReply.getCreatedAt().toString())
                .put("updatedAt", Instant.now().toString())
                .put("root", currentReply.getRoot())
                .build();

        ReplyRecord updatedReply = new ReplyRecord(replyDataUpdated);
        updatedReply.setAtUri(replyAtUri);

        client.updateRecord(updatedReply);

        var newReply = new com.jcs.javacommunitysite.jooq.tables.records.ReplyRecord();
        newReply.setAturi(updatedReply.getAtUri().toString());
        newReply.setRoot(updatedReply.getRoot().toString());
        newReply.setContent(updatedReply.getContent());
        newReply.setCreatedAt(updatedReply.getCreatedAt().atOffset(ZoneOffset.UTC));
        newReply.setUpdatedAt(updatedReply.getUpdatedAt().atOffset(ZoneOffset.UTC));

        model.addAttribute("reply", newReply);
        return "pages/post/components/reply";
    }

    @GetMapping("/post/{userDid}/{postRKey}/htmx/cancelEditReply")
    public String cancelEditReply(Model model,
                                  HttpServletResponse response,
                                  @ModelAttribute NewReplyForm newReplyForm,
                                  @PathVariable("userDid") String userDid,
                                  @PathVariable("postRKey") String postRKey,
                                  @RequestParam("reply") String reply) {
        try {
            var replyRecord = dsl.selectFrom(REPLY)
                    .where(REPLY.ATURI.eq(reply))
                    .fetchAny();

            model.addAttribute("reply", replyRecord);
            return "pages/post/components/reply";
        } catch (Exception e) {
            // TODO
            return "";
        }
    }

    @GetMapping("/post/{userDid}/{postRKey}/htmx/openEditPost")
    public String getOpenEditPost(Model model,
                                  HttpServletResponse response,
                                  @PathVariable("userDid") String userDid,
                                  @PathVariable("postRKey") String postRKey) {
        var clientOpt = sessionService.getCurrentClient();
        if (clientOpt.isEmpty() || !sessionService.isAuthenticated()) {
            response.setHeader("HX-Redirect", "/login?next=/post/" + userDid + "/" + postRKey + "&msg=To edit a post, please log in.");
            return "empty";
        }

        try {
            var postRecord = dsl.selectFrom(POST)
                    .where(POST.ATURI.eq(new AtUri(userDid, PostRecord.recordCollection, postRKey).toString()))
                    .fetchAny();

            var form = new UpdatePostForm();
            form.setContent(postRecord.getContent());
            model.addAttribute("form", form);
            model.addAttribute("post", postRecord);

            return "pages/post/htmx/postEditor";
        } catch (Exception e) {
            // TODO
            return "";
        }
    }

    @PutMapping("/post/{userDid}/{postRKey}/htmx/editPost")
    public String editPost(Model model,
                           HttpServletResponse response,
                           @ModelAttribute UpdatePostForm updatePostForm,
                           @PathVariable("userDid") String userDid,
                           @PathVariable("postRKey") String postRKey) throws AtprotoUnauthorized, IOException {
        var clientOpt = sessionService.getCurrentClient();
        if (clientOpt.isEmpty() || !sessionService.isAuthenticated()) {
            response.setHeader("HX-Redirect", "/login?next=/post/" + userDid + "/" + postRKey + "&msg=To edit a post, please log in.");
            return "empty";
        }

        AtprotoClient client = clientOpt.get();
        AtUri postAtUri = new AtUri(userDid, PostRecord.recordCollection, postRKey);

        PostRecord post = new PostRecord(postAtUri, dsl);

        post.setContent(updatePostForm.getContent());
        post.setUpdatedAt(Instant.now());

        client.updateRecord(post);

        var newPost = new com.jcs.javacommunitysite.jooq.tables.records.PostRecord();
        newPost.setAturi(post.getAtUri().toString());
        newPost.setTitle(post.getTitle());
        newPost.setContent(post.getContent());
        newPost.setCreatedAt(post.getCreatedAt().atOffset(ZoneOffset.UTC));
        newPost.setUpdatedAt(post.getUpdatedAt().atOffset(ZoneOffset.UTC));

        model.addAttribute("post", newPost);
        return "pages/post/components/opPost";
    }

    @GetMapping("/post/{userDid}/{postRKey}/htmx/cancelEditPost")
    public String cancelEditPost(Model model,
                                 HttpServletResponse response,
                                 @ModelAttribute UpdatePostForm updatePostForm,
                                 @PathVariable("userDid") String userDid,
                                 @PathVariable("postRKey") String postRKey) {
        try {
            var postRecord = dsl.selectFrom(POST)
                    .where(POST.ATURI.eq(new AtUri(userDid, PostRecord.recordCollection, postRKey).toString()))
                    .fetchAny();

            model.addAttribute("post", postRecord);
            return "pages/post/components/opPost";
        } catch (Exception e) {
            // TODO
            return "";
        }
    }


}
