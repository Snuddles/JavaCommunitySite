@param gg.jte.Content content
@param String backHref = null

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
            pfp
        </a>
    </div>
</div>