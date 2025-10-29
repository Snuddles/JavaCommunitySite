@param String currentUserAvatarUrl = null
@param String currentUserHandle = null
@param String displayName = null
@param String description = null

@template.layout.main(
    title = "Profile Page",
    content = @`
    <div class="page-content">

        @template.components.pageHeader(
        content = @`<h2>Profile</h2>`,
        avatarUrl = currentUserAvatarUrl
        )

        <div class="user-info-container">
            <div class="not-the-button">
                <a href="/pfp" class="">
                    <img src="${currentUserAvatarUrl}" alt="Profile Avatar" style="width: 70px; height: 70px; border-radius: 50%;">
                </a>
                <div class="name-handle">
                    <h4>${displayName}</h4>
                    <p>@${currentUserHandle}</p>
                </div>
            </div>
            <div class="right-side-button-container">
                <button class="button" onclick="window.open('https://bsky.app/profile/${currentUserHandle}', '_blank')">Edit on Bluesky</button>
            </div>
        </div>
        <p>${description}</p>

        <div class="pfp-tabs"
             >
            <button hx-get="/pfp/tab/questions"
                    hx-target="#tab-content"
                    class="button active"
                    hx-on-click="
                        this.parentElement.querySelectorAll('.button')
                        .forEach(b => b.classList.remove('active'));
                        this.classList.add('active');"
            >Questions</button>
            <button hx-get="/pfp/tab/answers"
                    hx-target="#tab-content"
                    class="button"
                    hx-on-click="
                        this.parentElement.querySelectorAll('.button')
                        .forEach(b => b.classList.remove('active'));
                        this.classList.add('active');"
            >Answers</button>
        </div>
        <div class="divider"></div>

        <div id="tab-content"
             hx-get="/pfp/tab/questions"
             hx-trigger="load"
             hx-swap="innerHTML">
        </div>

    </div>
`)