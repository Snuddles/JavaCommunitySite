@import com.jcs.javacommunitysite.atproto.AtUri
@import com.jcs.javacommunitysite.forms.NewReplyForm
@import com.jcs.javacommunitysite.jooq.tables.records.ReplyRecord

@param ReplyRecord reply
@param NewReplyForm form

!{AtUri root = new AtUri(reply.getRoot());}
!{AtUri self = new AtUri(reply.getAturi());}

<div id="reply-${self.getRecordKey()}">
    <div class="mid-reply-runner"></div>
    <div class="post">
        <div class="content">
            <div class="header">
                <div class="profile-pic-container">
                    <img src="/img/user.svg">
                </div>
                <div style="display: flex; align-items: center;">
                    <p><b>Edit your Reply</b></p>
                </div>
            </div>
            <form
                class="body"
                hx-put="/post/${root.getRecordKey()}/${root.getRecordKey()}/htmx/editReply?reply=${self.toString()}"
                hx-target="#reply-${self.getRecordKey()}"
                hx-swap="outerHTML"
            >
                <textarea id="reply-edit-textarea" class="post-textarea" name="content" required>${form.getContent()}</textarea>
                <div class="buttons">
                    <button
                        class="button transparent"
                        type="button"
                        hx-get="/post/${root.getRecordKey()}/${root.getRecordKey()}/htmx/cancelEditReply?reply=${self.toString()}"
                        hx-target="#reply-${self.getRecordKey()}"
                        hx-swap="outerHTML"
                    >
                        Cancel
                    </button>
                    <button class="button" type="submit">Save</button>
                </div>
            </form>
        </div>

        <div class="runner"></div>
    </div>
</div>