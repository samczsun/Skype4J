/*
 * Copyright 2015 Sam Sun <me@samczsun.com>
 *
 * This file is part of Skype4J.
 *
 * Skype4J is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Skype4J is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Skype4J.
 * If not, see http://www.gnu.org/licenses/.
 */

package com.samczsun.skype4j.formatting;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public RichText withBold() {
        this.bold = true;
        return this;
    }

    public RichText withUnderline() {
        this.underline = true;
        return this;
    }

    public RichText withItalic() {
        this.italic = true;
        return this;
    }

    public RichText withStrikethrough() {
        this.strikethrough = true;
        return this;
    }

    public RichText withBlink() {
        this.blink = true;
        return this;
    }

    public RichText withLink(String link) {
        this.link = link;
        return this;
    }

    public RichText withColor(Color color) {
        this.color = Integer.toHexString(color.getRGB());
        this.color = this.color.substring(2, this.color.length());
        return this;
    }

    public RichText withSize(int size) {
        this.size = size;
        return this;
    }

    public RichText withCode() {
        this.code = true;
        return this;
    }

    public RichText with(Text t) {
        this.children.add(t);
        return this;
    }

    public Text child(int index) {
        return this.children.get(index);
    }

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