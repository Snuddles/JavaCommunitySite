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
            <a href="/pfp" class="">
                <img src="${avatarUrl}" alt="Profile Avatar" style="width: 32px; height: 32px; border-radius: 50%;">
            </a>
        @else
            <a href="/login" class="">
                <img src="/user.svg" alt="Default Profile Avatar" style="width: 32px; height: 32px; border-radius: 50%;">
            </a>
        @endif
    </div>
</div>