package com.jcs.javacommunitysite.pages;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/browse")
    public String home() {
        return "pages/browse";
    }

    @GetMapping("/hotPosts")
    public String hotPosts() {
        return "pages/hotPosts";
    }

    @GetMapping("/newPosts")
    public String newPosts() {
        return "pages/newPosts";
    }

    @GetMapping("/newReplies")
    public String newReplies() {
        return "pages/newReplies";
    }
}
