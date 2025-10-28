@import com.jcs.javacommunitysite.atproto.AtUri
@import com.jcs.javacommunitysite.atproto.records.PostRecord

@template.layout.main(
    title = "Search",
    content = @`
    <div class="page-content">
        @template.components.pageHeader(
            content = @`<h2>Search</h2>`
        )

        <div class="search-box-container">
            <div style="display: flex; gap: 10px; align-items: center;">
                <input class="textbox" type="text" placeholder="Enter a search term" />
                <button class="button">Search</button>
            </div>
            <div class="filters">
                <button class="filter-button active">Status</button>
                <button class="filter-button">Tags</button>
                <button class="filter-button">Sort</button>
            </div>
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
        )@template.components.post(
            aturi = new AtUri("123", PostRecord.recordCollection, "abc"),
            title = "My Post",
            content = "Lorem Ipsum dolor amet Lorem Ipsum dolor amet Lorem Ipsum dolor amet Lorem Ipsum dolor amet",
            amtReplies = 24
        )
    </div>
    `
)