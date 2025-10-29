@import com.jcs.javacommunitysite.pages.searchpage.SearchForm

@param SearchForm searchForm = null
@param String currentUserAvatarUrl = null

@template.layout.main(
    title = "Search",
    content = @`
    <div class="page-content">
        @template.components.pageHeader(
            content = @`<h2>Search</h2>`,
            avatarUrl = currentUserAvatarUrl
        )

        <form
            class="search-box-container"
            hx-post="/search"
            hx-target="#search-results"
            hx-trigger="input delay:750ms, submit"
            hx-indicator="#indicator, #search-results"
            action=""
        >
            <div class="search-form-top">
                <input class="textbox" type="text" name="query" placeholder="Enter a search term"
                       value="" />
                <button class="button" type="submit">Search</button>
            </div>

            <div class="filters">
                <div class="filter-button-container">
                    <button id="statusSelectorButton" type="button" class="filter-button" onclick="togglePopup('statusSelector')">
                        Status
                        <img src="/arrow-down.svg" alt="" />
                    </button>
                    @template.pages.search.components.statusSelectorPopup(id = "statusSelector")
                </div>

                <div class="filter-button-container">
                    <button id="tagSelectorButton" type="button" class="filter-button" onclick="togglePopup('tagSelector')">
                        Tags
                        <img src="/arrow-down.svg" alt="" />
                    </button>
                    @template.pages.search.components.tagSelectorPopup(id = "tagSelector")
                </div>

                <div class="filter-button-container">
                    <button id="sortSelectorButton" type="button" class="filter-button" onclick="togglePopup('sortSelector')">
                        Sort
                        <img src="/arrow-down.svg" alt="" />
                    </button>
                    @template.pages.search.components.sortSelectorPopup(id = "sortSelector")
                </div>
            </div>
        </form>

        <div id="indicator" class="search-indicator htmx-indicator">
            <img src="throbber.svg" alt="Loading..." />
        </div>

        <div id="search-results" class="search-results htmx-indicator">
            <div class="no-results">
                <p>Enter a search term above to find posts by title, content, or tags.</p>
            </div>
        </div>
    </div>

    <script>
        const popups = ["tagSelector", "sortSelector", "statusSelector"]

        function togglePopup(id) {
            const element = document.getElementById(id)
            const elementButton = document.getElementById(id + "Button")
            const others = popups.filter(x => x !== id)
            others.forEach(x => document.getElementById(x).classList.add("hidden"))
            others.forEach(x => document.getElementById(x + "Button").classList.remove("active"))
            const nowHidden = element.classList.toggle("hidden")
            element.classList.remove("preload")
            elementButton.classList.toggle("active")

            if (!nowHidden) {
                const abortController = new AbortController()
                document.addEventListener("click", (e) => {
                    if (elementButton.contains(e.target)) return
                    if (!element.contains(e.target)) {
                        element.classList.add("hidden")
                        elementButton.classList.remove("active")
                        abortController.abort()
                    }
                }, { signal: abortController.signal })
            }
        }
    </script>
    `
)