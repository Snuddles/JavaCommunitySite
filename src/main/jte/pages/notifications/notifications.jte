@import com.jcs.javacommunitysite.util.UserInfo
@import java.util.List
@import java.util.ArrayList
@import java.util.Map

@param UserInfo user
@param List<Map<String, Object>> notifications = new ArrayList()

@template.layout.main(
    title = "Notifications",
    content = @`
    <div class="page-content notifications">

        @template.components.pageHeader(
            content = @`<h2>Notifications</h2>`,
            user = user
        )

        @for(Map<String, Object> notification : notifications)
            @template.pages.notifications.components.notification(
                id = notification.get("id").toString(),
                title = "New Reply",
                body = notification.get("text").toString(),
                actionText = "Go to post",
                actionHref = notification.get("postLink") != null ? notification.get("postLink").toString() : "#",
                isRead = (Boolean) notification.get("isRead")
            )
        @endfor

        @if(notifications.isEmpty())
            <p style="align-self: center; color: var(--subtext-color); padding: 40px;">
                <i>No notifications!</i>
            </p>
        @endif

    </div>
    `
)