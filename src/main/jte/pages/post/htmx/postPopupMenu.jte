@import com.jcs.javacommunitysite.atproto.AtUri

@param AtUri aturi
@param boolean ownsThisPost

<div id="popup-menu" class="popup-menu">
    @if(ownsThisPost)
        <button
            class="popup-option"
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
    @else
        <p>Sorry, nothing you can do here!</p>
    @endif
</div>