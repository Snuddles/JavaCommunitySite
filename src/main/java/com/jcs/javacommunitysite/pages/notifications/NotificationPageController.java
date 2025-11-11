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
import org.springframework.web.bind.annotation.RequestParam;

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

        // Get user's notifications here. Attach to model.

        return "pages/notifications/notifications";
    }

    @DeleteMapping("/notifications/delete")
    public String deleteNotification(Model model, HttpServletResponse resp, @RequestParam String id) {
        if (!sessionService.isAuthenticated()) {
            return "redirect:/login?next=/notifications&msg=To access the notifications page, please log in.";
        }

        // Delete notification here

        // if (errorHappened) {
        // return ErrorUtil.createErrorToast(resp, model, "Failed to delete notification, please try again later");

         return "empty";
    }
}
