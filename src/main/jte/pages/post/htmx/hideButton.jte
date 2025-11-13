@import com.jcs.javacommunitysite.atproto.AtUri

@param AtUri post
@param AtUri reply = null
@param boolean isHidden

!{var replyRkey = reply != null ? reply.getRecordKey() : null;}

!{
    String baseUrl = "/post/" + post.getDid() + "/" + post.getRecordKey() + "/htmx/";
    String target = "";
    String id = "";
    if (reply != null) {
        target = "Reply?reply=" + reply.toString();
        id = "reply-popup-" + reply.getRecordKey();
    } else {
        target = "Post";
        id = "post-popup";
    }
}

@if(isHidden)
    <button
        class="popup-option"
        hx-post="${baseUrl}unhide${target}"
        hx-target="this"
        hx-swap="outerHTML"
        hx-on::before-request="togglePopup('${id}'); toggleHidden('${replyRkey}', ${reply == null})"
    >
        <img src="/visible.svg" alt="" />
        Unhide
    </button>
@else
    <button
        class="popup-option"
        hx-post="${baseUrl}hide${target}"
        hx-target="this"
        hx-swap="outerHTML"
        hx-on::before-request="togglePopup('${id}'); toggleHidden('${replyRkey}', ${reply == null})"
    >
        <img src="/hidden.svg" alt="" />
        Hide
    </button>
@endif
