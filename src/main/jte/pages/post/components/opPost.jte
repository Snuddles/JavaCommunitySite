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
        </div>
        <div class="body">
            $unsafe{content}
        </div>
    </div>
</div>