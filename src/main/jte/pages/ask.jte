@import com.jcs.javacommunitysite.atproto.AtUri
@import com.jcs.javacommunitysite.atproto.records.PostRecord

@param com.jcs.javacommunitysite.pages.askpage.NewPostForm postForm

@template.layout.main(
    title = "Ask",
    content = @`
    <div class="page-content">

        @template.components.pageHeader(
            content = @`<h2>Ask a Question</h2>`
        )

        <div class="new-question-container">
            <form method="post">
                <div class="form-group">
                    <label for="title-input">Title</label>
                    <input id="title-input" class="textbox" type="text" name="title" value="${postForm.getTitle()}" placeholder="My Really Good Title" required>
                </div>

                <div class="form-group">
                    <label for="post-input">Post</label>
                    <textarea id="post-input" class="post-textarea" name="content" name="content" value="${postForm.getContent()}" required></textarea>
                </div>

                Put tags here

                <div class="right-side-buttons-container">
                    <button class="button" type="submit">Create Question</button>
                </div>
            </form>
        </div>

        <div class="divider"></div>

        <h2>My Questions</h2>

        @template.components.post(
            aturi = new AtUri("123", PostRecord.recordCollection, "abc"),
            title = "My Post",
            content = "Lorem Ipsum dolor amet Lorem Ipsum dolor amet Lorem Ipsum dolor amet Lorem Ipsum dolor amet",
            amtReplies = 24
        )
        @template.components.post(
            aturi = new AtUri("123", PostRecord.recordCollection, "abc"),
            title = "My Post",
            content = "Lorem Ipsum dolor amet Lorem Ipsum dolor amet Lorem Ipsum dolor amet Lorem Ipsum dolor amet",
            amtReplies = 24
        )
        @template.components.post(
            aturi = new AtUri("123", PostRecord.recordCollection, "abc"),
            title = "My Post",
            content = "Lorem Ipsum dolor amet Lorem Ipsum dolor amet Lorem Ipsum dolor amet Lorem Ipsum dolor amet",
            amtReplies = 24
        )@template.components.post(
            aturi = new AtUri("123", PostRecord.recordCollection, "abc"),
            title = "My Post",
            content = "Lorem Ipsum dolor amet Lorem Ipsum dolor amet Lorem Ipsum dolor amet Lorem Ipsum dolor amet",
            amtReplies = 24
        )



    </div>
    `
)