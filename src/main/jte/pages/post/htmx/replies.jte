@import com.jcs.javacommunitysite.jooq.tables.records.ReplyRecord
@import com.jcs.javacommunitysite.util.UserInfo
@import java.util.Map

@param ReplyRecord[] replies
@param Map<String, UserInfo> users
@param boolean hasMoreReplies
@param int nextPage

!{int amtReplies = replies.length;}
!{int i = 1;}

@for(var reply : replies)
    @template.pages.post.components.reply(
        reply = reply,
        user = users.get(reply.getOwnerDid()),
        selfHighlight = true,
        paginationTrigger = hasMoreReplies && (i == amtReplies),
        nextPage = nextPage,
        loggedInUser = users.get("self")
    )
    !{i++;}
@endfor
<div id="new-reply-insert-point"></div>

