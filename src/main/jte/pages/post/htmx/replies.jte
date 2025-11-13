@import com.jcs.javacommunitysite.jooq.tables.records.ReplyRecord
@import com.jcs.javacommunitysite.util.UserInfo
@import java.util.Arrays
@import java.util.Map

@param ReplyRecord[] replies
@param Map<String, UserInfo> users
@param boolean hasMoreReplies
@param int nextPage
@param String[] hiddenReplies = new String[0]

!{int amtReplies = replies.length;}
!{int i = 1;}
!{var hiddenRepliesList = Arrays.asList(hiddenReplies);}

@for(var reply : replies)
    !{var isHidden = hiddenRepliesList.contains(reply.getAturi());}
    @template.pages.post.components.reply(
        reply = reply,
        user = users.get(reply.getOwnerDid()),
        selfHighlight = true,
        paginationTrigger = hasMoreReplies && (i == amtReplies),
        nextPage = nextPage,
        loggedInUser = users.get("self"),
        isHidden = isHidden
    )
    !{i++;}
@endfor
<div id="new-reply-insert-point"></div>

