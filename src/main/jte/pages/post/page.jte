@import com.jcs.javacommunitysite.atproto.AtUri
@import com.jcs.javacommunitysite.forms.NewReplyForm
@import com.jcs.javacommunitysite.jooq.tables.records.PostRecord
@import com.jcs.javacommunitysite.jooq.tables.records.ReplyRecord
@import com.jcs.javacommunitysite.util.UserInfo
@import java.util.Map

@param PostRecord post
@param Map<String, UserInfo> users
@param ReplyRecord[] postReplies
@param NewReplyForm newReplyForm
@param boolean isPostHidden


!{AtUri aturi = new AtUri(post.getAturi());}

@template.layout.main(
title = post.getTitle(),
content = @`
    <div class="post-page">
        @template.components.pageHeader(
            content = @`<h3 style="color: var(--subtext-color)">Question</h3>`,
            user = users.get("self"),
        )

        <h2>${post.getTitle()}</h2>
        @template.pages.post.components.opPost(
            post = post,
            user = users.get(post.getOwnerDid()),
            isHidden = isPostHidden
        )

        !{int i = 1;}
        @for(var reply : postReplies)
            @template.pages.post.components.reply(
                reply=reply,
                user=users.get(reply.getOwnerDid()),
                paginationTrigger = (i == postReplies.length),
                nextPage = 2,
            )
            !{i++;}
        @endfor

        <div id="new-reply-insert-point"></div>

        <div class="mid-reply-runner"></div>

        @if(users.containsKey("self")) <!-- simple logged-in check -->
            @if(post.getIsOpen())
                <form
                    id="reply-container"
                    class="reply-container"
                    hx-post="/post/${aturi.getDid()}/${aturi.getRecordKey()}/htmx/reply"
                    hx-target="#new-reply-insert-point"
                    hx-swap="outerHTML"
                    hx-disabled-elt="#reply-textarea, #reply-button"
                    hx-indicator="#reply-indicator"
                    hx-on::after-request="if (!isError(event.detail.xhr) && event.target == this) this.reset()"
                >
                    <p class="make-reply-title">Make a Reply</p>
                    <div class="reply-bottom">
                        <img src="${users.get("self").avatarUri}">
                        @template.components.postEditor(content=newReplyForm.getContent(), id="reply")
                    </div>
                    <div class="reply-buttons" type="submit">
                        <img id="reply-indicator" class="htmx-indicator" src="/throbber.svg" width="24" />
                        <button id="reply-button" class="button" disabled>Reply</button>
                    </div>
                    <div id="reply-error-container"></div>
                    <div class="runner"></div>

                    @if(postReplies.length >= 1)
                        <div class="info-container" style="margin-top: 20px;">
                            <p>Has your question been answered?</p>
                            <button
                                class="button"
                                type="button"
                                hx-get="/post/${aturi.getDid()}/${aturi.getRecordKey()}/htmx/confirmClosePost"
                                hx-target="body"
                                hx-swap="beforeend"
                            >
                                Close Question
                            </button>
                        </div>
                    @endif

                    <script>
                        // Disable reply button when textarea empty
                        const textarea = document.getElementById("post-input-reply")
                        const button = document.getElementById("reply-button")
                        textarea.addEventListener("input", (e) => {
                            button.disabled = e.target.value.length <= 0;
                        })
                    </script>
                </form>
            @else
                @template.pages.post.components.closedPostNotif()
            @endif
        @else
            <div class="login-container">
                <p>
                    To reply to a question, please
                    <a href="/login?next=/post/${aturi.getDid()}/${aturi.getRecordKey()}&msg=To reply, please log in">
                        log in.
                    </a>
                </p>
            </div>
        @endif
    </div>
`
)