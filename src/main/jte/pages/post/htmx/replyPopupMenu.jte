@import com.jcs.javacommunitysite.atproto.AtUri

@param AtUri post
@param AtUri reply
@param boolean ownsThisReply
@param boolean isAdmin = false
@param boolean isHidden = false

<div id="popup-menu" class="popup-menu">
    @if(ownsThisReply)
        <button
            class="popup-option"
            hx-get="/post/${post.getDid()}/${post.getRecordKey()}/htmx/openEditReply?reply=${reply.toString()}"
            hx-target="#reply-${reply.getRecordKey()}"
            hx-swap="outerHTML"
        >
            Edit
        </button>
        <button
            class="popup-option"
            hx-get="/post/${post.getDid()}/${post.getRecordKey()}/htmx/confirmDeleteReply?reply=${reply.toString()}"
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
                hx-get="/post/${aturi.getDid()}/${aturi.getRecordKey()}/htmx/unhideReply?reply=${reply.toString()}"
                hx-target="body"
                hx-swap="beforeend"
            >
                Unhide
            </button>
        @else
            <button
                class="popup-option"
                hx-get="/post/${aturi.getDid()}/${aturi.getRecordKey()}/htmx/hideReply?reply=${reply.toString()}"
                hx-target="body"
                hx-swap="beforeend"
            >
                Hide
            </button>
        @endif
    @endif

    @if(!ownsThisReply && !isAdmin)
        <p>Sorry, nothing you can do here!</p>
    @endif
</div>