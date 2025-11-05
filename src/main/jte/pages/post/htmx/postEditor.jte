@import com.jcs.javacommunitysite.atproto.AtUri
@import com.jcs.javacommunitysite.forms.UpdatePostForm
@import com.jcs.javacommunitysite.jooq.tables.records.PostRecord
@import com.jcs.javacommunitysite.util.UserInfo
@import dev.mccue.json.Json
@import java.util.List
@import static dev.mccue.json.JsonDecoder.array
@import static dev.mccue.json.JsonDecoder.string

@param PostRecord post
@param UpdatePostForm form
@param UserInfo user
@param List<String> allTags

!{AtUri aturi = new AtUri(post.getAturi());}
!{
    var tagsJson = Json.read(post.getTags().data());
    var tagsArray = array(string()).decode(tagsJson);
}


<div id="post" class="post op-post">
    <div class="content">
        <div class="header">
            <div class="profile-pic-container">
                <img src="${user.avatarUri}" alt="Profile Picture" />
            </div>
            <div style="display: flex; align-items: center;">
                <p><b>Edit your Question</b></p>
            </div>
        </div>
        <form
            class="body"
            hx-put="/post/${aturi.getDid()}/${aturi.getRecordKey()}/htmx/editPost"
            hx-target="#post"
            hx-swap="outerHTML"
        >
            @template.components.postEditor(content=form.getContent())

            <div class="tag-selector">
                @for(String tag : allTags)
                    <label class="tag-selector-item">
                        <input type="checkbox" name="tags" value="${tag}" checked="${tagsArray.contains(tag)}" class="tag-button">
                        <span>${tag}</span>
                    </label>
                @endfor
            </div>

            <div class="buttons">
                <button
                        class="button transparent"
                        type="button"
                        hx-get="/post/${aturi.getDid()}/${aturi.getRecordKey()}/htmx/cancelEditPost"
                        hx-target="#post"
                        hx-swap="outerHTML"
                >
                    Cancel
                </button>
                <button class="button" type="submit">Save</button>
            </div>
        </form>
    </div>
</div>
