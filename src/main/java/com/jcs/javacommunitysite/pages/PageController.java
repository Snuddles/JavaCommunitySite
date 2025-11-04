package com.jcs.javacommunitysite.pages;
import com.jcs.javacommunitysite.atproto.service.AtprotoSessionService;
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
    public String getProfileMenu(Model model) {
        // Add anything you want to pass to the menu
        return "components/profileMenu";
    }
}
