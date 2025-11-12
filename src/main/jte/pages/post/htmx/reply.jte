@import com.jcs.javacommunitysite.jooq.tables.records.ReplyRecord
@import com.jcs.javacommunitysite.util.UserInfo

@param ReplyRecord reply
@param UserInfo user

@template.pages.post.components.reply(
    reply=reply,
    user=user,
    selfHighlight=true,
    loggedInUser = user,
)
<div id="new-reply-insert-point"></div>
