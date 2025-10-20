@param com.jcs.javacommunitysite.atproto.AtUri aturi

@template.components.modal(
    title="Delete this post?",
    content=@`
    <p>Are you sure you want to delete this post? This action cannot be undone.</p>
    `,
    buttons=@`
    <button class="button transparent" onclick="closeModal()">Cancel</button>
    <button
        class="button danger"
        hx-delete="/post/${aturi.getDid()}/${aturi.getRecordKey()}/htmx/deletePost"
        hx-target-error="body"
        hx-swap="beforeend"
    >
        Delete
    </button>
    `
)