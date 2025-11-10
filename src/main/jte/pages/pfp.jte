@import com.jcs.javacommunitysite.util.UserInfo

@param String description = null
@param UserInfo loggedInUser
@param UserInfo user

!{String displayName = !user.displayName.isBlank() ? user.displayName : user.handle;}

@template.layout.main(
    title = "Profile Page",
    content = @`
    <div class="page-content">

        @template.components.pageHeader(
            content = @`<h2>Profile</h2>`,
            user = loggedInUser,
        )

        <div class="user-info-container">
            <div class="not-the-button">
                <a href="/pfp" class="">
                    <img src="${user.avatarUri}" alt="Profile Avatar" style="width: 70px; height: 70px; border-radius: 50%;">
                </a>
                <div class="name-handle">
                    <div class="top-bar">
                        <h4>${displayName}</h4>
                        @if(user.isAdmin)
                            <div class="tag">Admin</div>
                        @endif
                    </div>
                    <p>@${user.handle}</p>
                </div>
            </div>
            @if(user.isSelf)
                <div class="right-side-button-container">
                    <button class="button" onclick="window.open('https://bsky.app/profile/${user.handle}', '_blank')">Edit on Bluesky</button>
                </div>
            @endif
        </div>
        <p>${user.bio}</p>

        <div class="divider"></div>

        <div class="pfp-tabs">
            <button hx-get="/pfp/tab/questions?did=${user.did}"
                    hx-target="#tab-content"
                    class="button active"
                    hx-on-click="
                        this.parentElement.querySelectorAll('.button')
                        .forEach(b => b.classList.remove('active'));
                        this.classList.add('active');"
            >Questions</button>
            <button hx-get="/pfp/tab/answers?did=${user.did}"
                    hx-target="#tab-content"
                    class="button"
                    hx-on-click="
                        this.parentElement.querySelectorAll('.button')
                        .forEach(b => b.classList.remove('active'));
                        this.classList.add('active');"
            >Answers</button>
        </div>

        <div id="tab-content"
             hx-get="/pfp/tab/questions?did=${user.did}"
             hx-trigger="load"
             hx-swap="innerHTML">
        </div>

    </div>
`)