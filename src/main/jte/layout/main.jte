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
    <link rel="stylesheet" href="/main.css" />
    <link rel="stylesheet" href="/root.css" />
    <script src="https://cdn.jsdelivr.net/npm/htmx.org@2.0.7/dist/htmx.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/htmx-ext-response-targets@2.0.2"></script>
</head>
<body class="flex flex-row bg-white" hx-ext="response-targets">
<div class="w-60 flex-initial p-4 flex-col border-r-solid border-r-1 border-r-black min-h-svh">
    <h1 class="flex text-5xl mt-2 mb-10 pl-2">jcs.</h1>
    <ul class="m-4 text-base">
        <li>
            <a class="block mt-1 p-2 rounded-md hover:bg-sky-500 hover:text-white" href="/browse">
                Browse
            </a>
        </li>
        <li>
            <a class="block mt-1 p-2 rounded-md hover:bg-sky-500 hover:text-white" href="/hotPosts">
                Hot Posts
            </a>
        </li>
        <li>
            <a class="block mt-1 p-2 rounded-md hover:bg-sky-500 hover:text-white" href="/newPosts">
                New Posts
            </a>
        </li>
        <li>
            <a class="block mt-1 p-2 rounded-md hover:bg-sky-500 hover:text-white" href="/newReplies">
                New Replies
            </a>
        </li>
    </ul>
    <div class="m-3 text-base mt-5">
        <p class="text-gray-500">Notifications</p>
        <ul class="m-2">
            <li class="text-sm">
                <a class="hover:underline" href="">
                    <p>New reply to your post</p>
                    <p class="text-xs text-gray-400">Lorem Ipsum filler text</p>
                </a>
            </li>
            <li class="text-sm mt-3">
                <a class="hover:underline" href="">
                    <p>New reaction to your post</p>
                    <p class="text-xs text-gray-400">Lorem Ipsum filler text</p>
                </a>
            </li>
            <li class="text-sm mt-3">
                <a class="hover:underline" href="">
                    <p>New mention on post</p>
                    <p class="text-xs text-gray-400">Lorem Ipsum filler text</p>
                </a>
            </li>
        </ul>
    </div>
    <div class="m-3 text-base mt-6">
        <p class="text-gray-500">Recent Browsing</p>
        <ul class="m-2">
            <li class="text-sm mt-3">
                <a class="hover:underline" href="">
                    How to set up Springb...
                </a>
            </li>
            <li class="text-sm mt-3">
                <a class="hover:underline" href="">
                    Check out my new too...
                </a>
            </li>
            <li class="text-sm mt-3">
                <a class="hover:underline" href="">
                    How to make a calculator
                </a>
            </li>
            <li class="text-sm mt-3">
                <a class="hover:underline" href="">
                    When did everyone sta...
                </a>
            </li>
        </ul>
    </div>
</div>
<%-- The Content for the right side of the page --%>
<div class="page-container">
    <div class="page">
        ${content}
    </div>
</div>
</body>
</html>