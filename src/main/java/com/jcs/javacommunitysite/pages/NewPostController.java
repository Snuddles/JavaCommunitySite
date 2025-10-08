package com.jcs.javacommunitysite.pages;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NewPostController {
    @GetMapping("/newPost")
    public String newPost() {
        return "newpost";
    }

}
