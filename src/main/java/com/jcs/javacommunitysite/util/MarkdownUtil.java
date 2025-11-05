package com.jcs.javacommunitysite.util;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Heading;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class MarkdownUtil {
    public static String render(String markdown) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);

        document.accept(new AbstractVisitor() {
            @Override
            public void visit(Heading heading) {
                int newLevel = Math.min(heading.getLevel() + 2, 6);
                heading.setLevel(newLevel);
                visitChildren(heading);
            }
        });


        HtmlRenderer renderer = HtmlRenderer.builder().escapeHtml(true).build();
        String contentHtml = renderer.render(document);

        return contentHtml;
    }
}
