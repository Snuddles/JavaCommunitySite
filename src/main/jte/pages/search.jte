@import com.jcs.javacommunitysite.atproto.AtUri
@import com.jcs.javacommunitysite.atproto.records.PostRecord
@import com.jcs.javacommunitysite.pages.searchpage.SearchForm
@import com.jcs.javacommunitysite.pages.searchpage.SearchResult

@param SearchForm searchForm = null
@param java.util.List<SearchResult> searchResults = null
@param Boolean hasResults = false
@param Boolean hasQuery = false
@param String currentUserAvatarUrl = null

@template.layout.main(
    title = "Search",
    content = @`
    <div class="page-content">
        @template.components.pageHeader(
            content = @`<h2>Search</h2>`,
            avatarUrl = currentUserAvatarUrl
        )

        <div class="search-box-container">
            <form method="GET" action="/search" class="search-form">
                <input class="textbox" type="text" name="q" placeholder="Enter a search term" 
                       value="${searchForm != null ? searchForm.getQuery() : ""}" />
                <button class="button" type="submit">Search</button>
                
                @if(searchForm != null && searchForm.getStatus() != null && !searchForm.getStatus().equals("all"))
                    <input type="hidden" name="status" value="${searchForm.getStatus()}" />
                @endif
                @if(searchForm != null && searchForm.getSortBy() != null && !searchForm.getSortBy().equals("relevance"))
                    <input type="hidden" name="sort" value="${searchForm.getSortBy()}" />
                @endif
            </form>
            
            @if(hasQuery)
                <div class="filters">
                    <a href="/search?q=${searchForm.getQuery()}&status=all&sort=${searchForm.getSortBy()}" 
                       class="filter-button ${searchForm.getStatus().equals("all") ? "active" : ""}">All</a>
                    <a href="/search?q=${searchForm.getQuery()}&status=open&sort=${searchForm.getSortBy()}" 
                       class="filter-button ${searchForm.getStatus().equals("open") ? "active" : ""}">Open</a>
                    <a href="/search?q=${searchForm.getQuery()}&status=closed&sort=${searchForm.getSortBy()}" 
                       class="filter-button ${searchForm.getStatus().equals("closed") ? "active" : ""}">Closed</a>
                </div>
                
                <div class="filters sort-filters">
                    <span class="sort-label">Sort by:</span>
                    <a href="/search?q=${searchForm.getQuery()}&status=${searchForm.getStatus()}&sort=relevance" 
                       class="filter-button ${searchForm.getSortBy().equals("relevance") ? "active" : ""}">Relevance</a>
                    <a href="/search?q=${searchForm.getQuery()}&status=${searchForm.getStatus()}&sort=newest" 
                       class="filter-button ${searchForm.getSortBy().equals("newest") ? "active" : ""}">Newest</a>
                    <a href="/search?q=${searchForm.getQuery()}&status=${searchForm.getStatus()}&sort=oldest" 
                       class="filter-button ${searchForm.getSortBy().equals("oldest") ? "active" : ""}">Oldest</a>
                    <a href="/search?q=${searchForm.getQuery()}&status=${searchForm.getStatus()}&sort=most_replies" 
                       class="filter-button ${searchForm.getSortBy().equals("most_replies") ? "active" : ""}">Most Replies</a>
                </div>
            @endif
        </div>

        <div class="search-results">
            @if(hasQuery && !hasResults)
                <div class="no-results">
                    <p>No posts found matching "${searchForm.getQuery()}". Try different keywords or check your spelling.</p>
                </div>
            @elseif(hasQuery && hasResults)
                <div class="results-info">
                    Found ${searchResults.size()} result${searchResults.size() == 1 ? "" : "s"} for "${searchForm.getQuery()}"
                </div>
                
                <ul class="results-list">
                    @for(var result : searchResults)
                        @template.components.post(
                            aturi = result.getAtUri(),
                            title = result.getTitle(),
                            content = result.getContent(),
                            amtReplies = result.getReplyCount(),
                            timeText = result.getTimeText(),
                            tags = result.getTags()
                        )
                    @endfor
                </ul>
            @elseif(!hasQuery)
                <div class="no-results">
                    <p>Enter a search term above to find posts by title, content, or tags.</p>
                </div>
            @endif
        </div>
    </div>
    `
)