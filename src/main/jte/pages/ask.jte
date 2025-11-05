@import com.jcs.javacommunitysite.atproto.AtUri
@import java.util.List
@import java.util.Map

@param com.jcs.javacommunitysite.pages.askpage.NewPostForm postForm
@param String currentUserAvatarUrl = null
@param List<com.jcs.javacommunitysite.jooq.tables.records.PostRecord> userPosts = null
@param Map<String, Integer> replyCountsMap = null
@param Map<String, String> timeTextsMap = null
@param Map<String, String> tags = null
@param Map<String, List<String>> postTags = null
@param boolean loggedIn

@template.layout.main(
    title = "Ask",
    content = @`
    <div class="page-content">

        @template.components.pageHeader(
            content = @`<h2>Ask a Question</h2>`,
            avatarUrl = currentUserAvatarUrl
        )

        @if(loggedIn)

            <div class="contained-section new-question-container">
                <form
                    hx-post="/ask"
                    hx-target="#my-questions-header"
                    hx-swap="afterend"
                    hx-on::after-request="if (event.detail.successful && event.target == this) this.reset()"
                >
                    <div class="form-group">
                        <label for="title-input">Title</label>
                        <input id="title-input" class="textbox" type="text" name="title" value="${postForm.getTitle()}" placeholder="My Really Good Title" required>
                    </div>

                    <div class="form-group">
                        <label for="post-input">Post</label>
                        @template.components.postEditor()
                    </div>

                    <div class="form-group">
                        <label for="tags-input">Tags</label>
                        <div class="tags-input-container">
                            <div class="tag-selector">
                                @for(var tag : tags.values())
                                    <label class="tag-selector-item">
                                        <input type="checkbox" name="tags" value="${tag}" class="tag-button">
                                        <span>${tag}</span>
                                    </label>
                                @endfor
                            </div>
                        </div>
                    </div>

                    <div class="right-side-buttons-container">
                        <button class="button" type="submit">Create Question</button>
                    </div>
                </form>
            </div>

            <h2 id="my-questions-header">My Questions</h2>

            @if(userPosts != null && !userPosts.isEmpty())
                @for(var post : userPosts)
                    @template.components.post(
                        aturi = new AtUri(post.getAturi()),
                        title = post.getTitle(),
                        content = post.getContent(),
                        amtReplies = (replyCountsMap != null && replyCountsMap.containsKey(post.getAturi())) ? replyCountsMap.get(post.getAturi()) : 0,
                        timeText = (timeTextsMap != null && timeTextsMap.containsKey(post.getAturi())) ? timeTextsMap.get(post.getAturi()) : null,
                        tags = (tags != null && postTags.containsKey(post.getAturi())) ? postTags.get(post.getAturi()) : null
                    )
                @endfor
            @else
                <p>You haven't asked any questions yet. Ask your first question above!</p>
            @endif

        @else
            <p>
                To ask a question, please
                <a href="/login?next=/ask&msg=To ask a question, please log in">
                    log in.
                </a>
            </p>
        @endif

    </div>
    `
)