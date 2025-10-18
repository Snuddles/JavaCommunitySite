@import com.jcs.javacommunitysite.forms.NewPostForm
@import java.util.List
@import java.util.Map

@param NewPostForm postForm
@param List<Map<String, String>> groups

@template.layout.main(
    title = "New Post",
    content = @`
    <div class="new-post-page">
        <h1>New Post</h1>

        <form method="post">
            <div class="form-group">
                <div class="category-select-container">
                    <div class="select-group-container">
                        <label for="group-input">Group</label>
                        <select id="group-input" class="selectbox" name="group" required hx-get="/newPost/htmx/getCategories" hx-target="#category-input">
                            <option value="" disabled selected>Select a Group</option>
                            @for(Map<String, String> group : groups)
                                <option value="${group.get("id")}">${group.get("name")}</option>
                            @endfor
                        </select>
                    </div>

                    <div class="select-group-container">
                        <label for="category-input">Category</label>
                        <select id="category-input" class="selectbox" name="category" required>
                            <option value="" disabled selected>Select a Category</option>
                        </select>
                    </div>
                </div>
            </div>

            <div class="form-group">
                <label for="title-input">Title</label>
                <input id="title-input" class="textbox" type="text" name="title" value="${postForm.getTitle()}" placeholder="My Really Good Title" required>
            </div>

            <div class="form-group">
                <label for="post-input">Post</label>
                <textarea id="post-input" class="post-textarea" name="content" name="content" value="${postForm.getContent()}" required></textarea>
            </div>

            <div class="right-side-buttons-container">
                <button class="button" type="submit">Create Post</button>
            </div>
        </form>
    </div>
    `
)
