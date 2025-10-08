@import java.util.ArrayList

@template.layout.main(
    title = "Post",
    stylesheets = new ArrayList<String>(),
    content = @`
    <div class="post-page">
        <h2>What is Springboot?</h2>
        @template.components.opPost()
        <div class="mid-reply-runner"></div>
        @template.components.reply()
        <div class="mid-reply-runner"></div>
        @template.components.reply()
        <div class="mid-reply-runner"></div>
        @template.components.reply()
        <div class="mid-reply-runner"></div>
        <div class="reply-container">
            <p>Make a Reply</p>
            <div class="reply-bottom">
                <img src="/img/user.svg">
                <textarea class="post-textarea"></textarea>
            </div>
            <div class="reply-buttons">
                <button class="button">Reply</button>
            </div>
            <div class="runner"></div>
        </div>
    </div>
    `
)