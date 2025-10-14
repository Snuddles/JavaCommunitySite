package com.jcs.javacommunitysite.pages;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Node;
import org.commonmark.node.Heading;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PostController {
    @GetMapping("/post")
    public String post(Model model) {
        String exampleText = "# Test \n This is a test post \n ```java\nSystem.out.println(\"Hello World!\");\n``` \n ## Test 2 \n ### Test 3 \n - lol \n - [x] lol x2 \n #### Test 4 \n testinggggg";
        Parser parser = Parser.builder().build();
        Node document = parser.parse(exampleText);

        document.accept(new AbstractVisitor() {
            @Override
            public void visit(Heading heading) {
                int newLevel = Math.min(heading.getLevel() + 2, 6);
                heading.setLevel(newLevel);
                visitChildren(heading);
            }
        });


        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String html = renderer.render(document);

        model.addAttribute("post", html);

        return "post";
    }
}
