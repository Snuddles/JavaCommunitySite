@import com.jcs.javacommunitysite.atproto.AtUri

@param AtUri aturi
@param String username
@param String handle
@param String content
@param String timestamp

<div class="post op-post">
    <div class="content">
        <div class="header">
            <div class="profile-pic-container">
                <img src="/img/user.svg">
            </div>
            <div class="poster-info">
                <p class="username">${username}</p>
                <p class="handle">${handle}</p>
            </div>
            <div class="time-info">
                ${timestamp}
            </div>
            <div id="post-popup-button-container" class="popup-menu-container">
                <button
                        class="popup-menu-button"
                        hx-get="/post/${aturi.getDid()}/${aturi.getRecordKey()}/htmx/postMenu"
                        hx-target="#post-popup-button-container"
                        hx-swap="beforeend"
                >
                    <img src="/img/more.svg" alt="More Options" width="20" />
                </button>
            </div>
        </div>
        <div class="body">
            $unsafe{content}
        </div>
    </div>
</div>
