@template.layout.main(content = @`
    <div class="ml-4 mr-4 flex-1 p-6 min-h-svh">
        <div class="flex flex-row justify-between">
            <div class="flex flex-col w-90">
                <h1 class="flex text-3xl mt-2">New Posts.</h1>
                <p class="text-sm text-gray-500 mt-1">Find and create posts in their appropriate categories.</p>
            </div>
            <div class="flex flex-row justify-between w-35 items-start">
                <a href="" class="rounded-md pl-2 pr-2 pt-1 pb-1 text-sm bg-sky-500 hover:bg-sky-600 text-white">
                    + New Post
                </a>
                <a href="" class="rounded-4xl bg-gray-500 p-1">
                    pfp
                </a>
            </div>
        </div>
        <div class="flex-col border-1 border-black rounded-sm mt-8">
            <div class="border-b-1 border-b-black m-2 pb-1">
                <a href="" class="text-gray-500 hover:underline">Questions</a>
            </div>
            <ul class="m-3">
                <li class="text-sm">
                    <a class="hover:underline" href="">
                        <p>Getting Started</p>
                        <p class="text-xs text-gray-400">If you're brand new, start here!</p>
                    </a>
                </li>
                <li class="text-sm mt-3">
                    <a class="hover:underline" href="/browse/questions">
                        <p>Springboot Questions</p>
                        <p class="text-xs text-gray-400">Questions about Springboot</p>
                    </a>
                </li>
                <li class="text-sm mt-3">
                    <a class="hover:underline" href="">
                        <p>Minecraft Modding Questions</p>
                        <p class="text-xs text-gray-400">Questions about writing mods for Minecraft</p>
                    </a>
                </li>
                <li class="text-sm mt-3">
                    <a class="hover:underline" href="">
                        <p>Kotlin Questions</p>
                        <p class="text-xs text-gray-400">Questions about Kotlin</p>
                    </a>
                </li>
            </ul>
        </div>
        <div class="flex-col border-1 border-black rounded-sm mt-8">
            <div class="border-b-1 border-b-black m-2 pb-1">
                <a href="" class="text-gray-500 hover:underline">Showcase</a>
            </div>
            <ul class="m-3">
                <li class="text-sm">
                    <a class="hover:underline" href="">
                        <p>Applications</p>
                        <p class="text-xs text-gray-400">Show off your cool applications</p>
                    </a>
                </li>
                <li class="text-sm mt-3">
                    <a class="hover:underline" href="">
                        <p>Libraries</p>
                        <p class="text-xs text-gray-400">Show off your cool libraries</p>
                    </a>
                </li>
                <li class="text-sm mt-3">
                    <a class="hover:underline" href="">
                        <p>Minecraft Mods</p>
                        <p class="text-xs text-gray-400">Show off your cool Minecraft mods</p>
                    </a>
                </li>
            </ul>
        </div>
        <div class="flex-col border-1 border-black rounded-sm mt-8">
            <div class="border-b-1 border-b-black m-2 pb-1">
                <a href="" class="text-gray-500 hover:underline">Lorem Ipsum</a>
            </div>
            <ul class="m-3">
                <li class="text-sm">
                    <a class="hover:underline" href="">
                        <p>Lorem Ipsum</p>
                        <p class="text-xs text-gray-400">Lorem Ipsum dolor filler text</p>
                    </a>
                </li>
                <li class="text-sm mt-3">
                    <a class="hover:underline" href="">
                        <p>Lorem Ipsum</p>
                        <p class="text-xs text-gray-400">Lorem Ipsum dolor filler text</p>
                    </a>
                </li>
                <li class="text-sm mt-3">
                    <a class="hover:underline" href="">
                        <p>Lorem Ipsum</p>
                        <p class="text-xs text-gray-400">Lorem Ipsum dolor filler text</p>
                    </a>
                </li>
                <li class="text-sm mt-3">
                    <a class="hover:underline" href="">
                        <p>Lorem Ipsum</p>
                        <p class="text-xs text-gray-400">Lorem Ipsum dolor filler text</p>
                    </a>
                </li>
            </ul>
        </div>
    </div>
`)