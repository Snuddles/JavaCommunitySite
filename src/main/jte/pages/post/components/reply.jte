@import com.jcs.javacommunitysite.atproto.AtUri
@import com.jcs.javacommunitysite.jooq.tables.records.ReplyRecord

@param ReplyRecord reply

!{AtUri root = new AtUri(reply.getRoot());}
!{
    AtUri self = null;
    if (reply.getAturi() != null) {
        self = new AtUri(reply.getAturi());
    }
}

<div id="reply-${self == null ? "new" : self.getRecordKey()}">
    <div class="mid-reply-runner"></div>
    <div class="post">
        <div class="content">
            <div class="header">
                <div class="profile-pic-container">
                    <img src="/img/user.svg">
                </div>
                <div class="poster-info">
                    <p class="username">Test User</p>
                    <p class="handle">@test.com</p>
                </div>
                <div class="time-info">
                    ${reply.getCreatedAt().toString()}
                </div>
                @if(self != null)
                    <div id="reply-popup-button-container-${self.getRecordKey()}" class="popup-menu-container">
                        <button
                                class="popup-menu-button"
                                hx-get="/post/${root.getDid()}/${root.getRecordKey()}/htmx/replyMenu?reply=${reply.getAturi()}"
                                hx-target="#reply-popup-button-container-${self.getRecordKey()}"
                                hx-swap="beforeend"
                        >
                            <img src="/img/more.svg" alt="More Options" width="20" />
                        </button>
                    </div>
                @endif
            </div>
            <div class="body">
                <p>
                    $unsafe{reply.getContent().toString()}
                </p>
            </div>
        </div>

        <div class="runner"></div>
    </div>
</div>
