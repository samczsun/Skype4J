package com.samczsun.skype4j.formatting;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

public class RichText extends Text {
    private boolean bold = false;
    private boolean italic = false;
    private boolean underline = false;
    private boolean strikethrough = false;
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
}