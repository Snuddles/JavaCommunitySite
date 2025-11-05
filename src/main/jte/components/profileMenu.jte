@param boolean isAdmin

<ul id="profileMenu" class="popup-menu">
    <li><a class="popup-option" href="/pfp">Profile</a></li>
    <li><a class="popup-option" href="/logout">Logout</a></li>
    @if(isAdmin)
        <li><a class="popup-option" href="/admin">Admin</a></li>
    @endif
</ul>
