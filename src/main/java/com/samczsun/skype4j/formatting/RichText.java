/*
 * Copyright 2016 Sam Sun <me@samczsun.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.samczsun.skype4j.formatting;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.awt.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

/**
 * Represents a rich text component. This component can be formatted.
 * All children will also have the specified formats.
 */
public class RichText extends Text {

    public enum Format {
        BOLD("b", RichText::withBold),
        ITALIC("i", RichText::withItalic),
        UNDERLINE("u", RichText::withUnderline),
        STRIKE_THROUGH("s", RichText::withStrikethrough),
        CODE("pre", RichText::withCode),
        BLINK("blink", RichText::withBlink);

        private final String tagName;

        private final Consumer<RichText> apply;

        Format(String tagName, Consumer<RichText> apply) {
            this.tagName = tagName;
            this.apply = apply;
        }

        public String getTagName() {
            return this.tagName;
        }

        public Consumer<RichText> getApplicator() {
            return this.apply;
        }

        public String getOpenTag() {
            return "<" + this.tagName + ">";
        }

        public String getCloseTag() {
            return "</" + this.tagName + ">";
        }
    }

    private static final Map<String, BiConsumer<RichText, Element>> TAG_APPLIER = Collections.unmodifiableMap(
            new HashMap<String, BiConsumer<RichText, Element>>() {{
                Arrays.stream(Format.values()).forEach(format -> put(format.getTagName(), (text, elem) -> format.getApplicator().accept(text)));
                put("font", (text, elem) -> {
                    if (elem.hasAttr("size")) {
                        text.withSize(Integer.parseInt(elem.attr("size")));
                    }
                    if (elem.hasAttr("color")) {
                        text.withColor(Color.decode(elem.attr("color")));
                    }
                });
                put("a", (text, elem) -> text.withLink(elem.attr("href")));
                put("#text", (text, elem) -> {
                    // How do we handle this?
                });
            }}
    );

    private static final Map<String, BiPredicate<RichText, Element>> TAG_TEST = Collections.unmodifiableMap(
            new HashMap<String, BiPredicate<RichText, Element>>() {{
                Arrays.stream(Format.values()).forEach(format -> put(format.getTagName(), (text, elem) -> text.hasFormat(format)));
                put("font", (text, elem) -> {
                    boolean equal = true;
                    if (elem.hasAttr("size") && text.size >= 0) {
                        equal = equal && text.size == Integer.parseInt(elem.attr("size"));
                    } else if (!elem.hasAttr("size") && text.size == -1) {
                        equal = equal && true;
                    } else {
                        equal = false;
                    }
                    if (equal) {
                        if (elem.hasAttr("color") && text.color != null) {
                            String color = elem.attr("color");
                            equal = equal && text.color.equals(color.substring(color.indexOf('#') + 1));
                        } else if (!elem.hasAttr("color") && text.color == null) {
                            equal = equal && true;
                        } else {
                            equal = false;
                        }
                    }
                    return equal;
                });
                put("a", (text, elem) -> elem.attr("href").equals(text.link));
                put("#text", (text, elem) -> {
                    // How do we handle this?
                    return false;
                });
            }}
    );

    private final Set<Format> formats = EnumSet.noneOf(RichText.Format.class);

    private String link = null;

    private String color = null;

    private int size = -1;

    private RichText next;

    private RichText previous;

    private String text;

    RichText(String text) {
        this(null, text);
    }

    RichText(RichText previous, String text) {
        this.previous = previous;
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    private RichText setText(String text) {
        this.text = text;
        return this;
    }

    private void appendText(String text) {
        this.text += text;
    }

    /**
     * Make this text component bold
     *
     * @return The same RichText instance
     */
    public RichText withBold() {
        this.formats.add(Format.BOLD);
        return this;
    }

    /**
     * Make this text component underlined
     *
     * @return The same RichText instance
     */
    public RichText withUnderline() {
        this.formats.add(Format.UNDERLINE);
        return this;
    }

    /**
     * Make this text component italicized
     *
     * @return The same RichText instance
     */
    public RichText withItalic() {
        this.formats.add(Format.ITALIC);
        return this;
    }

    /**
     * Make this text component struck through
     *
     * @return The same RichText instance
     */
    public RichText withStrikethrough() {
        this.formats.add(Format.STRIKE_THROUGH);
        return this;
    }

    /**
     * Make this text component blink
     *
     * @return The same RichText instance
     */
    public RichText withBlink() {
        this.formats.add(Format.BLINK);
        return this;
    }

    /**
     * Make this text component link to the supplied URL
     *
     * @param link The URL to link to
     * @return The same RichText instance
     */
    public RichText withLink(String link) {
        this.link = link;
        return this;
    }

    /**
     * Give this text component a color
     *
     * @param color The color to use
     * @return The same RichText instance
     */
    public RichText withColor(Color color) {
        this.color = Integer.toHexString(color.getRGB());
        this.color = this.color.substring(2, this.color.length());
        return this;
    }

    /**
     * Give this text component a size
     *
     * @param size The size to use
     * @return The same RichText instance
     */
    public RichText withSize(int size) {
        this.size = size;
        return this;
    }

    /**
     * Make this text component code-formatted
     *
     * @return The same RichText instance
     */
    public RichText withCode() {
        this.formats.add(Format.CODE);
        return this;
    }

    public boolean hasFormat(Format format) {
        return this.formats.contains(format);
    }


    public RichText append(String text) {
        return append(text, false);
    }

    public RichText append(String text, boolean clearFormat) {
        this.next = new RichText(this, Text.parseEmojis(text));
        if (!clearFormat) {
            this.next.copyFormat(this);
        }
        return this.next;
    }

    private void copyFormat(RichText from) {
        this.formats.addAll(from.formats);
        this.link = from.link;
        this.color = from.color;
        this.size = from.size;
    }

    public String write() {
        return this.previous != null ? this.previous.write() : this.write0();
    }

    private String write0() {
        StringBuilder output = new StringBuilder();
        java.util.List<Format> formats = Arrays.asList(RichText.Format.values());
        formats.stream()
                .filter(format -> this.previous == null || !this.previous.formats.contains(format))
                .filter(this.formats::contains)
                .map(Format::getOpenTag)
                .forEach(output::append);

        boolean font = size != -1 || color != null;
        boolean openFont = font;
        boolean openLink = this.link != null;
        if (this.previous != null) {
            openLink = openLink && !this.link.equals(this.previous.link);
            openFont = openFont && (this.size != this.previous.size || !Objects.equals(this.color, this.previous.color));
        }
        if (openFont) {
            output.append("<font ");
            if (size != -1) {
                output.append("size=\"").append(size).append("\" ");
            }
            if (color != null) {
                output.append("color=\"#").append(color).append("\" ");
            }
            output.setLength(output.length() - 1);
            output.append(">");
        }
        if (openLink) {
            output.append("<a href=\"").append(this.link).append("\">");
        }
        output.append(this.text);
        boolean closeLink = this.link != null;
        boolean closeFont = font;
        if (this.next != null) {
            closeLink = closeLink && !this.link.equals(this.next.link);
            closeFont = closeFont && (this.size != this.next.size || !Objects.equals(this.color, this.next.color));
        }
        if (closeLink) {
            output.append("</a>");
        }
        if (closeFont) {
            output.append("</font>");
        }
        Collections.reverse(formats);
        formats.stream()
                .filter(format -> this.next == null || !this.next.formats.contains(format))
                .filter(this.formats::contains)
                .map(Format::getCloseTag)
                .forEach(output::append);

        if (this.next != null) {
            output.append(this.next.write0());
        }
        return output.toString();
    }

    @Override
    public String toString() {
        return this.previous != null ? this.previous.toString() : this.write();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (this.previous != null) {
            return this.previous.equals(o);
        }
        RichText text = (RichText) o;
        while (text.previous != null) {
            text = text.previous;
        }
        return this.equals0(text);
    }

    private boolean equals0(RichText richText) {
        if (!this.formats.equals(richText.formats)) return false;
        if (this.size != richText.size) return false;
        if (Objects.equals(this.link, richText.link)) return false;
        if (Objects.equals(this.color, richText.color)) return false;
        return this.next == null ? richText.next == null : this.next.equals0(richText.next);
    }

    @Override
    public int hashCode() {
        return this.previous != null ? this.previous.hashCode() : this.hashCode0();
    }

    public int hashCode0() {
        int result = (this.formats.contains(Format.BOLD) ? 1 : 0);
        result = 31 * result + (this.formats.contains(Format.ITALIC) ? 1 : 0);
        result = 31 * result + (this.formats.contains(Format.UNDERLINE) ? 1 : 0);
        result = 31 * result + (this.formats.contains(Format.STRIKE_THROUGH) ? 1 : 0);
        result = 31 * result + (this.formats.contains(Format.CODE) ? 1 : 0);
        result = 31 * result + (this.formats.contains(Format.BLINK) ? 1 : 0);
        result = 31 * result + (this.link != null ? this.link.hashCode() : 0);
        result = 31 * result + (this.color != null ? this.color.hashCode() : 0);
        result = 31 * result + this.size;
        result = 31 * result + (this.next != null ? this.next.hashCode0() : 0);
        return result;
    }

    public static RichText fromHtml(String html) {
        Document doc = Jsoup.parse(html);
        doc.outputSettings().prettyPrint(false);
        RichText root = new RichText("");
        parse(root, doc.getElementsByTag("body").get(0));
        return root;
    }

    private static RichText parse(RichText root, Node node) {
        RichText current = root;
        if (node instanceof Element) {
            Element elem = (Element) node;
            applyTag(current, elem);
            String inner = elem.html();
            Elements children = elem.children();
            if (children.size() > 0) {
                String[] parts = new String[children.size() + 1];
                int i = 0;
                int index = 0;
                for (Element child : children) {
                    int startChild = inner.indexOf("<" + child.tag().toString(), index);
                    int endChild = startChild + child.outerHtml().length();
                    parts[i++] = inner.substring(index, startChild);
                    index = endChild;
                }
                parts[i] = inner.substring(index);
                Element last = elem;
                for (int j = 0; j < parts.length; j++) {
                    if (hasTag(root, last)) {
                        current.appendText(parts[j]);
                    } else {
                        current = current.append(parts[j], true);
                        current.copyFormat(root);
                    }
                    if (j < children.size()) {
                        Element child = children.get(j);
                        if (!hasTag(current, child)) {
                            current = current.append("", true);
                            current.copyFormat(root);
                        }
                        current = parse(current, child);
                        last = child;
                    }
                }
            } else {
                current.appendText(inner);
            }
        }
        return current;
    }

    private static void applyTag(RichText text, Element tag) {
        RichText.TAG_APPLIER.getOrDefault(tag.tagName(), (t, elem) -> {
        }).accept(text, tag);
    }

    private static boolean hasTag(RichText text, Element tag) {
        return RichText.TAG_TEST.getOrDefault(tag.tagName(), (t, elem) -> true).test(text, tag);
    }
}