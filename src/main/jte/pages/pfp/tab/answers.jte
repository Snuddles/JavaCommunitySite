@import java.util.*

@param List<String> replies
@param Map<String, Map<String, String>> replyData
@param Map<String, Map<String, String>> postData
@param boolean hasMoreUserAnswers = false
@param int nextPageUserAnswers = 2
@param String did

<div class="userAnswers">
    @for (String reply : replies)
        !{Map<String, String> post = postData.get(reply);}
        !{Map<String, String> r = replyData.get(reply);}

        <div class="userAnswer">
            <div class="otherPost">
                <h5 class="postTitle">${post != null ? post.get("title") : "(deleted post)"}</h5>
                <p class="postContent">${post != null ? post.get("content") : ""}</p>
            </div>
            <div class="userReply">
                <p class="replyContent">&#x21AA; ${r.get("content")}</p>
            </div>
        </div>
    @endfor

    @if(hasMoreUserAnswers)
        <div
            id="pfp-answers-insert-point"
            style="width: 0; height: 0;"
            hx-get="/pfp/tab/answers?page=${nextPageUserAnswers}&did=${did}"
            hx-trigger="intersect once"
            hx-target="#pfp-answers-insert-point"
            hx-swap="outerHTML"
        ></div>
    @endif
</div>
