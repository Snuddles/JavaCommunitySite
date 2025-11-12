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
 import com.jcs.javacommunitysite.util.ErrorUtil;
 import com.jcs.javacommunitysite.util.ModUtil;
 import com.jcs.javacommunitysite.util.UserInfo;
 import dev.mccue.json.Json;
 import jakarta.servlet.http.HttpServletResponse;
 import org.jooq.DSLContext;
 import org.jooq.JSON;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.*;

 import java.io.IOException;
 import java.time.Instant;
 import java.time.ZoneOffset;
 import java.util.HashSet;
 import java.util.Set;

 import static com.jcs.javacommunitysite.jooq.tables.HiddenPost.HIDDEN_POST;
 import static com.jcs.javacommunitysite.jooq.tables.Post.POST;
 import static com.jcs.javacommunitysite.jooq.tables.Reply.REPLY;
 import static com.jcs.javacommunitysite.jooq.tables.HiddenReply.HIDDEN_REPLY;
 import static com.jcs.javacommunitysite.jooq.tables.Tags.TAGS;
 import static com.jcs.javacommunitysite.jooq.tables.Notification.NOTIFICATION;
 import com.jcs.javacommunitysite.jooq.enums.NotificationType;

 @Controller
 public class PostPageController {

     private final DSLContext dsl;
     private final AtprotoSessionService sessionService;

     public PostPageController(DSLContext dsl, AtprotoSessionService sessionService) {
         this.dsl = dsl;
         this.sessionService = sessionService;
     }

     @GetMapping("/post/{userDid}/{postRKey}")
     public String getPost(
             Model model,
             HttpServletResponse response,
             @PathVariable("userDid") String userDid,
             @PathVariable("postRKey") String postRKey
     ) {
         AtUri aturi = new AtUri(userDid, QuestionRecord.recordCollection, postRKey);

         // Get post
         com.jcs.javacommunitysite.jooq.tables.records.PostRecord post;
         try {
             post = dsl.selectFrom(POST)
                     .where(POST.ATURI.eq(aturi.toString()))
                     .fetchAny();

             if (post == null) {
                 return ""; // TODO redirect 404
             }
         } catch (Exception e) {
             return ""; // TODO redirect to 500
         }

         // Get replies
         com.jcs.javacommunitysite.jooq.tables.records.ReplyRecord[] postReplies;
         try {
             postReplies = dsl.selectFrom(REPLY)
                     .where(REPLY.ROOT_POST_ATURI.eq(aturi.toString()))
                     .orderBy(REPLY.CREATED_AT.asc())
                     .limit(20)
                     .fetchArray();
         } catch (Exception e) {
             return ErrorUtil.createErrorToast(response, model, "Failed to get replies. Please try again later.");
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
         model.addAttribute("isPostHidden", ModUtil.isPostHidden(dsl, aturi));

         return "pages/post/page";
     }

     @GetMapping("/post/{userDid}/{postRKey}/replies")
     public String getReplies(Model model,
                              HttpServletResponse response,
                              @PathVariable("userDid") String userDid,
                              @PathVariable("postRKey") String postRKey,
                              @RequestParam int page) {
         AtUri aturi = new AtUri(userDid, QuestionRecord.recordCollection, postRKey);

         try {
             var replies = dsl.selectFrom(REPLY)
                     .where(REPLY.ROOT_POST_ATURI.eq(aturi.toString()))
                     .orderBy(REPLY.CREATED_AT.asc())
                     .limit(20)
                     .offset((page - 1) * 20)
                     .fetchArray();

             var totalReplies = dsl.selectCount()
                     .from(REPLY)
                     .where(REPLY.ROOT_POST_ATURI.eq(aturi.toString()))
                     .fetchOne(0, int.class);

             boolean hasMoreReplies = totalReplies > ((page + 1) * 20);

             Set<String> userDids = new HashSet<>();
             for (var reply : replies) {
                 userDids.add(reply.getOwnerDid());
             }
             var usersMap = UserInfo.getFromDb(dsl, sessionService, userDids);

             model.addAttribute("replies", replies);
             model.addAttribute("users", usersMap);
             model.addAttribute("nextPage", page + 1);
             model.addAttribute("hasMoreReplies", hasMoreReplies);

             return "pages/post/htmx/replies";
         } catch (Exception e) {
             return ErrorUtil.createErrorToast(response, model, "Failed to load more replies. Please try again later.");
         }
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
             return "empty";
         }

         AtprotoClient client = clientOpt.get();

         AtUri rootAturi = new AtUri(userDid, QuestionRecord.recordCollection, postRKey);

         ReplyRecord reply;
         try {
             reply = new ReplyRecord(newReplyForm.getContent(), rootAturi);
             client.createRecord(reply);
             
             String postOwnerDid = userDid;
             String replyingUserDid = client.getSession().getDid();
             
             if (!postOwnerDid.equals(replyingUserDid)) {
                 dsl.insertInto(NOTIFICATION)
                     .set(NOTIFICATION.RECIPIENT_USER_DID, postOwnerDid)
                     .set(NOTIFICATION.TRIGGERING_USER_DID, replyingUserDid)
                     .set(NOTIFICATION.POST_ATURI, rootAturi.toString())
                     .set(NOTIFICATION.REPLY_ATURI, reply.getAtUri().toString())
                     .set(NOTIFICATION.TYPE, NotificationType.NEW_COMMENT)
                     .execute();
             }
         } catch (Exception e) {
             return ErrorUtil.createErrorToast(response, model, "An error occurred while trying to reply to the question. Please try again later.");
         }

         
         var fakeReply = new com.jcs.javacommunitysite.jooq.tables.records.ReplyRecord();
         fakeReply.setRootPostAturi(rootAturi.toString());
         fakeReply.setContent(reply.getContent());
         fakeReply.setCreatedAt(reply.getCreatedAt().atOffset(ZoneOffset.UTC));

         model.addAttribute("user", UserInfo.getFromDb(dsl, sessionService, client.getSession().getDid()));
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
             return ErrorUtil.createErrorToast(response, model, "An error occurred while trying to delete the reply. Please try again later.");
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
             return "empty";

         } catch (Exception e) {
             return ErrorUtil.createErrorToast(response, model, "Failed to delete the post. Please try again later.");
         }
     }

     // TODO convert into non htmx modal? same w reply
     @GetMapping("/post/{userDid}/{postRKey}/htmx/confirmDeletePost")
     public String getConfirmDeletePost(Model model,
                                        HttpServletResponse response,
                                        @PathVariable("userDid") String userDid,
                                        @PathVariable("postRKey") String postRKey) {
         var clientOpt = sessionService.getCurrentClient();
         if (clientOpt.isEmpty() || !sessionService.isAuthenticated()) {
             response.setHeader("HX-Redirect", "/login?next=/post/" + userDid + "/" + postRKey + "&msg=To delete a post, please log in.");
             return "empty";
         }

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
             model.addAttribute("user", UserInfo.getFromDb(dsl, sessionService, client.getSession().getDid()));

             return "pages/post/htmx/replyEditor";
         } catch (Exception e) {
             return ErrorUtil.createErrorToast(response, model, "Failed to open reply editor. Please try again later.");
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

         // Build updated reply data
         Json replyDataUpdated = Json.objectBuilder()
                 .put("content", newReplyForm.getContent())
                 .put("createdAt", currentReply.getCreatedAt().toString())
                 .put("updatedAt", Instant.now().toString())
                 .put("root", currentReply.getRoot())
                 .build();

         ReplyRecord updatedReply = new ReplyRecord(replyDataUpdated);
         updatedReply.setAtUri(replyAtUri);

         try {
             client.updateRecord(updatedReply);
         } catch (Exception e) {
             return ErrorUtil.createErrorToast(response, model, "Failed to edit reply. Please try again later.");
         }

         var newReply = new com.jcs.javacommunitysite.jooq.tables.records.ReplyRecord();
         newReply.setAturi(updatedReply.getAtUri().toString());
         newReply.setRootPostAturi(updatedReply.getRoot().toString());
         newReply.setContent(updatedReply.getContent());
         newReply.setCreatedAt(updatedReply.getCreatedAt().atOffset(ZoneOffset.UTC));
         newReply.setUpdatedAt(updatedReply.getUpdatedAt().atOffset(ZoneOffset.UTC));

         model.addAttribute("reply", newReply);
         model.addAttribute("user", UserInfo.getFromDb(dsl, sessionService, updatedReply.getAtUri().getDid()));
         model.addAttribute("loggedInUser", UserInfo.getSelfFromDb(dsl, sessionService));
         model.addAttribute("isHidden", ModUtil.isReplyHidden(dsl, currentReply.getAtUri())); // TODO

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
             model.addAttribute("user", UserInfo.getFromDb(dsl, sessionService, new AtUri(reply).getDid()));
             model.addAttribute("isHidden", ModUtil.isReplyHidden(dsl, new AtUri(reply))); // TODO
             model.addAttribute("loggedInUser", UserInfo.getSelfFromDb(dsl, sessionService));
             return "pages/post/components/reply";
         } catch (Exception e) {
             return ErrorUtil.createErrorToast(response, model, "Failed to retrieve reply. Please try again later.");
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

             var allTags = dsl.selectDistinct(TAGS.TAG_NAME)
                     .from(TAGS)
                     .fetchInto(String.class);

             var form = new UpdatePostForm();
             form.setContent(postRecord.getContent());
             model.addAttribute("form", form);
             model.addAttribute("allTags", allTags);
             model.addAttribute("post", postRecord);
             model.addAttribute("user", UserInfo.getFromDb(dsl, sessionService, client.getSession().getDid()));

             return "pages/post/htmx/postEditor";
         } catch (Exception e) {
             return ErrorUtil.createErrorToast(response, model, "Failed to open post editor. Please try again later.");
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
         post.setTags(updatePostForm.getTags());

         try {
             client.updateRecord(post);
         } catch (Exception e) {
             return ErrorUtil.createErrorToast(response, model, "Failed to edit post. Please try again later.");
         }

         var newPost = new com.jcs.javacommunitysite.jooq.tables.records.PostRecord();
         newPost.setAturi(post.getAtUri().toString());
         newPost.setTitle(post.getTitle());
         newPost.setContent(post.getContent());
         newPost.setCreatedAt(post.getCreatedAt().atOffset(ZoneOffset.UTC));
         newPost.setUpdatedAt(post.getUpdatedAt().atOffset(ZoneOffset.UTC));
         newPost.setTags(JSON.valueOf(Json.of(post.getTags(), Json::of).toString()));

         model.addAttribute("post", newPost);
         model.addAttribute("user", UserInfo.getFromDb(dsl, sessionService, post.getAtUri().getDid()));
         model.addAttribute("isHidden", ModUtil.isPostHidden(dsl, new AtUri(userDid, QuestionRecord.recordCollection, postRKey))); // TODO
         model.addAttribute("selfHighlight", true);
         model.addAttribute("loggedInUser", UserInfo.getSelfFromDb(dsl, sessionService));

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
             model.addAttribute("user", UserInfo.getFromDb(dsl, sessionService, userDid));
             model.addAttribute("isHidden", ModUtil.isPostHidden(dsl, new AtUri(userDid, QuestionRecord.recordCollection, postRKey))); // TODO
             model.addAttribute("selfHighlight", true);
             model.addAttribute("loggedInUser", UserInfo.getSelfFromDb(dsl, sessionService));
             return "pages/post/components/opPost";
         } catch (Exception e) {
             return ErrorUtil.createErrorToast(response, model, "Failed to retrieve post. Please try again later.");
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

         // TODO Check if they are an admin

         try {
             var hidePostRecord = new HidePostRecord(new AtUri(userDid, QuestionRecord.recordCollection, postRKey), (Instant) null);
             client.createRecord(hidePostRecord);

             model.addAttribute("post", new AtUri(userDid, QuestionRecord.recordCollection, postRKey));
             model.addAttribute("isHidden", true);
             return "pages/post/htmx/hideButton";
         } catch (Exception e) {
             return ErrorUtil.createErrorToast(response, model, "Failed to hide post. Please try again later.");
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

         // TODO Check if they are an admin

         try {
             var hideReplyRecord = new HideReplyRecord(new AtUri(reply), (Instant) null);
             client.createRecord(hideReplyRecord);

             model.addAttribute("post", new AtUri(userDid, QuestionRecord.recordCollection, postRKey));
             model.addAttribute("reply", new AtUri(reply));
             model.addAttribute("isHidden", true);
             return "pages/post/htmx/hideButton";
         } catch (Exception e) {
             return ErrorUtil.createErrorToast(response, model, "Failed to hide reply. Please try again later.");
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

         // TODO Check if they are an admin

         var postAtUri = new AtUri(userDid, QuestionRecord.recordCollection, postRKey);
         try {
             var hiddePostAturi = dsl.select(HIDDEN_POST.ATURI)
                     .from(HIDDEN_POST)
                     .where(HIDDEN_POST.POST_ATURI.eq(postAtUri.toString()))
                     .fetchOneInto(String.class);
             client.deleteRecord(new AtUri(hiddePostAturi));

             model.addAttribute("post", new AtUri(userDid, QuestionRecord.recordCollection, postRKey));
             model.addAttribute("isHidden", false);
             return "pages/post/htmx/hideButton";
         } catch (Exception e) {
             return ErrorUtil.createErrorToast(response, model, "Failed to unhide post. Please try again later.");
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

         // TODO Check if they are an admin

         try {
             var hiddenReplyAturi = dsl.select(HIDDEN_REPLY.ATURI)
                     .from(HIDDEN_REPLY)
                     .where(HIDDEN_REPLY.REPLY_ATURI.eq(reply))
                     .fetchOneInto(String.class);
             client.deleteRecord(new AtUri(hiddenReplyAturi));

             model.addAttribute("post", new AtUri(userDid, QuestionRecord.recordCollection, postRKey));
             model.addAttribute("reply", new AtUri(reply));
             model.addAttribute("isHidden", false);
             return "pages/post/htmx/hideButton";
         } catch (Exception e) {
             return ErrorUtil.createErrorToast(response, model, "Failed to unhide reply. Please try again later.");
         }
     }

     @GetMapping("/post/{userDid}/{postRKey}/htmx/confirmClosePost")
     public String getConfirmClosePost(Model model,
                                       HttpServletResponse response,
                                       @PathVariable("userDid") String userDid,
                                       @PathVariable("postRKey") String postRKey) {
         var clientOpt = sessionService.getCurrentClient();
         if (clientOpt.isEmpty() || !sessionService.isAuthenticated()) {
             response.setHeader("HX-Redirect", "/login?next=/post/" + userDid + "/" + postRKey + "&msg=To close a post, please log in.");
             return "empty";
         }

         model.addAttribute("aturi", new AtUri(userDid, QuestionRecord.recordCollection, postRKey));
         return "pages/post/htmx/confirmClosePostModal";
     }

     @PostMapping("/post/{userDid}/{postRKey}/htmx/closePost")
     public String closePost(Model model,
                             HttpServletResponse response,
                             @PathVariable("userDid") String userDid,
                             @PathVariable("postRKey") String postRKey) throws AtprotoUnauthorized, IOException {
         var clientOpt = sessionService.getCurrentClient();
         if (clientOpt.isEmpty() || !sessionService.isAuthenticated()) {
             response.setHeader("HX-Redirect", "/login?next=/post/" + userDid + "/" + postRKey + "&msg=To close a post, please log in.");
             return "empty";
         }

         AtprotoClient client = clientOpt.get();
         AtUri postAtUri = new AtUri(userDid, QuestionRecord.recordCollection, postRKey);

         QuestionRecord post = new QuestionRecord(postAtUri, dsl);
         post.setOpen(false);
         post.setUpdatedAt(Instant.now());

         try {
             client.updateRecord(post);
         } catch (Exception e) {
             return ErrorUtil.createErrorToast(response, model, "Failed to close post. Please try again later.");
         }

         // replace reply box with closed post notif
         return "pages/post/components/closedPostNotif";
     }


 }
