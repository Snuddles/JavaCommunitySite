package com.jcs.javacommunitysite;

import com.jcs.javacommunitysite.forms.LoginForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String test(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute LoginForm loginForm, Model model) {
        // check login stuffs
        var loginValid = false;

        if (!loginValid) {
            model.addAttribute("loginForm", loginForm);
            model.addAttribute("errMsg", "Could not log you in. Your handle or password may be incorrect.");
            return "login";
        }

        return "redirect:/";
    }

}
