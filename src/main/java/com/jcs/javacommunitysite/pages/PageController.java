package com.jcs.javacommunitysite.pages;
import com.jcs.javacommunitysite.atproto.AtprotoClient;
import com.jcs.javacommunitysite.atproto.service.AtprotoSessionService;
import com.jcs.javacommunitysite.util.UserInfo;
import jakarta.servlet.http.HttpServletResponse;
import org.jooq.DSLContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {
    private final AtprotoSessionService sessionService;
    private final DSLContext dsl;

    public PageController(AtprotoSessionService sessionService, DSLContext dsl) {
        this.sessionService = sessionService;
        this.dsl = dsl;
    }

    @GetMapping("/groups/{groupName}")
    public String groupPage(@PathVariable String groupName, Model model) {
        model.addAttribute("groupName", groupName);
        return "pages/groupCategories";
    }

    @GetMapping("/profileMenu")
    public String getProfileMenu(Model model, HttpServletResponse response) {
        var clientOpt = sessionService.getCurrentClient();
        if (clientOpt.isEmpty() || !sessionService.isAuthenticated()) {
            response.setHeader("HX-Redirect", "/login?next=/ask&msg=Please log in.");
            return "empty";
        }
        AtprotoClient client = clientOpt.get();

        model.addAttribute("isAdmin", UserInfo.isAdmin(dsl, client.getSession().getDid()));

        return "components/profileMenu";
    }
}
