package com.jcs.javacommunitysite.util;

import com.jcs.javacommunitysite.atproto.AtUri;
import org.jooq.DSLContext;

import static com.jcs.javacommunitysite.jooq.tables.HiddenPost.HIDDEN_POST;
import static com.jcs.javacommunitysite.jooq.tables.HiddenReply.HIDDEN_REPLY;
import static com.jcs.javacommunitysite.jooq.tables.HiddenUser.HIDDEN_USER;

public class ModUtil {
    public static boolean isPostHidden(DSLContext dsl, AtUri atUri) {
        return dsl.fetchExists(HIDDEN_POST, HIDDEN_POST.POST_ATURI.eq(atUri.toString()));
    }

    public static boolean isReplyHidden(DSLContext dsl, AtUri atUri) {
        return dsl.fetchExists(HIDDEN_REPLY, HIDDEN_REPLY.REPLY_ATURI.eq(atUri.toString()));
    }

    public static boolean isUserHidden(DSLContext dsl, String did) {
        return dsl.fetchExists(HIDDEN_USER, HIDDEN_USER.TARGET_DID.eq(did));
    }

}
