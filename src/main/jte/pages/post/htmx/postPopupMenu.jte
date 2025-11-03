@import com.jcs.javacommunitysite.atproto.AtUri

@param AtUri aturi
@param boolean ownsThisPost
@param boolean isAdmin = false
@param boolean isHidden = false

<div id="popup-menu" class="popup-menu">
    @if(ownsThisPost)
        <button
            class="popup-option"
            hx-get="/post/${aturi.getDid()}/${aturi.getRecordKey()}/htmx/openEditPost"
            hx-target="#post"
            hx-swap="outerHTML"
        >
            Edit
        </button>
        <button
            class="popup-option"
            hx-get="/post/${aturi.getDid()}/${aturi.getRecordKey()}/htmx/confirmDeletePost"
            hx-target="body"
            hx-swap="beforeend"
        >
            Delete
        </button>
    @endif
    @if(isAdmin)
        @if(isHidden)
            <button
                class="popup-option"
                hx-get="/post/${aturi.getDid()}/${aturi.getRecordKey()}/htmx/unhidePost"
                hx-target="body"
                hx-swap="beforeend"
            >
                Unhide
            </button>
        @else
            <button
                class="popup-option"
                hx-get="/post/${aturi.getDid()}/${aturi.getRecordKey()}/htmx/hidePost"
                hx-target="body"
                hx-swap="beforeend"
            >
                Hide
            </button>
        @endif
    @endif

    @if(!ownsThisPost && !isAdmin)
        <p>Sorry, nothing you can do here!</p>
    @endif
</div>