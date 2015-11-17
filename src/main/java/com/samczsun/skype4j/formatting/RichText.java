/*
 * Copyright 2015 Sam Sun <me@samczsun.com>
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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a rich text component. This component can be formatted.
 * All children will also have the specified formats.
 */
public class RichText extends Text {
    private boolean bold = false;
    private boolean italic = false;
    private boolean underline = false;
    private boolean strikethrough = false;
    private boolean code = false;
    private boolean blink = false;
    private String link = null;
    private String color = null;
    private int size = -1;

    private List<Text> children = new ArrayList<>();

    RichText() {
    }

    /**
     * Make this text component bold
     *
     * @return The same RichText instance
     */
    public RichText withBold() {
        this.bold = true;
        return this;
    }

    /**
     * Make this text component underlined
     *
     * @return The same RichText instance
     */
    public RichText withUnderline() {
        this.underline = true;
        return this;
    }

    /**
     * Make this text component italicized
     *
     * @return The same RichText instance
     */
    public RichText withItalic() {
        this.italic = true;
        return this;
    }

    /**
     * Make this text component struck through
     *
     * @return The same RichText instance
     */
    public RichText withStrikethrough() {
        this.strikethrough = true;
        return this;
    }

    /**
     * Make this text component blink
     *
     * @return The same RichText instance
     */
    public RichText withBlink() {
        this.blink = true;
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
        this.code = true;
        return this;
    }

    /**
     * Add a child to this text component
     *
     * @return The same RichText instance
     */
    public RichText with(Text t) {
        this.children.add(t);
        return this;
    }

    /**
     * Get the child component at the given index
     *
     * @return The same text component at the given index
     */
    public Text child(int index) {
        return this.children.get(index);
    }

    /**
     * Get all the children of this text component
     *
     * @return A view of all the children
     */
    public List<Text> children() {
        return Collections.unmodifiableList(this.children);
    }

    public String write() {
        StringBuilder output = new StringBuilder();
        if (bold) {
            output.append("<b>");
        }
        if (italic) {
            output.append("<i>");
        }
        if (underline) {
            output.append("<u>");
        }
        if (strikethrough) {
            output.append("<s>");
        }
        if (blink) {
            output.append("<blink>");
        }
        if (code) {
            output.append("<pre>");
        }
        boolean font = size != -1 || color != null;
        if (font) {
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
        if (this.link != null) {
            output.append("<a href=\"").append(this.link).append("\">");
        }
        for (Text t : this.children) {
            output.append(t.write());
        }
        if (this.link != null) {
            output.append("</a>");
        }
        if (font) {
            output.append("</font>");
        }
        if (code) {
            output.append("</pre>");
        }
        if (blink) {
            output.append("</blink>");
        }
        if (strikethrough) {
            output.append("</s>");
        }
        if (underline) {
            output.append("</u>");
        }
        if (italic) {
            output.append("</i>");
        }
        if (bold) {
            output.append("</b>");
        }
        return output.toString();
    }

    public String toString() {
        return this.write();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RichText richText = (RichText) o;

        if (bold != richText.bold) return false;
        if (italic != richText.italic) return false;
        if (underline != richText.underline) return false;
        if (strikethrough != richText.strikethrough) return false;
        if (code != richText.code) return false;
        if (blink != richText.blink) return false;
        if (size != richText.size) return false;
        if (link != null ? !link.equals(richText.link) : richText.link != null) return false;
        if (color != null ? !color.equals(richText.color) : richText.color != null) return false;
        return children.equals(richText.children);

    }

    @Override
    public int hashCode() {
        int result = (bold ? 1 : 0);
        result = 31 * result + (italic ? 1 : 0);
        result = 31 * result + (underline ? 1 : 0);
        result = 31 * result + (strikethrough ? 1 : 0);
        result = 31 * result + (code ? 1 : 0);
        result = 31 * result + (blink ? 1 : 0);
        result = 31 * result + (link != null ? link.hashCode() : 0);
        result = 31 * result + (color != null ? color.hashCode() : 0);
        result = 31 * result + size;
        result = 31 * result + children.hashCode();
        return result;
    }
}