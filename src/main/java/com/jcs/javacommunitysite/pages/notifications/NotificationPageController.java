package com.jcs.javacommunitysite.pages.notifications;

import com.jcs.javacommunitysite.atproto.service.AtprotoSessionService;
import com.jcs.javacommunitysite.util.ErrorUtil;
import com.jcs.javacommunitysite.util.UserInfo;
import jakarta.servlet.http.HttpServletResponse;
import org.jooq.DSLContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.jcs.javacommunitysite.jooq.tables.Notification.NOTIFICATION;
import static com.jcs.javacommunitysite.jooq.tables.User.USER;
import static com.jcs.javacommunitysite.jooq.tables.Post.POST;

@Controller
public class NotificationPageController {

    private final AtprotoSessionService sessionService;
    private final DSLContext dsl;

    public NotificationPageController(AtprotoSessionService sessionService, DSLContext dsl) {
        this.sessionService = sessionService;
        this.dsl = dsl;
    }
    @GetMapping("/notifications")
    public String notifications(Model model) {
        if (!sessionService.isAuthenticated()) {
            return "redirect:/login?next=/notifications&msg=To access the notifications page, please log in.";
        }

        var user = UserInfo.getSelfFromDb(dsl, sessionService);
        model.addAttribute("user", user);

        var clientOpt = sessionService.getCurrentClient();
        if (clientOpt.isPresent()) {
            String userDid = clientOpt.get().getSession().getDid();
            
            List<Map<String, Object>> notifications = new ArrayList<>();
            
            var records = dsl.select()
                .from(NOTIFICATION)
                .leftJoin(USER).on(USER.DID.eq(NOTIFICATION.TRIGGERING_USER_DID))
                .leftJoin(POST).on(POST.ATURI.eq(NOTIFICATION.POST_ATURI))
                .where(NOTIFICATION.RECIPIENT_USER_DID.eq(userDid))
                .orderBy(NOTIFICATION.CREATED_AT.desc())
                .fetch();
            
            for (var record : records) {
                Map<String, Object> notification = new HashMap<>();
                
                UUID notificationId = record.get(NOTIFICATION.ID);
                notification.put("id", notificationId.toString());
                
                notification.put("type", record.get(NOTIFICATION.TYPE).toString());
                notification.put("createdAt", record.get(NOTIFICATION.CREATED_AT));
                notification.put("readAt", record.get(NOTIFICATION.READ_AT));
                notification.put("isRead", record.get(NOTIFICATION.READ_AT) != null);
                
                String triggeringUserHandle = record.get(USER.HANDLE);
                String triggeringUserDisplayName = record.get(USER.DISPLAY_NAME);
                notification.put("triggeringUserHandle", triggeringUserHandle);
                notification.put("triggeringUserDisplayName", triggeringUserDisplayName);
                
                String postTitle = record.get(POST.TITLE);
                String postAturi = record.get(NOTIFICATION.POST_ATURI);
                notification.put("postTitle", postTitle);
                notification.put("postAturi", postAturi);
                
                String notificationText = createNotificationText(record.get(NOTIFICATION.TYPE).toString(), 
                    triggeringUserDisplayName != null ? triggeringUserDisplayName : triggeringUserHandle, 
                    postTitle);
                notification.put("text", notificationText);
                
                if (postAturi != null) {
                    String[] parts = postAturi.replace("at://", "").split("/");
                    if (parts.length >= 3) {
                        String userDid2 = parts[0];
                        String rkey = parts[2];
                        notification.put("postLink", "/post/" + userDid2 + "/" + rkey);
                    }
                }
                
                notifications.add(notification);
            }
            
            model.addAttribute("notifications", notifications);
        } else {
            model.addAttribute("notifications", new ArrayList<>());
        }

        return "pages/notifications/notifications";
    }
    
    private String createNotificationText(String type, String userName, String postTitle) {
        switch (type) {
            case "NEW_COMMENT":
                return userName + " replied to your post \"" + (postTitle != null ? postTitle : "Untitled") + "\"";
            case "NEW_VOTE":
                return userName + " voted on your post \"" + (postTitle != null ? postTitle : "Untitled") + "\"";
            case "USER_MENTION":
                return userName + " mentioned you in a post";
            default:
                return "New notification from " + userName;
        }
    }

    @DeleteMapping("/notifications/delete")
    public String deleteNotification(Model model, HttpServletResponse resp, @RequestParam String id) {
        if (!sessionService.isAuthenticated()) {
            return "redirect:/login?next=/notifications&msg=To access the notifications page, please log in.";
        }

        try {
            var clientOpt = sessionService.getCurrentClient();
            if (clientOpt.isPresent()) {
                String userDid = clientOpt.get().getSession().getDid();
                UUID notificationUuid = UUID.fromString(id);
                
                int deletedRows = dsl.deleteFrom(NOTIFICATION)
                    .where(NOTIFICATION.ID.eq(notificationUuid))
                    .and(NOTIFICATION.RECIPIENT_USER_DID.eq(userDid))
                    .execute();
                
                if (deletedRows == 0) {
                    return ErrorUtil.createErrorToast(resp, model, "Notification not found or you don't have permission to delete it");
                }
            } else {
                return ErrorUtil.createErrorToast(resp, model, "Authentication required");
            }
        } catch (IllegalArgumentException e) {
            return ErrorUtil.createErrorToast(resp, model, "Invalid notification ID");
        } catch (Exception e) {
            return ErrorUtil.createErrorToast(resp, model, "Failed to delete notification, please try again later");
        }

        return "empty";
    }
    
    @PostMapping("/notifications/markread")
    public String markNotificationAsRead(Model model, HttpServletResponse resp, @RequestParam String id) {
        if (!sessionService.isAuthenticated()) {
            return "redirect:/login?next=/notifications&msg=To mark notifications as read, please log in.";
        }

        try {
            var clientOpt = sessionService.getCurrentClient();
            if (clientOpt.isPresent()) {
                String userDid = clientOpt.get().getSession().getDid();
                UUID notificationUuid = UUID.fromString(id);
                
                int updatedRows = dsl.update(NOTIFICATION)
                    .set(NOTIFICATION.READ_AT, java.time.OffsetDateTime.now())
                    .where(NOTIFICATION.ID.eq(notificationUuid))
                    .and(NOTIFICATION.RECIPIENT_USER_DID.eq(userDid))
                    .and(NOTIFICATION.READ_AT.isNull())
                    .execute();
                
                if (updatedRows == 0) {
                    return ErrorUtil.createErrorToast(resp, model, "Notification not found or already marked as read");
                }
            } else {
                return ErrorUtil.createErrorToast(resp, model, "Authentication required");
            }
        } catch (IllegalArgumentException e) {
            return ErrorUtil.createErrorToast(resp, model, "Invalid notification ID");
        } catch (Exception e) {
            return ErrorUtil.createErrorToast(resp, model, "Failed to mark notification as read, please try again later");
        }

        return "empty";
    }
}
