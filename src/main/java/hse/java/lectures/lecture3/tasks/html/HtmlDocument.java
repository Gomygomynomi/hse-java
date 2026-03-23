package hse.java.lectures.lecture3.tasks.html;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

public class HtmlDocument {
    private static final Set<String> ALLOWED_TAGS = Set.of("html", "head", "body", "div", "p");

    public HtmlDocument(String filePath) {
        this(Path.of(filePath));
    }

    public HtmlDocument(Path filePath) {
        String content = readFile(filePath);
        validate(content);
    }

    private String readFile(Path filePath) {
        try {
            return Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + filePath, e);
        }
    }

    private void validate(String content) {
        Deque<String> stack = new ArrayDeque<>();
        boolean hasHtml = false;
        boolean hasHead = false;
        boolean hasBody = false;

        int i = 0;
        int n = content.length();

        while (i < n) {
            int open = content.indexOf('<', i);
            if (open == -1) break;

            int close = content.indexOf('>', open);
            if (close == -1) {
                throw new RuntimeException("Malformed HTML: missing '>'");
            }

            String tagContent = content.substring(open + 1, close);
            boolean isClosing = tagContent.startsWith("/");
            String tagName;

            if (isClosing) {
                tagName = tagContent.substring(1).trim().split("\\s+")[0].toLowerCase();
            } else {
                tagName = tagContent.trim().split("\\s+")[0].toLowerCase();
                if (tagName.endsWith("/")) {
                    tagName = tagName.substring(0, tagName.length() - 1);
                }
            }

            if (!ALLOWED_TAGS.contains(tagName)) {
                throw new UnsupportedTagException("Unsupported tag: " + tagName);
            }

            if (isClosing) {
                if (stack.isEmpty()) {
                    throw new UnexpectedClosingTagException("Unexpected closing tag: </" + tagName + ">");
                }
                String top = stack.pop();
                if (!top.equals(tagName)) {
                    throw new MismatchedClosingTagException("Expected </" + top + ">, but got </" + tagName + ">");
                }
                if (tagName.equals("html")) {
                    hasHtml = true;
                }
            } else {
                if (tagName.equals("html")) {
                    if (hasHtml) {
                        throw new InvalidStructureException("Multiple <html> tags");
                    }
                    hasHtml = true;
                } else if (tagName.equals("head")) {
                    if (hasHead) {
                        throw new InvalidStructureException("Multiple <head> tags");
                    }
                    if (hasBody) {
                        throw new InvalidStructureException("<head> after <body>");
                    }
                    hasHead = true;
                } else if (tagName.equals("body")) {
                    if (hasBody) {
                        throw new InvalidStructureException("Multiple <body> tags");
                    }
                    hasBody = true;
                }
                stack.push(tagName);
            }

            i = close + 1;
        }

        if (!stack.isEmpty()) {
            throw new UnclosedTagException("Unclosed tags: " + stack);
        }
        if (!hasHtml) {
            throw new InvalidStructureException("No <html> root tag");
        }
    }
}