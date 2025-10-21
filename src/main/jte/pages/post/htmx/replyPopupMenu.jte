@import com.jcs.javacommunitysite.atproto.AtUri

@param AtUri post
@param AtUri reply
@param boolean ownsThisReply

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
    @else
        <p>Sorry, nothing you can do here!</p>
    @endif
</div>