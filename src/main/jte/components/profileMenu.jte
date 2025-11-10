@import com.jcs.javacommunitysite.util.UserInfo
@param UserInfo user

!{String displayName = !user.displayName.isBlank() ? user.displayName : user.handle;}

<ul id="profileMenu" class="popup-menu hidden preload">
    <div class="user-info">
        <span class="display-name">${displayName}</span>
        <span class="sub-text">@${user.handle}</span>
    </div>
    <li><a class="popup-option" href="/pfp">
        <img src="/user.svg" alt="" />
        Profile
    </a></li>
    <li><a class="popup-option" href="/logout">
        <img src="/logout.svg" alt="" />
        Logout
    </a></li>
    @if(user.isAdmin)
        <li><a class="popup-option" href="/admin">
            <img src="/shield.svg" alt="" />
            Admin
        </a></li>
    @endif
</ul>
