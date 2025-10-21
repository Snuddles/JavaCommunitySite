@param com.jcs.javacommunitysite.atproto.AtUri post
@param com.jcs.javacommunitysite.atproto.AtUri reply

@template.components.modal(
    title="Delete this reply?",
    content=@`
    <p>Are you sure you want to delete this reply? This action cannot be undone.</p>
    `,
    buttons=@`
    <img id="delete-reply-indicator" class="htmx-indicator" src="/throbber.svg" width="24" />
    <button class="button transparent" onclick="closeModal()">Cancel</button>
    <button
        class="button danger"
        hx-delete="/post/${post.getDid()}/${post.getRecordKey()}/htmx/deleteReply?reply=${reply.toString()}"
        hx-target="#reply-${reply.getRecordKey()}"
        hx-target-error="body"
        hx-swap="beforeend"
        hx-disabled-elt="this"
        hx-indicator="#delete-reply-indicator"
        hx-on:success="closeModal()"
    >
        Delete
    </button>
    `
)