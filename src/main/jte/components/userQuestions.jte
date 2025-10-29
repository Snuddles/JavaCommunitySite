@import com.jcs.javacommunitysite.atproto.AtUri
@import java.util.List
@import java.util.Map

@param List<com.jcs.javacommunitysite.jooq.tables.records.PostRecord> userPosts = null
@param Map<String, Integer> replyCountsMap = null
@param Map<String, String> timeTextsMap = null
@param Map<String, List<String>> tagsMap = null

<div class="userPosts">
    @if(userPosts != null && !userPosts.isEmpty())
        @for(var post : userPosts)
            @template.components.post(
            aturi = new AtUri(post.getAturi()),
            title = post.getTitle(),
            content = post.getContent(),
            amtReplies = (replyCountsMap != null && replyCountsMap.containsKey(post.getAturi())) ? replyCountsMap.get(post.getAturi()) : 0,
            timeText = (timeTextsMap != null && timeTextsMap.containsKey(post.getAturi())) ? timeTextsMap.get(post.getAturi()) : null,
            tags = (tagsMap != null && tagsMap.containsKey(post.getAturi())) ? tagsMap.get(post.getAturi()) : null
            )
        @endfor
    @else
        <p>You haven't asked any questions yet. Ask your first question above!</p>
    @endif
</div>