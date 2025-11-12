@import com.jcs.javacommunitysite.atproto.AtUri
@import com.jcs.javacommunitysite.jooq.tables.records.PostRecord
@import com.jcs.javacommunitysite.util.MarkdownUtil
@import com.jcs.javacommunitysite.util.TimeUtil
@import com.jcs.javacommunitysite.util.UserInfo
@import dev.mccue.json.Json
@import static dev.mccue.json.JsonDecoder.array
@import static dev.mccue.json.JsonDecoder.string

@param PostRecord post
@param UserInfo user
@param boolean isHidden
@param boolean selfHighlight = false
@param UserInfo loggedInUser

!{AtUri aturi = new AtUri(post.getAturi());}
!{
    var tagsJson = Json.read(post.getTags().data());
    var tagsArray = array(string()).decode(tagsJson);
}

!{String displayName = !user.displayName.isBlank() ? user.displayName : user.handle;}


<div id="post" class="post op-post">
    <div class="content">
        <div class="header">
            <div class="profile-pic-container">
                <img src="${user.avatarUri}" alt="Profile Picture" />
            </div>
            <div class="poster-info">
                <div class="top-bar">
                    <p class="username">${displayName}</p>
                    @if(user.did.equals(post.getOwnerDid()))
                        <div class="tag">OP</div>
                    @endif
                    @if(user.isAdmin)
                        <div class="tag">Admin</div>
                    @endif
                </div>
                <a class="handle" href="/pfp/${user.did}">@${user.handle}</a>
            </div>
            <div class="time-info">
                ${TimeUtil.calculateTimeText(post.getCreatedAt())}
            </div>
            <div id="post-popup-button-container" class="popup-menu-container">
                <button
                    id="post-popup-button"
                    class="button icon"
                    onclick="togglePopup('post-popup')"
                >
                    <img src="/more.svg" alt="More Options" width="20" />
                </button>
                @template.pages.post.htmx.postPopupMenu(
                    id = "post-popup",
                    aturi = aturi,
                    ownsThisPost = user.isSelf,
                    isCurUserAdmin = loggedInUser != null ? loggedInUser.isAdmin : false,
                    isHidden = isHidden
                )
            </div>
        </div>
        <div class="body">
            $unsafe{MarkdownUtil.render(post.getContent())}
        </div>

        @if(tagsArray.size() > 0)
            <div class="tags">
                @for(var tag : tagsArray)
                    <span class="tag">${tag}</span>
                @endfor
            </div>
        @endif
    </div>

    @if(selfHighlight)
        <script>
            Prism.highlightAllUnder(document.getElementById("post"))
        </script>
    @endif

</div>
