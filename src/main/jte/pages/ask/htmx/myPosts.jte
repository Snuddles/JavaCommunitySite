@import com.jcs.javacommunitysite.atproto.AtUri
@import com.jcs.javacommunitysite.util.TimeUtil
@import java.util.List
@import java.util.Map

@param List<com.jcs.javacommunitysite.jooq.tables.records.PostRecord> userPosts
@param Map<String, Integer> replyCountsMap
@param Map<String, String> timeTextsMap
@param Map<String, List<String>> postTags
@param boolean hasMoreUserPosts
@param int nextPageUserPosts

!{int total = userPosts.size();}
!{int i = 1;}

@for(var post : userPosts)
    @template.components.post(
        aturi = new AtUri(post.getAturi()),
        title = post.getTitle(),
        content = post.getContent(),
        amtReplies = (replyCountsMap != null && replyCountsMap.containsKey(post.getAturi())) ? replyCountsMap.get(post.getAturi()) : 0,
        timeText = TimeUtil.calculateTimeText(post.getCreatedAt()),
        tags = null,
        closed = !post.getIsOpen(),
    )
    !{i++;}
@endfor

@if(hasMoreUserPosts)
    <div
        id="my-questions-insert-point"
        style="width: 0; height: 0;"
        hx-get="/ask/htmx/myPosts?page=${nextPageUserPosts}"
        hx-trigger="intersect once"
        hx-target="#my-questions-insert-point"
        hx-swap="outerHTML"
    ></div>
@endif
