@import com.jcs.javacommunitysite.atproto.AtUri
@import com.jcs.javacommunitysite.atproto.records.PostRecord

@template.layout.main(
    title = "Answer",
    content = @`
    <div class="page-content">
        @template.components.pageHeader(
            content = @`<h2>Open Questions</h2>`
        )

        <div class="section-marker-container">
            <div class="marker green"></div>
            <h3>Participated</h3>
        </div>

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
        )
        @template.components.post(
            aturi = new AtUri("123", PostRecord.recordCollection, "abc"),
            title = "My Post",
            content = "Lorem Ipsum dolor amet Lorem Ipsum dolor amet Lorem Ipsum dolor amet Lorem Ipsum dolor amet",
            amtReplies = 24
        )

        <div class="section-marker-container">
            <div class="marker orange"></div>
            <h3>Available</h3>
        </div>

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
    </div>
    `
)