@param String category
@param String topic

@template.layout.main(content = @`
    <div class="ml-4 mr-4 flex-1 p-6 min-h-svh">
        <div class="flex flex-row justify-between">
            <div class="flex flex-col w-200">
                <p class="text-sm text-gray-500 mt-1">${category} /</p>
                <h1 class="flex text-3xl mt-2">${topic}</h1>
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
                <a href="" class="text-gray-500 hover:underline">Posts</a>
            </div>
            <ul class="m-3">
                @for(int i = 0; i < topic.length(); i++)
                    <li class="text-sm m-2">
                        <a class="hover:underline" href="">
                            <p>Lorem Ipsum</p>
                            <p class="text-xs text-gray-400">Lorem Ipsum</p>
                        </a>
                    </li>
                @endfor
            </ul>
        </div>
    </div>
`)
