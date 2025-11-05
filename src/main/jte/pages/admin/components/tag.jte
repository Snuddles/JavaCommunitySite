@param com.jcs.javacommunitysite.atproto.AtUri atUri
@param String name
@param boolean isOwnTag

<div id="tag-${atUri.getRecordKey()}" class="admin-tag">
    <span>${name}</span>
    @if(isOwnTag)
        <button
            class="button icon"
            hx-post="/admin/htmx/deleteTag?tag=${atUri.toString()}"
            hx-target="#tag-${atUri.getRecordKey()}"
            hx-swap="outerHTML"
        >
            <img src="/close.svg" alt="Delete" />
        </button>
    @endif
</div>