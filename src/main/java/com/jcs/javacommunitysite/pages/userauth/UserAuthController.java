package com.jcs.javacommunitysite.pages.userauth;

import com.jcs.javacommunitysite.atproto.service.AtprotoSessionService;
import com.jcs.javacommunitysite.atproto.exceptions.AtprotoUnauthorized;
import com.jcs.javacommunitysite.atproto.AtprotoUtil;
import com.jcs.javacommunitysite.forms.LoginForm;
import dev.mccue.json.JsonObject;
import org.jooq.DSLContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.OffsetDateTime;

import static com.jcs.javacommunitysite.atproto.AtprotoUtil.getPdsHostFromHandle;
import static com.jcs.javacommunitysite.jooq.tables.User.USER;
import static dev.mccue.json.JsonDecoder.field;
import static dev.mccue.json.JsonDecoder.optionalNullableField;
import static dev.mccue.json.JsonDecoder.string;

@Controller
public class UserAuthController {

    private final AtprotoSessionService sessionService;
    private final DSLContext dsl;

    public UserAuthController(AtprotoSessionService sessionService, DSLContext dsl) {
        this.sessionService = sessionService;
        this.dsl = dsl;
    }

    @GetMapping("/login")
    public String login(Model model, @RequestParam(required = false) String msg) {
        // Check if user is already logged in - redirect if so
        if (sessionService.isAuthenticated()) {
            return "redirect:/browse";
        }

        model.addAttribute("loginForm", new LoginForm());
        if (msg != null && !msg.isBlank()) {
            model.addAttribute("customMsg", msg);
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(
            Model model,
            @ModelAttribute LoginForm loginForm,
            @RequestParam(required = false) String next,
            @RequestParam(required = false) String msg
    ) {
        // Check if user is already logged in - log out if so
        if (sessionService.isAuthenticated()) {
            sessionService.clearSession();
        }

        try {
            String handle = loginForm.getHandle();
            String password = loginForm.getPassword();

            String pdsHost = getPdsHostFromHandle(handle);

            sessionService.createSession(pdsHost, handle, password);

            JsonObject profile = AtprotoUtil.getBskyProfile(handle);
            String userDid = field(profile, "did", string());
            
            ensureUserExists(userDid, handle, profile);

            // Success
            if (next != null && !next.isBlank()) {
                return "redirect:" + next;
            } else {
                return "redirect:/ask";
            }

        } catch (AtprotoUnauthorized e) {
            System.err.println("Login failed - Unauthorized: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errMsg", "Could not log you in. Your handle or password may be incorrect.");
        } catch (IOException e) {
            System.err.println("Login failed - IO Exception: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errMsg", "Could not log you in. Please try again later.");
        }

        model.addAttribute("loginForm", loginForm);
        if (msg != null && !msg.isBlank()) {
            model.addAttribute("customMsg", msg);
        }
        return "login";
    }

    @PostMapping("/logout")
    public String logout(Model model) {
        if (sessionService.isAuthenticated()) {
            sessionService.clearSession();
        }

        model.addAttribute("loginForm", new LoginForm());
        return "login";
    }

    /**
     * Ensures a user exists in the database. If not, creates them using information from their Bluesky profile.
     */
    private void ensureUserExists(String userDid, String handle, JsonObject profile) {
        try {
            var existingUser = dsl.selectFrom(USER)
                    .where(USER.DID.eq(userDid))
                    .fetchOne();
            
            String displayName = optionalNullableField(profile, "displayName", string(), null);
            String description = optionalNullableField(profile, "description", string(), null);
            String avatarUrl = optionalNullableField(profile, "avatar", string(), "");
            
            if (existingUser == null) {
                dsl.insertInto(USER)
                    .set(USER.DID, userDid)
                    .set(USER.HANDLE, handle)
                    .set(USER.DISPLAY_NAME, displayName)
                    .set(USER.DESCRIPTION, description)
                    .set(USER.AVATAR_BLOBURL, avatarUrl)
                    .set(USER.CREATED_AT, OffsetDateTime.now())
                    .execute();
                
                System.out.println("Created new user: " + handle + " (" + userDid + ")");
            } else {
                // User exists, check if any fields need updating
                boolean needsUpdate = false;
                
                if (!handle.equals(existingUser.getHandle())) {
                    needsUpdate = true;
                    System.out.println("Handle changed: " + existingUser.getHandle() + " -> " + handle);
                }
                
                if (!java.util.Objects.equals(displayName, existingUser.getDisplayName())) {
                    needsUpdate = true;
                    System.out.println("Display name changed: " + existingUser.getDisplayName() + " -> " + displayName);
                }
                
                if (!java.util.Objects.equals(description, existingUser.getDescription())) {
                    needsUpdate = true;
                    System.out.println("Description changed: " + existingUser.getDescription() + " -> " + description);
                }
                
                if (!java.util.Objects.equals(avatarUrl, existingUser.getAvatarBloburl())) {
                    needsUpdate = true;
                    System.out.println("Avatar URL changed: " + existingUser.getAvatarBloburl() + " -> " + avatarUrl);
                }
                
                if (needsUpdate) {
                    System.out.println("User data changed, updating: " + handle + " (" + userDid + ")");
                    dsl.update(USER)
                        .set(USER.HANDLE, handle)
                        .set(USER.DISPLAY_NAME, displayName)
                        .set(USER.DESCRIPTION, description)
                        .set(USER.AVATAR_BLOBURL, avatarUrl)
                        .set(USER.UPDATED_AT, OffsetDateTime.now())
                        .where(USER.DID.eq(userDid))
                        .execute();
                    System.out.println("Updated existing user: " + handle + " (" + userDid + ")");
                } else {
                    System.out.println("User data unchanged, no update needed: " + handle + " (" + userDid + ")");
                }
            }
        } catch (Exception e) {
            System.err.println("Error ensuring user exists for handle: " + handle + ", DID: " + userDid);
            System.err.println("Error details: " + e.getMessage());
            e.printStackTrace();
            // Don't throw the exception - login should still succeed even if user creation fails
        }
    }
}