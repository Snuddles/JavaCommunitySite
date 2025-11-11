@import com.jcs.javacommunitysite.util.UserInfo

@param gg.jte.Content content
@param String backHref = null
@param UserInfo user = null

<div class="page-header">
    <div class="left-side">
        @if(backHref != null)
            <a class="button icon" href="${backHref}">&larr;</a>
        @endif
        <div>
            ${content}
        </div>
    </div>
    <div class="top-right-buttons">
        @if(user != null)
            <a class="button icon" href="/notifications">
                <img src="/bell.svg" alt="Notifications" />
            </a>
            <div class="pfp-container">
                <img
                    id="profileMenu-button"
                    src="${user.avatarUri}"
                    alt="Profile"
                    class="pfp-img"
                    onclick="togglePopup('profileMenu')"
                    style="width: 32px; height: 32px; border-radius: 50%;"
                />
                <div class="profile-menu-popup-container">
                    @template.components.profileMenu(
                        user = user
                    )
                </div>
            </div>
        @else
            <a class="button" href="/login">Log in</a>
        @endif
    </div>
</div>