@import com.jcs.javacommunitysite.forms.NewReplyForm
@import com.jcs.javacommunitysite.jooq.tables.records.ReplyRecord

@param String did
@param String rkey

@param String title
@param String postContent
@param String postTimestamp

@param ReplyRecord[] postReplies

@param NewReplyForm newReplyForm

@template.layout.main(
title = title,
content = @`
    <div class="post-page">
        <h2>${title}</h2>
        @template.pages.post.components.opPost(content=postContent, username="Test", handle="test.bsky.social", timestamp=postTimestamp)

        @for(var reply : postReplies)
            <div class="mid-reply-runner"></div>
            @template.pages.post.components.reply(reply=reply)
        @endfor

        <div id="new-reply-insert-point"></div>

        <form
            class="reply-container"
            hx-post="/post/${did}/${rkey}/htmx/reply"
            hx-target="#new-reply-insert-point"
            hx-target-error="#reply-error-container"
            hx-swap="outerHTML"
            hx-disabled-elt="#reply-textarea, #reply-button"
            hx-indicator="#reply-indicator"
            hx-on::after-request="if (event.detail.successful) this.reset()"
        >
            <p>Make a Reply</p>
            <div class="reply-bottom">
                <img src="/img/user.svg">
                <textarea id="reply-textarea" class="post-textarea" name="content" required>${newReplyForm.getContent()}</textarea>
            </div>
            <div class="reply-buttons" type="submit">
                <img id="reply-indicator" class="htmx-indicator" src="/throbber.svg" width="24" />
                <button id="reply-button" class="button" disabled>Reply</button>
            </div>
            <div id="reply-error-container"></div>
            <div class="runner"></div>

            <script>
                // Disable reply button when textarea empty
                const textarea = document.getElementById("reply-textarea")
                const button = document.getElementById("reply-button")
                textarea.addEventListener("input", (e) => {
                    button.disabled = e.target.value.length <= 0;
                })
            </script>
        </form>
    </div>
`
)