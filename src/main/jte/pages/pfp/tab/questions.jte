@import com.jcs.javacommunitysite.atproto.AtUri
@import java.util.List
@import java.util.Map

@param List<com.jcs.javacommunitysite.jooq.tables.records.PostRecord> userPosts
@param Map<String, Integer> replyCountsMap
@param Map<String, String> timeTextsMap
@param Map<String, List<String>> tagsMap
@param boolean hasMoreUserPosts = false
@param int nextPageUserPosts = 2
@param String did

<div class="userPosts">
    @if(userPosts != null && !userPosts.isEmpty())
        @for(var post : userPosts)
            @template.components.post(
                aturi = new AtUri(post.getAturi()),
                title = post.getTitle(),
                content = post.getContent(),
                amtReplies = (replyCountsMap != null && replyCountsMap.containsKey(post.getAturi())) ? replyCountsMap.get(post.getAturi()) : 0,
                timeText = null,
                tags = null
            )
        @endfor
        @if(hasMoreUserPosts)
            <div
                id="pfp-questions-insert-point"
                style="width: 0; height: 0;"
                hx-get="/pfp/tab/questions?page=${nextPageUserPosts}&did=${did}"
                hx-trigger="intersect once"
                hx-target="#pfp-questions-insert-point"
                hx-swap="outerHTML"
            ></div>
        @endif
    @else
        <p>This user hasn't asked any questions yet.</p>
    @endif
</div>
