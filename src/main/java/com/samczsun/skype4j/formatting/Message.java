package com.samczsun.skype4j.formatting;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class Message {
    private final ArrayList<Text> components = new ArrayList<>();

    private Message() {
    }

    public static Message create() {
        return new Message();
    }

    public Message with(Text text) {
        this.components.add(text);
        return this;
    }

    public List<Text> children() {
        return Collections.unmodifiableList(components);
    }

    public Text child(int x) {
        return components.get(x);
    }

    public String write() {
        StringBuilder result = new StringBuilder();
        for (Text t : components) {
            result.append(t);
        }
        return result.toString();
    }

    public String asHtml() {
        return this.write();
    }

    public String toString() {
        return this.write();
    }

    public String asPlaintext() {
        return Jsoup.parse(write()).text();
    }

    public static Message fromHtml(String text) {
        final Message parsed = create();
        Document doc = Jsoup.parse(text);
        doc.getElementsByTag("body").get(0).traverse(new NodeVisitor() {
            Stack<RichText> stack = new Stack<>();

            @Override
            public void head(Node node, int depth) {
                if (!node.nodeName().equals("body")) {
                    if (depth != 1) {
                        if (node.nodeName().equals("b")) {
                            RichText newText = Text.rich().withBold();
                            stack.peek().with(newText);
                            stack.push(newText);
                        } else if (node.nodeName().equals("i")) {
                            RichText newText = Text.rich().withItalic();
                            stack.peek().with(newText);
                            stack.push(newText);
                        } else if (node.nodeName().equals("s")) {
                            RichText newText = Text.rich().withStrikethrough();
                            stack.peek().with(newText);
                            stack.push(newText);
                        } else if (node.nodeName().equals("u")) {
                            RichText newText = Text.rich().withUnderline();
                            stack.peek().with(newText);
                            stack.push(newText);
                        } else if (node.nodeName().equals("blink")) {
                            RichText newText = Text.rich().withBlink();
                            stack.peek().with(newText);
                            stack.push(newText);
                        } else if (node.nodeName().equals("font")) {
                            Element e = (Element) node;
                            RichText newText = Text.rich();
                            if (e.hasAttr("size")) {
                                newText.withSize(Integer.parseInt(e.attr("size")));
                            }
                            if (e.hasAttr("color")) {
                                newText.withColor(Color.decode(e.attr("color")));
                            }
                            stack.peek().with(newText);
                            stack.push(newText);
                        } else if (node.nodeName().equals("a")) {
                            Element e = (Element) node;
                            RichText newText = Text.rich();
                            if (e.hasAttr("href")) {
                                newText.withLink(e.attr("href"));
                            }
                            stack.peek().with(newText);
                            stack.push(newText);
                        } else if (node.nodeName().equals("#text")) {
                            stack.peek().with(Text.plain(((TextNode) node).getWholeText()));
                        } else {
                            parsed.with(Text.rich().with(Text.plain("UnsupportedTag" + node.nodeName())));
                        }
                    } else {
                        if (node.nodeName().equals("b")) {
                            RichText currentText = Text.rich().withBold();
                            parsed.with(currentText);
                            stack.push(currentText);
                        } else if (node.nodeName().equals("i")) {
                            RichText currentText = Text.rich().withItalic();
                            parsed.with(currentText);
                            stack.push(currentText);
                        } else if (node.nodeName().equals("s")) {
                            RichText currentText = Text.rich().withStrikethrough();
                            parsed.with(currentText);
                            stack.push(currentText);
                        } else if (node.nodeName().equals("u")) {
                            RichText currentText = Text.rich().withUnderline();
                            parsed.with(currentText);
                            stack.push(currentText);
                        } else if (node.nodeName().equals("blink")) {
                            RichText currentText = Text.rich().withBlink();
                            parsed.with(currentText);
                            stack.push(currentText);
                        } else if (node.nodeName().equals("font")) {
                            Element e = (Element) node;
                            RichText currentText = Text.rich();
                            if (e.hasAttr("size")) {
                                currentText.withSize(Integer.parseInt(e.attr("size")));
                            }
                            if (e.hasAttr("color")) {
                                currentText.withColor(Color.decode(e.attr("color")));
                            }
                            parsed.with(currentText);
                            stack.push(currentText);
                        } else if (node.nodeName().equals("a")) {
                            Element e = (Element) node;
                            RichText currentText = Text.rich();
                            if (e.hasAttr("href")) {
                                currentText.withLink(e.attr("href"));
                            }
                            parsed.with(currentText);
                            stack.push(currentText);
                        } else if (node.nodeName().equals("#text")) {
                            parsed.with(Text.plain(((TextNode) node).getWholeText()));
                        } else {
                            parsed.with(Text.rich().with(Text.plain("UnsupportedTag" + node.nodeName())));
                        }
                    }
                }
            }

            @Override
            public void tail(Node node, int depth) {
                if (!node.nodeName().equals("body") && !node.nodeName().equals("#text")) {
                    stack.pop();
                }
            }
        });
        return parsed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return o.toString().equals(toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
