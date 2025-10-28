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
        <a href="" class="">
            @if(avatarUrl != null && !avatarUrl.trim().isEmpty())
                <img src="${avatarUrl}" alt="Profile Avatar" style="width: 32px; height: 32px; border-radius: 50%;">
            @else
                <img src="/user.svg" alt="Default Profile Avatar" style="width: 32px; height: 32px; border-radius: 50%;">
            @endif
        </a>
    </div>
</div>