package com.jcs.javacommunitysite.pages.indexpage;

import org.jooq.DSLContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.jcs.javacommunitysite.atproto.service.AtprotoSessionService;

@Controller
public class IndexPageController {

    private final AtprotoSessionService sessionService;
    private final DSLContext dsl;

    public IndexPageController(AtprotoSessionService sessionService, DSLContext dsl) {
        this.sessionService = sessionService;
        this.dsl = dsl;
    }

    @GetMapping("/")
    public String index(Model model) {
        if (sessionService.isAuthenticated()) {
            model.addAttribute("loggedIn", true);
        }

        return "index";
    }
    
}
