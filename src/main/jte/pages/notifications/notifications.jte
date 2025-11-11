@import com.jcs.javacommunitysite.util.UserInfo
@import java.util.List
@import java.util.ArrayList
@import java.util.Map

@param UserInfo user
@param List<Map<String, String>> notifications = new ArrayList()

@template.layout.main(
    title = "Notifications",
    content = @`
    <div class="page-content notifications">

        @template.components.pageHeader(
            content = @`<h2>Notifications</h2>`,
            user = user
        )

<%--        @template.pages.notifications.components.notification(--%>
<%--            id = "a",--%>
<%--            title = "You have a new reply",--%>
<%--            body = "Gregor replied to your post",--%>
<%--            actionText = "Go to post",--%>
<%--            actionHref = "post/somedid/somerkey",--%>
<%--        )--%>

        @if(notifications.isEmpty())
            <p style="align-self: center; color: var(--subtext-color); padding: 40px;">
                <i>No notifications!</i>
            </p>
        @endif

    </div>
    `
)