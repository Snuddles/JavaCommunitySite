@import com.jcs.javacommunitysite.atproto.AtUri
@import com.jcs.javacommunitysite.atproto.records.PostRecord
@import java.util.List
@import java.util.Map

@param com.jcs.javacommunitysite.pages.answerpage.NewReplyForm replyForm
@param String currentUserAvatarUrl = null
@param List<Map<String, Object>> posts = null
@param boolean filterMyPosts = false

@template.layout.main(
    title = "Answer",
    content = @`
    <div class="page-content">
        @template.components.pageHeader(
            content = @`<h2>Open Questions</h2>`
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
                    amtReplies = 0
                )
            @endfor
        @else
            <p>No posts found.</p>
        @endif
    </div>
    `
)