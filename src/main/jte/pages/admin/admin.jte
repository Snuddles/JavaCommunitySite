@import com.jcs.javacommunitysite.atproto.AtUri
@import com.jcs.javacommunitysite.pages.admin.AdminPageController
@import com.jcs.javacommunitysite.util.UserInfo
@import java.util.List
@import java.util.Map
@import com.jcs.javacommunitysite.pages.admin.AdminPageController.AdminUser

@param Map<String, String> myTags
@param Map<String, String> othersTags
@param UserInfo user = null
@param List<AdminUser> admins

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
            <h3>Manage Admins</h3>
            <br/>
            <div class="contained-section">
                <form
                    class="admin-user-form"
                    hx-get="/admin/user-search"
                    hx-target="#user-search-result"
                    hx-swap="innerHTML"
                >
                    <h4>Search Users</h4>
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
            <h3>Admin List</h3>
            <div class="contained-section">
                @for(AdminUser admin : admins)
                    <div class="card">
                        <div class="card-left">
                            @if(admin.displayName().isEmpty())
                                <h6>${admin.handle()}</h6>
                            @else
                                <h6>${admin.displayName()}</h6>
                            @endif
                            <p>${admin.handle()}</p>
                        </div>
                        <div class="card-right">
                            @if(admin.roleName().equals("admin"))
                                <div class="admin-role">
                                    <form
                                        hx-post="/admin/revoke"
                                        hx-vals='{"targetDid":"${admin.did()}"}'
                                        hx-target=".admin-role"
                                        hx-swap="innerHTML"
                                    >
                                        <button class="button btn-danger" type="submit">Remove Admin</button>
                                    </form>
                                </div>
                            @else
                               <p>Superadmin — Cannot modify</p>
                            @endif
                        </div>
                    </div>
                @endfor
            </div>
        <div>

        </div>

<%--        <div>--%>
<%--            <h3>Audit Log</h3>--%>
<%--        </div>--%>
    </div>
`
)