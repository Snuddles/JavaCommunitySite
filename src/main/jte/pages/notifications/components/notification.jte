@param String id
@param String title
@param String body
@param String actionText = null
@param String actionHref = null
@param boolean isRead = true

<div id="notification-${id}" class="notification${!isRead ? " unread" : ""}">
    @if(!isRead)
        <div class="unread-indicator"></div>
    @endif
    <img src="/mail.svg" alt="Notification" />
    <div class="notif-content">
        <h5>${title}</h5>
        <p>${body}</p>
    </div>
    <div class="action-buttons">
        @if(actionText != null)
            @if(!isRead)
                <a class="button" 
                   href="${actionHref}"
                   hx-post="/notifications/markread?id=${id}"
                >${actionText}</a>
            @else
                <a class="button" href="${actionHref}">${actionText}</a>
            @endif
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
