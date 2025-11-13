package com.jcs.javacommunitysite.util;

import com.jcs.javacommunitysite.atproto.service.AtprotoSessionService;
import org.jooq.DSLContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.jcs.javacommunitysite.jooq.tables.Notification.NOTIFICATION;
import static com.jcs.javacommunitysite.jooq.tables.User.USER;
import static com.jcs.javacommunitysite.jooq.tables.UserRole.USER_ROLE;

public class UserInfo {
    public String handle;
    public String avatarUri;
    public String displayName;
    public String did;
    public String bio;
    public boolean isSelf;
    public boolean isAdmin;
    public int numUnreadNotifs;

    public UserInfo(String handle, String avatarUri, String displayName, String did, String bio, boolean isSelf, boolean isAdmin, int numUnreadNotifs) {
        this.handle = handle;
        this.avatarUri = avatarUri;
        this.displayName = displayName;
        this.did = did;
        this.bio = bio;
        this.isSelf = isSelf;
        this.isAdmin = isAdmin;
        this.numUnreadNotifs = numUnreadNotifs;
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

        var adminList = dsl.select(USER_ROLE.USER_DID)
                .from(USER_ROLE)
                .where(USER_ROLE.USER_DID.in(userDids))
                .and(USER_ROLE.ROLE_ID.in(1, 2))
                .fetchSet(USER_ROLE.USER_DID);

        // Get unread notifications count for self user
        var unreadNotifsCount = selfDid != null ?
                dsl.selectCount()
                        .from(NOTIFICATION)
                        .where(NOTIFICATION.RECIPIENT_USER_DID.eq(selfDid))
                        .and(NOTIFICATION.READ_AT.isNull())
                        .fetchOne(0, int.class) : 0;

        // Map users to map
        var usersMap = new HashMap<String, UserInfo>();
        for (var user : associatedUsers) {
            usersMap.put(user.getDid(), new UserInfo(
                    user.getHandle(),
                    user.getAvatarBloburl(),
                    user.getDisplayName(),
                    user.getDid(),
                    user.getDescription(),
                    selfDid != null && user.getDid().equals(selfDid),
                    adminList.contains(user.getDid()),
                    selfDid != null && user.getDid().equals(selfDid) ? unreadNotifsCount : 0
            ));
        }

        // Set a key 'self' pointing to the user's own UserInfo
        if (selfDid != null && usersMap.containsKey(selfDid)) {
            usersMap.put("self", usersMap.get(selfDid));
        }

        return usersMap;
    }

    public static UserInfo getFromDb(DSLContext dsl, AtprotoSessionService session, String did) {
        String selfDid = null;
        if (session.isAuthenticated()) {
            selfDid = session.getCurrentClient().get().getSession().getDid();
        }

        // Query
        var user = dsl.selectFrom(USER)
                .where(USER.DID.eq(did))
                .fetchOne();

        int numUnreadNotifs = did.equals(selfDid) ?
                dsl.selectCount()
                    .from(NOTIFICATION)
                    .where(NOTIFICATION.RECIPIENT_USER_DID.eq(selfDid))
                    .and(NOTIFICATION.READ_AT.isNull())
                    .fetchOne(0, int.class) : 0;

        return new UserInfo(
                user.getHandle(),
                user.getAvatarBloburl(),
                user.getDisplayName(),
                user.getDid(),
                user.getDescription(),
                selfDid != null && selfDid.equals(did),
                isAdmin(dsl, did),
                numUnreadNotifs
        );
    }

    public static UserInfo getSelfFromDb(DSLContext dsl, AtprotoSessionService session) {
        if (!session.isAuthenticated()) {
            return null;
        }
        return getFromDb(dsl, session, session.getCurrentClient().get().getSession().getDid());
    }
    
    public static boolean isAdmin(DSLContext dsl, String userDid) {
        return dsl.fetchExists(USER_ROLE,
                USER_ROLE.USER_DID.eq(userDid)
                        .and(USER_ROLE.ROLE_ID.in(1, 2))); // 1 = superadmin, 2 = admin
    }
}
