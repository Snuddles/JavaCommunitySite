@import com.jcs.javacommunitysite.atproto.AtUri
@import com.jcs.javacommunitysite.jooq.tables.records.PostRecord

@param PostRecord post

!{AtUri aturi = new AtUri(post.getAturi());}


<div id="post" class="post op-post">
    <div class="content">
        <div class="header">
            <div class="profile-pic-container">
                <img src="/img/user.svg">
            </div>
            <div class="poster-info">
                <p class="username">Test Username</p>
                <p class="handle">${aturi.getDid()}</p>
            </div>
            <div class="time-info">
                ${post.getCreatedAt().toString()}
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
            $unsafe{post.getContent()}
        </div>
    </div>
</div>
