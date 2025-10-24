@param gg.jte.Content content
@param String title

<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>${title} | JCS</title>
    <link rel="stylesheet" href="/root.css" />
    <script src="https://cdn.jsdelivr.net/npm/htmx.org@2.0.7/dist/htmx.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/htmx-ext-response-targets@2.0.2"></script>
</head>
<body class="root" hx-ext="response-targets">
<div class="sidebar">

    <h1><a href="/">jcs.</a></h1>

    <%-- Nav buttons --%>
    <ul class="nav-buttons">
        <li>
            <a id="ask-nav-button" href="/ask">
                <img src="question.svg" alt="Ask" />
                Ask
            </a>
        </li>
        <li>
            <a id="answer-nav-button" href="/answer">
                <img src="answer.svg" alt="Answer" />
                Answer
            </a>
        </li>
        <li>
            <a id="search-nav-button" href="/search">
                <img src="search.svg" alt="Search" />
                Search
            </a>
        </li>

        <%-- Spacer --%>
        <div style="flex: 1"></div>

        <li>
            <a id="chat-nav-button" href="/chat">
                <img src="chat.svg" alt="Chat" />
                Chat
            </a>
        </li>
    </ul>
</div>

<%-- The Content for the right side of the page --%>
<div class="page-container">
    <div class="page">
        ${content}
    </div>
</div>
</body>
<script>
    document.addEventListener("click", () => {
        document.getElementById("popup-menu")?.remove()
    })

    // Nav button highlighting
    const path = window.location.pathname.split('/')[1];
    const buttons = {
        'ask': 'ask-nav-button',
        'answer': 'answer-nav-button',
        'search': 'search-nav-button',
        'chat': 'chat-nav-button',
    };

    if (path in buttons) {
        document.getElementById(buttons[path])?.classList.add('active');
    }

</script>
</html>