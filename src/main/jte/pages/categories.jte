@param String category

@template.layout.main(content = @`
    <div class="ml-4 mr-4 flex-1 p-6 min-h-svh">
        <div class="flex flex-row justify-between">
            <div class="flex flex-col w-90">
                <h1 class="flex text-3xl mt-2">${category}</h1>
                <p class="text-sm text-gray-500 mt-1">Lorem Ipsum.</p>
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
    </div>
`)
