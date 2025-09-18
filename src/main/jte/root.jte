@param gg.jte.Content content
@param String title
@param java.util.List<String> stylesheets

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>${title}</title>
    <link rel="stylesheet" href="/root.css" />
    @for(String stylesheet : stylesheets)
        <link rel="stylesheet" href="${stylesheet}" />
    @endfor
</head>
<body>
    ${content}
</body>
</html>