@import com.jcs.javacommunitysite.atproto.AtUri
@import com.jcs.javacommunitysite.util.UserInfo
@import java.util.List
@import java.util.Map

@param com.jcs.javacommunitysite.pages.answerpage.NewReplyForm replyForm
@param List<Map<String, Object>> posts = null
@param boolean filterMyPosts = false
@param boolean hasMoreAnswerPosts = false
@param int nextPageAnswerPosts = 2
@param boolean loggedIn = false
@param UserInfo user = null

@template.layout.main(
    title = "Answer",
    content = @`
    <div class="page-content">
        @template.components.pageHeader(
            content = @`<h2>Open Questions</h2>`,
            user = user
        )

        <!-- Filter Checkbox -->
        <form method="get" action="/answer">
            <label>
                @if(filterMyPosts)
                    <input type="checkbox" 
                           name="filterMyPosts" 
                           value="true"
                           checked
                           onchange="this.form.submit()">
                @else
                    <input type="checkbox" 
                           name="filterMyPosts" 
                           value="true"
                           onchange="this.form.submit()">
                @endif
                Show only posts I've participated in
            </label>
        </form>

        <!-- Display Posts -->
        @if(posts != null && !posts.isEmpty())
            @for(var post : posts)
                @template.components.post(
                    aturi = new AtUri((String)post.get("aturi")),
                    title = (String)post.get("title"),
                    content = (String)post.get("content"),
                    amtReplies = (post.get("countReplies") instanceof Number n ? n.intValue() : 0)
                )
            @endfor
            @if(hasMoreAnswerPosts)
                <div
                    id="answer-posts-insert-point"
                    style="width: 0; height: 0;"
                    hx-get="/answer/htmx/posts?page=${nextPageAnswerPosts}&filterMyPosts=${filterMyPosts}"
                    hx-trigger="intersect once"
                    hx-target="#answer-posts-insert-point"
                    hx-swap="outerHTML"
                ></div>
            @endif
        @else
            <p>No posts found.</p>
        @endif
    </div>
    `
)