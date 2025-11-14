@import com.jcs.javacommunitysite.atproto.AtUri
@import com.jcs.javacommunitysite.jooq.tables.records.ReplyRecord
@import com.jcs.javacommunitysite.util.MarkdownUtil
@import com.jcs.javacommunitysite.util.TimeUtil
@import com.jcs.javacommunitysite.util.UserInfo

@param ReplyRecord reply
@param UserInfo loggedInUser
@param UserInfo user
@param boolean isHidden = false
@param boolean selfHighlight = false
@param boolean paginationTrigger = false
@param int nextPage = -1

!{AtUri root = new AtUri(reply.getRootPostAturi());}
!{
    AtUri self = new AtUri(reply.getAturi());
}

!{String displayName = !user.displayName.isBlank() ? user.displayName : user.handle;}

<div id="reply-${self.getRecordKey()}" class="${isHidden ? "hidden" : ""}">
    <div class="mid-reply-runner"></div>
    <div class="post">
        <div class="content">
            <div class="header">
                <div class="profile-pic-container">
                    <img src="${user.avatarUri}" alt="Profile Picture" />
                </div>
                <div class="poster-info">
                    <div class="top-bar">
                        <p class="username">${displayName}</p>
                        @if(user.did.equals(root.getDid()))
                            <div class="tag">OP</div>
                        @endif
                        @if(user.isAdmin)
                            <div class="tag">Admin</div>
                        @endif
                    </div>
                    <a class="handle" href="/pfp/${user.did}">@${user.handle}</a>
                </div>
                <div class="time-info">
                    ${TimeUtil.calculateTimeText(reply.getCreatedAt())}
                </div>
                <div id="reply-popup-button-container-${self.getRecordKey()}" class="popup-menu-container">
                    <button
                        id="reply-popup-${self.getRecordKey()}-button"
                        class="button icon"
                        onclick="togglePopup('reply-popup-${self.getRecordKey()}')"
                    >
                        <img src="/more.svg" alt="More Options" width="20" />
                    </button>
                    @template.pages.post.htmx.replyPopupMenu(
                        id = "reply-popup-" + self.getRecordKey(),
                        post = root,
                        reply = self,
                        ownsThisReply = user.isSelf,
                        isCurUserAdmin = loggedInUser != null ? loggedInUser.isAdmin : false,
                        isHidden = isHidden
                    )
                </div>
            </div>
            <div class="body">
                <p>
                    $unsafe{MarkdownUtil.render(reply.getContent().toString())}
                </p>

                <p
                    id="reply-${self.getRecordKey()}-hide-text"
                    style="display: ${isHidden ? "block" : "none"}; color: var(--subtext-color);"
                >
                    <i>This reply is hidden</i>
                </p>
            </div>
        </div>

        @if(paginationTrigger)
            <div
                class="runner"
                hx-get="/post/${root.getDid()}/${root.getRecordKey()}/replies?page=${nextPage}"
                hx-trigger="intersect once"
                hx-target="#new-reply-insert-point"
                hx-swap="outerHTML"
            ></div>
        @else
            <div class="runner"></div>
        @endif
    </div>

    @if(selfHighlight)
        <script>
            Prism.highlightAllUnder(document.getElementById('reply-${self == null ? "new" : self.getRecordKey()}'))
        </script>
    @endif
</div>
