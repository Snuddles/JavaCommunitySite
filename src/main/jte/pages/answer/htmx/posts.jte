@import com.jcs.javacommunitysite.atproto.AtUri
@import java.util.List
@import java.util.Map

@param List<Map<String, Object>> posts
@param boolean hasMoreAnswerPosts
@param int nextPageAnswerPosts
@param boolean filterMyPosts

!{int total = posts.size();}
!{int i = 1;}

@for(var post : posts)
    @template.components.post(
        aturi = new AtUri((String)post.get("aturi")),
        title = (String)post.get("title"),
        content = (String)post.get("content"),
        amtReplies = (post.get("countReplies") instanceof Number n ? n.intValue() : 0)
    )
    !{i++;}
@endfor

@if(hasMoreAnswerPosts)
    <div
        id="answer-posts-insert-point"
        style="width: 0; height: 0;"
        class="runner"
        hx-get="/answer/htmx/posts?page=${nextPageAnswerPosts}&filterMyPosts=${filterMyPosts}"
        hx-trigger="intersect once"
        hx-target="#answer-posts-insert-point"
        hx-swap="outerHTML"
    ></div>
@endif
