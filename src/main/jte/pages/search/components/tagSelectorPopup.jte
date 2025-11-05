@import java.util.Map

@param String id
@param Map<String, String> tags = null

<div id="${id}" class="tag-selector-popup hidden preload">
    <p><b>Select Tags</b></p>

    <div class="tag-selector">
        @for(var tag : tags.values())
            <label class="tag-selector-item">
                <input type="checkbox" name="tags" value="${tag}" class="tag-button">
                <span>${tag}</span>
            </label>
        @endfor
    </div>

</div>
