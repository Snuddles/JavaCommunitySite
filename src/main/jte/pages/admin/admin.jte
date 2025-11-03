@param String currentUserAvatarUrl = null

@template.layout.main(
    title = "Admin",
    content = @`
    @template.components.pageHeader(
        content = @`<h2>Admin Controls</h2>`,
        avatarUrl = currentUserAvatarUrl
    )

    `
)