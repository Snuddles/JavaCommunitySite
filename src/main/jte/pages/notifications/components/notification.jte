@param String id
@param String title
@param String body
@param String actionText = null
@param String actionHref = null

<div id="notification-${id}" class="notification">
    <img src="/mail.svg" alt="Notification" />
    <div class="notif-content">
        <h5>${title}</h5>
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
