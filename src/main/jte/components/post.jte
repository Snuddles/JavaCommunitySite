@param com.jcs.javacommunitysite.atproto.AtUri aturi
@param String title
@param String content
@param int amtReplies

<li class="post-item">
    <a href="/post/${aturi.getDid()}/${aturi.getRecordKey()}">
        <div class="post-name-container">
            <h4 class="post-name">${title}</h4>
        </div>
        <p class="post-preview">${content}</p>
        <div class="tags">
            <div class="tag">Springboot</div>
            <div class="tag">Java 21</div>
            <div class="tag">Thymeleaf</div>
        </div>
    </a>

    <div style="min-width: 150px; text-align: right">
        <p>2 days ago</p>
        <p style="color: var(--subtext-color)">${amtReplies} replies</p>
    </div>
</li>