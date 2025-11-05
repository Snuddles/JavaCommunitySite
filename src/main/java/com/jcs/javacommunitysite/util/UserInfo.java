package com.jcs.javacommunitysite.util;

import com.jcs.javacommunitysite.atproto.service.AtprotoSessionService;
import org.jooq.DSLContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.jcs.javacommunitysite.jooq.tables.User.USER;
import static com.jcs.javacommunitysite.jooq.tables.UserRole.USER_ROLE;

public class UserInfo {
    public String handle;
    public String avatarUri;
    public String displayName;
    public String did;

    public UserInfo(String handle, String avatarUri, String displayName, String did) {
        this.handle = handle;
        this.avatarUri = avatarUri;
        this.displayName = displayName;
        this.did = did;
    }

    public static Map<String, UserInfo> getFromDb(DSLContext dsl, AtprotoSessionService session, Set<String> userDids) {
        // Insert self is logged in
        String selfDid = null;
        if (session.isAuthenticated()) {
            selfDid = session.getCurrentClient().get().getSession().getDid();
            userDids.add(selfDid);
        }

        // Query
        var associatedUsers = dsl.selectFrom(USER)
                .where(USER.DID.in(userDids))
                .fetchArray();

        // Map users to map
        var usersMap = new HashMap<String, UserInfo>();
        for (var user : associatedUsers) {
            usersMap.put(user.getDid(), new UserInfo(
                    user.getHandle(),
                    user.getAvatarBloburl(),
                    user.getDisplayName(),
                    user.getDid()
            ));
        }

        // Set a key 'self' pointing to the user's own UserInfo
        if (selfDid != null && usersMap.containsKey(selfDid)) {
            usersMap.put("self", usersMap.get(selfDid));
        }

        return usersMap;
    }

    public static UserInfo getFromDb(DSLContext dsl, String did) {
        // Query
        var user = dsl.selectFrom(USER)
                .where(USER.DID.eq(did))
                .fetchOne();

        return new UserInfo(
                user.getHandle(),
                user.getAvatarBloburl(),
                user.getDisplayName(),
                user.getDid()
        );
    }
    
    public static boolean isAdmin(DSLContext dsl, String userDid) {
        return dsl.fetchExists(USER_ROLE,
                USER_ROLE.USER_DID.eq(userDid)
                        .and(USER_ROLE.ROLE_ID.in(1, 2))); // 1 = superadmin, 2 = admin
    }
}
