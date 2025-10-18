@import com.jcs.javacommunitysite.jooq.tables.records.ReplyRecord

@param ReplyRecord reply

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
        </div>
        <div class="body">
            <p>
                $unsafe{reply.getContent().toString()}
            </p>
        </div>
    </div>

    <div class="runner"></div>
</div>