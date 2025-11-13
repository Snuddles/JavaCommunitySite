package com.jcs.javacommunitysite.pages.admin;

import com.jcs.javacommunitysite.atproto.AtUri;
import com.jcs.javacommunitysite.atproto.AtprotoClient;
import com.jcs.javacommunitysite.atproto.AtprotoUtil;
import com.jcs.javacommunitysite.atproto.records.TagRecord;
import com.jcs.javacommunitysite.atproto.service.AtprotoSessionService;
import com.jcs.javacommunitysite.util.UserInfo;
import jakarta.servlet.http.HttpServletResponse;
import org.jooq.Record3;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.jooq.DSLContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.jcs.javacommunitysite.jooq.tables.Role.ROLE;
import static com.jcs.javacommunitysite.jooq.tables.Tags.TAGS;
import static com.jcs.javacommunitysite.jooq.tables.User.USER;
import static com.jcs.javacommunitysite.jooq.tables.UserRole.USER_ROLE;
import static dev.mccue.json.JsonDecoder.field;
import static dev.mccue.json.JsonDecoder.string;


@Controller
public class AdminPageController {
    private final DSLContext dsl;
    private final AtprotoSessionService sessionService;

    public AdminPageController(DSLContext dsl, AtprotoSessionService sessionService) {
        this.dsl = dsl;
        this.sessionService = sessionService;
    }

    @GetMapping("/admin")
    public String admin(
            Model model
    ) {
        var clientOpt = sessionService.getCurrentClient();
        if (clientOpt.isEmpty() || !sessionService.isAuthenticated()) {
            return "redirect:/login?next=/admin&msg=To access the admin panel, please log in.";
        }
        AtprotoClient client = clientOpt.get();
        // TODO check admin

        Map<String, String> myTags = dsl
                .select(TAGS.ATURI, TAGS.TAG_NAME)
                .from(TAGS)
                .where(TAGS.CREATED_BY.eq(client.getSession().getDid()))
                .fetchMap(TAGS.ATURI, TAGS.TAG_NAME);

        Map<String, String> othersTags = dsl
                .select(TAGS.ATURI, TAGS.TAG_NAME)
                .from(TAGS)
                .where(TAGS.CREATED_BY.ne(client.getSession().getDid()))
                .fetchMap(TAGS.ATURI, TAGS.TAG_NAME);

        List<AdminUser> admins = dsl
                .select(USER.DID, USER.HANDLE, USER.DISPLAY_NAME, ROLE.NAME)
                .from(USER)
                .join(USER_ROLE).on(USER_ROLE.USER_DID.eq(USER.DID))
                .join(ROLE).on(USER_ROLE.ROLE_ID.eq(ROLE.ID))
                .where(ROLE.NAME.in("admin", "superadmin"))
                .orderBy(ROLE.NAME.desc(), USER.HANDLE.asc())   // superadmin first, then admin
                .fetch(record -> new AdminUser(
                        record.get(USER.DID),
                        record.get(USER.HANDLE),
                        record.get(USER.DISPLAY_NAME),
                        record.get(ROLE.NAME)
                ));

        model.addAttribute("admins", admins);
        model.addAttribute("myTags", myTags);
        model.addAttribute("othersTags", othersTags);
        model.addAttribute("user", UserInfo.getSelfFromDb(dsl, sessionService));
        return "pages/admin/admin";
    }

    @PostMapping("/admin/htmx/createTag")
    public String createTag(
            NewTagForm newTagForm,
            HttpServletResponse response,
            Model model
    ) {
        var clientOpt = sessionService.getCurrentClient();
        if (clientOpt.isEmpty() || !sessionService.isAuthenticated()) {
            response.setHeader("HX-Redirect", "/login?next=/admin&msg=To access the admin panel, please log in.");
            return "empty";
        }
        AtprotoClient client = clientOpt.get();
        // TODO check admin

        var newTag = new TagRecord(newTagForm.getName());
        try {
            var result = client.createRecord(newTag);
            model.addAttribute("atUri", new AtUri(field(result, "uri", string())));
            model.addAttribute("name", newTagForm.getName());
            model.addAttribute("isOwnTag", true);
            return "pages/admin/components/tag";
        } catch (Exception e) {
            // TODO error handling
            return "empty";
        }
    }

    @PostMapping("/admin/htmx/deleteTag")
    public String deleteTag(
            HttpServletResponse response,
            @RequestParam("tag") String tagAtUri
    ) {
        var clientOpt = sessionService.getCurrentClient();
        if (clientOpt.isEmpty() || !sessionService.isAuthenticated()) {
            response.setHeader("HX-Redirect", "/login?next=/admin&msg=To access the admin panel, please log in.");
            return "empty";
        }
        AtprotoClient client = clientOpt.get();
        // TODO check admin

        try {
            client.deleteRecord(new AtUri(tagAtUri));
            return "empty";
        } catch (Exception e) {
            // TODO error handling
            return "empty";
        }
    }


    @GetMapping("/admin/user-search")
    public String searchUser(@RequestParam String handle, Model model) {
        var user = dsl.selectFrom(USER)
                .where(USER.HANDLE.eq(handle))
                .fetchOne();

        boolean alreadyAdmin = false;
        boolean targetIsSuperadmin = false;

        if (user != null) {
            Integer adminRoleId = dsl.select(ROLE.ID).from(ROLE)
                    .where(ROLE.NAME.eq("admin")).fetchOne(ROLE.ID);
            Integer superadminRoleId = dsl.select(ROLE.ID).from(ROLE)
                    .where(ROLE.NAME.eq("superadmin")).fetchOne(ROLE.ID);

            alreadyAdmin = dsl.fetchExists(
                    dsl.selectOne().from(USER_ROLE)
                            .where(USER_ROLE.USER_DID.eq(user.getDid()))
                            .and(USER_ROLE.ROLE_ID.eq(adminRoleId))
            );

            targetIsSuperadmin = dsl.fetchExists(
                    dsl.selectOne().from(USER_ROLE)
                            .where(USER_ROLE.USER_DID.eq(user.getDid()))
                            .and(USER_ROLE.ROLE_ID.eq(superadminRoleId))
            );
        }

        model.addAttribute("searchedUser", user);
        model.addAttribute("alreadyAdmin", alreadyAdmin);
        model.addAttribute("isSuperAdmin", targetIsSuperadmin);
        return "pages/admin/fragments/user_card";
    }


    @PostMapping("/admin/grant")
    public String grantAdmin(@RequestParam String targetDid, Model model) {
        var client = sessionService.getCurrentClient().orElseThrow();

        boolean isAdmin = dsl.fetchExists(
                dsl.selectOne().from(USER_ROLE)
                        .join(ROLE).on(USER_ROLE.ROLE_ID.eq(ROLE.ID))
                        .where(USER_ROLE.USER_DID.eq(client.getSession().getDid()))
                        .and(ROLE.NAME.eq("superadmin"))
        );
        if (!isAdmin) {
            model.addAttribute("message", "You are not authorized to grant roles.");
            return "pages/admin/fragments/message";
        }

        Integer adminRoleId = dsl.select(ROLE.ID).from(ROLE)
                .where(ROLE.NAME.eq("admin")).fetchOne(ROLE.ID);

        boolean alreadyAdmin = dsl.fetchExists(
                dsl.selectOne().from(USER_ROLE)
                        .where(USER_ROLE.USER_DID.eq(targetDid))
                        .and(USER_ROLE.ROLE_ID.eq(adminRoleId))
        );

        if (!alreadyAdmin) {
            dsl.insertInto(USER_ROLE)
                    .set(USER_ROLE.USER_DID, targetDid)
                    .set(USER_ROLE.ROLE_ID, adminRoleId)
                    .execute();
            model.addAttribute("message", "Admin role granted successfully!");
        } else {
            model.addAttribute("message", "User is already an admin.");
        }

        return "pages/admin/fragments/message";
    }

    @PostMapping("/admin/revoke")
    public String revokeAdmin(@RequestParam String targetDid, Model model) {
        var client = sessionService.getCurrentClient().orElseThrow();

        // Only SUPERADMINs can grant/revoke admin
        boolean requesterIsSuperadmin = dsl.fetchExists(
                dsl.selectOne().from(USER_ROLE)
                        .join(ROLE).on(USER_ROLE.ROLE_ID.eq(ROLE.ID))
                        .where(USER_ROLE.USER_DID.eq(client.getSession().getDid()))
                        .and(ROLE.NAME.eq("superadmin"))
        );
        if (!requesterIsSuperadmin) {
            model.addAttribute("message", "You are not authorized to revoke roles.");
            return "pages/admin/fragments/message";
        }

        Integer adminRoleId = dsl.select(ROLE.ID).from(ROLE)
                .where(ROLE.NAME.eq("admin")).fetchOne(ROLE.ID);
        Integer superadminRoleId = dsl.select(ROLE.ID).from(ROLE)
                .where(ROLE.NAME.eq("superadmin")).fetchOne(ROLE.ID);

        // Do not allow revoking from superadmins
        boolean targetIsSuperadmin = dsl.fetchExists(
                dsl.selectOne().from(USER_ROLE)
                        .where(USER_ROLE.USER_DID.eq(targetDid))
                        .and(USER_ROLE.ROLE_ID.eq(superadminRoleId))
        );
        if (targetIsSuperadmin) {
            model.addAttribute("message", "Cannot modify a superadmin.");
            return "pages/admin/fragments/message";
        }

        boolean targetIsAdmin = dsl.fetchExists(
                dsl.selectOne().from(USER_ROLE)
                        .where(USER_ROLE.USER_DID.eq(targetDid))
                        .and(USER_ROLE.ROLE_ID.eq(adminRoleId))
        );
        if (!targetIsAdmin) {
            model.addAttribute("message", "User is not an admin.");
            return "pages/admin/fragments/message";
        }

        int rows = dsl.deleteFrom(USER_ROLE)
                .where(USER_ROLE.USER_DID.eq(targetDid))
                .and(USER_ROLE.ROLE_ID.eq(adminRoleId))
                .execute();

        model.addAttribute("message",
                rows > 0 ? "Admin role removed successfully!" : "Nothing changed.");
        return "pages/admin/fragments/message";
    }

    public record AdminUser(
            String did,
            String handle,
            String displayName,
            String roleName
    ){}
}
