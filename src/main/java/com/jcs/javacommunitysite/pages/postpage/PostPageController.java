 package com.jcs.javacommunitysite.pages.postpage;

 import com.jcs.javacommunitysite.atproto.AtUri;
 import com.jcs.javacommunitysite.atproto.AtprotoClient;
 import com.jcs.javacommunitysite.atproto.records.HidePostRecord;
 import com.jcs.javacommunitysite.atproto.records.HideReplyRecord;
 import com.jcs.javacommunitysite.atproto.records.QuestionRecord;
 import com.jcs.javacommunitysite.forms.UpdatePostForm;
 import com.jcs.javacommunitysite.atproto.exceptions.AtprotoUnauthorized;
 import com.jcs.javacommunitysite.atproto.records.ReplyRecord;
 import com.jcs.javacommunitysite.atproto.service.AtprotoSessionService;
 import com.jcs.javacommunitysite.forms.NewReplyForm;
 import com.jcs.javacommunitysite.util.UserInfo;
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
 import java.time.ZoneOffset;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;

 import static com.jcs.javacommunitysite.jooq.tables.Post.POST;
 import static com.jcs.javacommunitysite.jooq.tables.Reply.REPLY;
 import static com.jcs.javacommunitysite.jooq.tables.User.USER;

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
         AtUri aturi = new AtUri(userDid, QuestionRecord.recordCollection, postRKey);

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
                     .where(REPLY.ROOT_POST_ATURI.eq(aturi.toString()))
                     .orderBy(REPLY.CREATED_AT.asc())
                     .fetchArray();
         } catch (Exception e) {
             // TODO
             return "";
         }

         // Get all users associated with this post
         Set<String> associatedUserDids = new HashSet<>();
         associatedUserDids.add(post.getOwnerDid());
         for (var reply : postReplies) {
             associatedUserDids.add(reply.getOwnerDid());
         }
         var usersMap = UserInfo.getFromDb(dsl, sessionService, associatedUserDids);

         model.addAttribute("users", usersMap);
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
             response.setHeader("HX-Redirect", "/login?next=/post/" + userDid + "/" + postRKey + "&msg=To reply to a question, please log in.");
             return "";
         }

         AtprotoClient client = clientOpt.get();

         AtUri rootAturi = new AtUri(userDid, QuestionRecord.recordCollection, postRKey);

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
         fakeReply.setRootPostAturi(rootAturi.toString());
         fakeReply.setContent(reply.getContent());
         fakeReply.setCreatedAt(reply.getCreatedAt().atOffset(ZoneOffset.UTC));

         model.addAttribute("user", UserInfo.getFromDb(dsl, client.getSession().getDid()));
         model.addAttribute("reply", fakeReply);

         return "pages/post/htmx/reply";
     }

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
             AtUri postAtUri = new AtUri(userDid, QuestionRecord.recordCollection, postRKey);
             QuestionRecord post = new QuestionRecord(postAtUri);
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
//         boolean isAdmin =
//         boolean isHidden =

//         model.addAttribute("isAdmin", isAdmin);
//         model.addAttribute("isHidden", isHidden);
         model.addAttribute("ownsThisPost", ownsThisPost);
         model.addAttribute("aturi", new AtUri(userDid, QuestionRecord.recordCollection, postRKey));

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

         var postAtUri = new AtUri(userDid, QuestionRecord.recordCollection, postRKey);
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

//         boolean isAdmin =
//         boolean isHidden =

//         model.addAttribute("isAdmin", isAdmin);
//         model.addAttribute("isHidden", isHidden);
         model.addAttribute("aturi", new AtUri(userDid, QuestionRecord.recordCollection, postRKey));

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

         var postAtUri = new AtUri(userDid, QuestionRecord.recordCollection, postRKey);
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
         var client = clientOpt.get();

         try {
             var replyRecord = dsl.selectFrom(REPLY)
                     .where(REPLY.ATURI.eq(reply))
                     .fetchAny();

             var form = new NewReplyForm();
             form.setContent(replyRecord.getContent());
             model.addAttribute("form", form);
             model.addAttribute("reply", replyRecord);
             model.addAttribute("user", UserInfo.getFromDb(dsl, client.getSession().getDid()));

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
         newReply.setRootPostAturi(updatedReply.getRoot().toString());
         newReply.setContent(updatedReply.getContent());
         newReply.setCreatedAt(updatedReply.getCreatedAt().atOffset(ZoneOffset.UTC));
         newReply.setUpdatedAt(updatedReply.getUpdatedAt().atOffset(ZoneOffset.UTC));

         model.addAttribute("reply", newReply);
         model.addAttribute("user", UserInfo.getFromDb(dsl, updatedReply.getAtUri().getDid()));

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
             model.addAttribute("user", UserInfo.getFromDb(dsl, new AtUri(reply).getDid()));
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
         var client = clientOpt.get();

         try {
             var postRecord = dsl.selectFrom(POST)
                     .where(POST.ATURI.eq(new AtUri(userDid, QuestionRecord.recordCollection, postRKey).toString()))
                     .fetchAny();

             var form = new UpdatePostForm();
             form.setContent(postRecord.getContent());
             model.addAttribute("form", form);
             model.addAttribute("post", postRecord);
             model.addAttribute("user", UserInfo.getFromDb(dsl, client.getSession().getDid()));

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
         AtUri postAtUri = new AtUri(userDid, QuestionRecord.recordCollection, postRKey);

         QuestionRecord post = new QuestionRecord(postAtUri, dsl);

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
         model.addAttribute("user", UserInfo.getFromDb(dsl, post.getAtUri().getDid()));

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
                     .where(POST.ATURI.eq(new AtUri(userDid, QuestionRecord.recordCollection, postRKey).toString()))
                     .fetchAny();

             model.addAttribute("post", postRecord);
             model.addAttribute("user", UserInfo.getFromDb(dsl, userDid));
             return "pages/post/components/opPost";
         } catch (Exception e) {
             // TODO
             return "";
         }
     }

     @PostMapping("/post/{userDid}/{postRKey}/htmx/hidePost")
     public String hidePost(Model model,
                            HttpServletResponse response,
                            @PathVariable("userDid") String userDid,
                            @PathVariable("postRKey") String postRKey)
     {
         var clientOpt = sessionService.getCurrentClient();
         if (clientOpt.isEmpty() || !sessionService.isAuthenticated()) {
             response.setHeader("HX-Redirect", "/login?next=/post/" + userDid + "/" + postRKey + "&msg=To hide a post, please log in.");
             return "empty";
         }
         AtprotoClient client = clientOpt.get();

         // Check if they are an admin

         try {
             var hidePostRecord = new HidePostRecord(new AtUri(userDid, QuestionRecord.recordCollection, postRKey), (Instant) null);
             client.createRecord(hidePostRecord);
             return "empty";
         } catch (Exception e) {
             response.setStatus(500);
             model.addAttribute("toastMsg", "An error occurred while trying to hide the post. Please try again later.");
             return "components/errorToast";
         }
     }

     @PostMapping("/post/{userDid}/{postRKey}/htmx/hideReply")
     public String hideReply(Model model,
                            HttpServletResponse response,
                            @PathVariable("userDid") String userDid,
                            @PathVariable("postRKey") String postRKey,
                            @RequestParam("reply") String reply)
     {
         var clientOpt = sessionService.getCurrentClient();
         if (clientOpt.isEmpty() || !sessionService.isAuthenticated()) {
             response.setHeader("HX-Redirect", "/login?next=/post/" + userDid + "/" + postRKey + "&msg=To hide a reply, please log in.");
             return "empty";
         }
         AtprotoClient client = clientOpt.get();

         // Check if they are an admin

         try {
             var hideReplyRecord = new HideReplyRecord(new AtUri(userDid, ReplyRecord.recordCollection, postRKey), (Instant) null);
             client.createRecord(hideReplyRecord);
             return "empty";
         } catch (Exception e) {
             response.setStatus(500);
             model.addAttribute("toastMsg", "An error occurred while trying to hide the reply. Please try again later.");
             return "components/errorToast";
         }
     }

     @PostMapping("/post/{userDid}/{postRKey}/htmx/unhidePost")
     public String unhidePost(Model model,
                            HttpServletResponse response,
                            @PathVariable("userDid") String userDid,
                            @PathVariable("postRKey") String postRKey)
     {
         var clientOpt = sessionService.getCurrentClient();
         if (clientOpt.isEmpty() || !sessionService.isAuthenticated()) {
             response.setHeader("HX-Redirect", "/login?next=/post/" + userDid + "/" + postRKey + "&msg=To unhide a post, please log in.");
             return "empty";
         }
         AtprotoClient client = clientOpt.get();

         // Check if they are an admin

         try {
             client.deleteRecord(new AtUri(userDid, QuestionRecord.recordCollection, postRKey));
             return "empty";
         } catch (Exception e) {
             response.setStatus(500);
             model.addAttribute("toastMsg", "An error occurred while trying to unhide the post. Please try again later.");
             return "components/errorToast";
         }
     }

     @PostMapping("/post/{userDid}/{postRKey}/htmx/unhideReply")
     public String unhideReply(Model model,
                               HttpServletResponse response,
                               @PathVariable("userDid") String userDid,
                               @PathVariable("postRKey") String postRKey,
                               @RequestParam("reply") String reply) {
         var clientOpt = sessionService.getCurrentClient();
         if (clientOpt.isEmpty() || !sessionService.isAuthenticated()) {
             response.setHeader("HX-Redirect", "/login?next=/post/" + userDid + "/" + postRKey + "&msg=To unhide a reply, please log in.");
             return "empty";
         }
         AtprotoClient client = clientOpt.get();

         // Check if they are an admin

         try {
             client.deleteRecord(new AtUri(reply));
             return "empty";
         } catch (Exception e) {
             response.setStatus(500);
             model.addAttribute("toastMsg", "An error occurred while trying to unhide the reply. Please try again later.");
             return "components/errorToast";
         }
     }


 }
