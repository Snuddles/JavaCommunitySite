package com.jcs.javacommunitysite.userauth;

import com.jcs.javacommunitysite.atproto.AtprotoClient;
import com.jcs.javacommunitysite.atproto.service.AtprotoSessionService;
import com.jcs.javacommunitysite.atproto.exceptions.AtprotoUnauthorized;
import com.jcs.javacommunitysite.forms.LoginForm;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@Controller
public class LoginController {
    
    private final AtprotoSessionService sessionService;

    public LoginController(AtprotoSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/login")
    public String test(Model model) {
        // Check if user is already logged in - redirect if so
        if (sessionService.isAuthenticated()) {
            return "redirect:/";
        }

        model.addAttribute("loginForm", new LoginForm());
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute LoginForm loginForm, Model model) {
        // Check if user is already logged in - log out if so
        if (sessionService.isAuthenticated()) {
            sessionService.clearSession();
        }

        try {
            String pdsHost = "https://bsky.social"; // TODO automatically determine this from handle
            String handle = loginForm.getHandle();
            String password = loginForm.getPassword();
            
            AtprotoClient client = sessionService.createSession(pdsHost, handle, password);

            // Success
            return "redirect:/";

        } catch (AtprotoUnauthorized e) {
            model.addAttribute("loginForm", loginForm);
            model.addAttribute("errMsg", "Could not log you in. Your handle or password may be incorrect.");
            return "login";
        } catch (IOException e) {
            model.addAttribute("loginForm", loginForm);
            model.addAttribute("errMsg", "Could not log you in. Please try again later.");
            return "login";
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        sessionService.clearSession();
        // TODO make this not an API endpoint, send to 'logged out' page instead
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Logged out successfully"
        ));
    }

    // TODO this probably isn't needed
//    @GetMapping("/status")
//    public ResponseEntity<?> getAuthStatus() {
//        if (sessionService.isAuthenticated()) {
//            return ResponseEntity.ok(Map.of(
//                "authenticated", true,
//                "handle", sessionService.getCurrentSession().map(s -> s.getHandle()).orElse(null)
//            ));
//        } else {
//            return ResponseEntity.ok(Map.of("authenticated", false));
//        }
//    }
}