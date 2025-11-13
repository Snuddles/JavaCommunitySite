@import com.jcs.javacommunitysite.util.TimeUtil
@import java.time.OffsetDateTime

@param String id
@param String title
@param String body
@param String actionText = null
@param String actionHref = null
@param boolean isRead = true
@param String createdAt

<div id="notification-${id}" class="notification${!isRead ? " unread" : ""}">
    <img src="/mail.svg" alt="Notification" />
    <div class="notif-content">
        <div class="title-container">
            <h5>${title}</h5>
            <p>&#x2022;</p>
            <p>${TimeUtil.calculateTimeText(OffsetDateTime.parse(createdAt))}</p>
        </div>
        <p>${body}</p>
    </div>
    <div class="action-buttons">
        @if(actionText != null)
            <a class="button" href="${actionHref}">${actionText}</a>
        @endif
        <button
            hx-delete="/notifications/delete?id=${id}"
            hx-target="#notification-${id}"
            hx-swap="outerHTML"
            class="button icon"
        >
            <img src="/delete.svg" alt="Delete notification" />
        </button>
    </div>
</div>
