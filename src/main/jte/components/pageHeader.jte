@param gg.jte.Content content
@param String backHref = null
@param String avatarUrl = null

<div class="page-header">
    <div class="left-side">
        @if(backHref != null)
            <a class="button icon" href="${backHref}">&larr;</a>
        @endif
        <div>
            ${content}
        </div>
    </div>
    <div class="top-right-buttons">
        @if(avatarUrl != null && !avatarUrl.trim().isEmpty())
<%--            <a href="/pfp" class="">--%>
<%--                <img src="${avatarUrl}" alt="Profile Avatar" style="width: 32px; height: 32px; border-radius: 50%;">--%>
<%--            </a>--%>
            <div class="pfp-container" hx-on:click="event.stopPropagation()">
                <img src="${avatarUrl}" alt="Profile" class="pfp-img"
                     hx-get="/profileMenu"
                     hx-target="#profileMenuSlot"
                     hx-swap="innerHTML"
                     hx-trigger="click"
                    style="width: 32px; height: 32px; border-radius: 50%;"/>
                <div id="profileMenuSlot" hx-on:click="event.stopPropagation()"></div>
            </div>
        @else
            <a class="button" href="/login">Log in</a>
        @endif
    </div>
    <script>
        // close when clicking anywhere outside the avatar/menu
        document.addEventListener('click', () => {
          const slot = document.getElementById('profileMenuSlot');
          if (slot) slot.innerHTML = '';   // clear menu
        });
    </script>
</div>