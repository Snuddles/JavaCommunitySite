@import java.util.*;

@param List<String> replies = null
@param Map<String, Map<String, String>> replyData = null
@param Map<String, Map<String, String>> postData = null
@param List<String> tagsList = null

<div class="userAnswers">
    @for (String reply : replies)
        !{Map<String, String> post = postData.get(reply);}
        !{Map<String, String> r = replyData.get(reply);}

        <div class="userAnswer">
            <div class="otherPost">
                <h5 class="postTitle">${post.get("title")}</h5>
                <p class="postContent">${post.get("content")}</p>
            </div>
            <div class="userReply">
                <div class="arrow-right"></div>
                <p class="replyContent">${r.get("content")}</p>
            </div>
            @if(tagsList != null && !tagsList.isEmpty())
                <div class="tags">
                    @for(var tag : tagsList)
                        <div class="tag">${tag}</div>
                    @endfor
                </div>
            @endif
        </div>
    @endfor
</div>