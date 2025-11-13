@import com.jcs.javacommunitysite.atproto.AtUri
@import com.jcs.javacommunitysite.util.UserInfo
@import java.util.Map

@param Map<String, String> myTags
@param Map<String, String> othersTags
@param UserInfo user = null

@template.layout.main(
    title = "Admin",
    content = @`
    <div class="page-content">
        @template.components.pageHeader(
            content = @`<h2>Admin Controls</h2>`,
            user = user,
        )

        <div class="admin-tags-container">
            <h3>Manage Tags</h3>

            <div class="contained-section">
                <div class="sub-container">
                    <h4>Your Tags</h4>
                    <div id="my-tags" class="tags">
                        @for(Map.Entry<String, String> tag : myTags.entrySet())
                            @template.pages.admin.components.tag(
                                atUri = new AtUri(tag.getKey()),
                                name = tag.getValue(),
                                isOwnTag = true,
                            )
                        @endfor
                        @if(myTags.size() == 0)
                            <p style="color: var(--subtext-color)">
                                <i>No tags!</i>
                            </p>
                        @endif
                    </div>
                </div>

                <div class="sub-container">
                    <h5>Create a Tag</h5>
                    <form
                            class="create-tag-form"
                            hx-post="/admin/htmx/createTag"
                            hx-target="#my-tags"
                            hx-swap="beforeend"
                            hx-on::after-request="if (event.detail.successful && event.target == this) this.reset()"
                    >
                        <input class="textbox" type="text" placeholder="Tag Name" name="name">
                        <button class="button">Create</button>
                    </form>
                </div>
            </div>

            <div class="contained-section">
                <div class="sub-container">
                    <h4>Others' Tags</h4>
                    <div id="others-tags" class="tags">
                        @for(Map.Entry<String, String> tag : othersTags.entrySet())
                            @template.pages.admin.components.tag(
                                atUri = new AtUri(tag.getKey()),
                                name = tag.getValue(),
                                isOwnTag = false,
                            )
                        @endfor
                        @if(othersTags.size() == 0)
                            <p style="color: var(--subtext-color)">
                                <i>No tags!</i>
                            </p>
                        @endif
                    </div>
                </div>
            </div>

        </div>

        <div>
            <h3>Admin Users</h3>
            <br/>
            <div class="contained-section">
                <form
                    class="admin-user-form"
                    hx-get="/admin/user-search"
                    hx-target="#user-search-result"
                    hx-swap="innerHTML"
                >
                    <h4>Manage Admins</h4>
                    <div class="admin-search">
                        <input
                            class="textbox"
                            type="text"
                            name="handle"
                            placeholder="Search user by handle…"
                            autocomplete="off"
                            hx-trigger="keyup changed delay:300ms, submit"
                        />
                        <button class="button btn-primary" type="submit">Search</button>
                    </div>
                    <!-- optional spinner -->
                    <span class="spinner htmx-indicator"></span>
                </form>

                <div id="user-search-result" class="mt"></div>
            </div>
        </div>

<%--        <div>--%>
<%--            <h3>Audit Log</h3>--%>
<%--        </div>--%>
    </div>
`
)