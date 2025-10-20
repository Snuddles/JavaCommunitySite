@param java.util.List<java.util.Map<String, Object>> group
@template.layout.main(title = "Browse", content = @`
    <div class="ml-4 mr-4 flex-1 p-6 min-h-svh">
        <div class="flex flex-row justify-between">
            <div class="flex flex-col w-90">
                <h1 class="flex text-3xl mt-2">Browse Categories.</h1>
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
        @if(group.isEmpty())
            <br/>
            <p>Make sure to log in.</p>
        @endif
        @for(java.util.Map<String, Object> a : group)
            !{Object b = a.get("group");}
            !{java.util.Map<?, ?> c = (java.util.Map<?, ?>) b;}
            !{String topic = c.get("name").toString();}
            <div class="flex-col border-1 border-black rounded-sm mt-8">
                <div class="border-b-1 border-b-black m-2 pb-1">
                    <a href="/topics/${topic.toLowerCase()}" class="text-gray-500 hover:underline">
                        ${topic}
                    </a>
                </div>
                <ul class="m-3">
                    !{Object d = a.get("categories");}
                    !{java.util.List<?> cats = (java.util.List<?>) d;}
                    @for(Object cat : cats)
                        !{java.util.Map<?,?> e = (java.util.Map<?, ?>) cat;}
                        !{String cat_name = e.get("name").toString();}
                        !{String cat_desc = e.get("description").toString();}
                        <li class="text-sm">
                            <a class="hover:underline" href="/topics/${topic}/${cat_name}">
<%--                                ${cat.toString()}--%>
                                <p>${cat_name}</p>
                                <p class="text-xs text-gray-400">${cat_desc}</p>
                            </a>
                        </li>
                    @endfor
                </ul>
            </div>
        @endfor
<%--        <div class="flex-col border-1 border-black rounded-sm mt-8">--%>
<%--            <div class="border-b-1 border-b-black m-2 pb-1">--%>
<%--                <a href="/topics/questions" class="text-gray-500 hover:underline">Questions</a>--%>
<%--            </div>--%>
<%--            <ul class="m-3">--%>
<%--                <li class="text-sm">--%>
<%--                    <a class="hover:underline" href="/topics/questions/getting_started">--%>
<%--                        <p>Getting Started</p>--%>
<%--                        <p class="text-xs text-gray-400">If you're brand new, start here!</p>--%>
<%--                    </a>--%>
<%--                </li>--%>
<%--                <li class="text-sm mt-3">--%>
<%--                    <a class="hover:underline" href="/topics/questions/springboot">--%>
<%--                        <p>Springboot Questions</p>--%>
<%--                        <p class="text-xs text-gray-400">Questions about Springboot</p>--%>
<%--                    </a>--%>
<%--                </li>--%>
<%--                <li class="text-sm mt-3">--%>
<%--                    <a class="hover:underline" href="/topics/questions/minecraft_modding_questions">--%>
<%--                        <p>Minecraft Modding Questions</p>--%>
<%--                        <p class="text-xs text-gray-400">Questions about writing mods for Minecraft</p>--%>
<%--                    </a>--%>
<%--                </li>--%>
<%--                <li class="text-sm mt-3">--%>
<%--                    <a class="hover:underline" href="/topics/questions/kotlin_questions">--%>
<%--                        <p>Kotlin Questions</p>--%>
<%--                        <p class="text-xs text-gray-400">Questions about Kotlin</p>--%>
<%--                    </a>--%>
<%--                </li>--%>
<%--            </ul>--%>
<%--        </div>--%>
<%--        <div class="flex-col border-1 border-black rounded-sm mt-8">--%>
<%--            <div class="border-b-1 border-b-black m-2 pb-1">--%>
<%--                <a href="/topics/showcase" class="text-gray-500 hover:underline">Showcase</a>--%>
<%--            </div>--%>
<%--            <ul class="m-3">--%>
<%--                <li class="text-sm">--%>
<%--                    <a class="hover:underline" href="/topics/showcase/applications">--%>
<%--                        <p>Applications</p>--%>
<%--                        <p class="text-xs text-gray-400">Show off your cool applications</p>--%>
<%--                    </a>--%>
<%--                </li>--%>
<%--                <li class="text-sm mt-3">--%>
<%--                    <a class="hover:underline" href="/topics/showcase/libraries">--%>
<%--                        <p>Libraries</p>--%>
<%--                        <p class="text-xs text-gray-400">Show off your cool libraries</p>--%>
<%--                    </a>--%>
<%--                </li>--%>
<%--                <li class="text-sm mt-3">--%>
<%--                    <a class="hover:underline" href="/topics/showcase/minecraft_mods">--%>
<%--                        <p>Minecraft Mods</p>--%>
<%--                        <p class="text-xs text-gray-400">Show off your cool Minecraft mods</p>--%>
<%--                    </a>--%>
<%--                </li>--%>
<%--            </ul>--%>
<%--        </div>--%>
<%--        <div class="flex-col border-1 border-black rounded-sm mt-8">--%>
<%--            <div class="border-b-1 border-b-black m-2 pb-1">--%>
<%--                <a href="" class="text-gray-500 hover:underline">Lorem Ipsum</a>--%>
<%--            </div>--%>
<%--            <ul class="m-3">--%>
<%--                <li class="text-sm">--%>
<%--                    <a class="hover:underline" href="">--%>
<%--                        <p>Lorem Ipsum</p>--%>
<%--                        <p class="text-xs text-gray-400">Lorem Ipsum dolor filler text</p>--%>
<%--                    </a>--%>
<%--                </li>--%>
<%--                <li class="text-sm mt-3">--%>
<%--                    <a class="hover:underline" href="">--%>
<%--                        <p>Lorem Ipsum</p>--%>
<%--                        <p class="text-xs text-gray-400">Lorem Ipsum dolor filler text</p>--%>
<%--                    </a>--%>
<%--                </li>--%>
<%--                <li class="text-sm mt-3">--%>
<%--                    <a class="hover:underline" href="">--%>
<%--                        <p>Lorem Ipsum</p>--%>
<%--                        <p class="text-xs text-gray-400">Lorem Ipsum dolor filler text</p>--%>
<%--                    </a>--%>
<%--                </li>--%>
<%--                <li class="text-sm mt-3">--%>
<%--                    <a class="hover:underline" href="">--%>
<%--                        <p>Lorem Ipsum</p>--%>
<%--                        <p class="text-xs text-gray-400">Lorem Ipsum dolor filler text</p>--%>
<%--                    </a>--%>
<%--                </li>--%>
<%--            </ul>--%>
<%--        </div>--%>
    </div>
`)