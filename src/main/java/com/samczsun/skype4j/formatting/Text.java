package com.samczsun.skype4j.formatting;

import java.awt.Color;

import org.apache.commons.lang3.StringEscapeUtils;

public class Text {
    private Message parent;

    private String text = null;
    private boolean bold = false;
    private boolean italic = false;
    private boolean underline = false;
    private boolean strikethrough = false;
    private boolean blink = false;
    private String link = null;
    private String color = null;
    private int size = -1;

    public Text(Message parent, String text) {
        this.text = text;
        this.parent = parent;
    }

    public Text bold() {
        this.bold = true;
        return this;
    }

    public Text underline() {
        this.underline = true;
        return this;
    }

    public Text italic() {
        this.italic = true;
        return this;
    }

    public Text strikethrough() {
        this.strikethrough = true;
        return this;
    }

    public Text blink() {
        this.blink = true;
        return this;
    }

    public Text link(String link) {
        this.link = link;
        return this;
    }

    public Text link() {
        this.link = this.text;
        return this;
    }

    public Text color(Color color) {
        this.color = Integer.toHexString(color.getRGB());
        this.color = this.color.substring(2, this.color.length());
        return this;
    }

    public Text size(int size) {
        this.size = size;
        return this;
    }

    public Text text(String text) {
        Text t = new Text(parent, StringEscapeUtils.escapeHtml4(text));
        parent.components.add(t);
        return t;
    }
    
    public Text unsafeText(String text) {
        Text t = new Text(parent, text);
        parent.components.add(t);
        return t;
    }

    public Text text(String text, Object... args) {
        return text(String.format(text, args));
    }

    public Text text(byte text) {
        return text(Integer.toString(text));
    }

    public Text text(char text) {
        return text(String.valueOf(text));
    }

    public Text text(short text) {
        return text(Integer.toString(text));
    }

    public Text text(double text) {
        return text(Double.toString(text));
    }

    public Text text(float text) {
        return text(Float.toString(text));
    }

    public Text text(int text) {
        return text(Integer.toString(text));
    }

    public Text text(long text) {
        return text(Long.toString(text));
    }

    public Text text(Object text) {
        return text(text.toString());
    }

    public Text newLine() {
        return text("\n");
    }
    
    public Text removeLast() {
        parent.components.remove(parent.components.size() - 1);
        parent.components.trimToSize();
        return this;
    }

    public String write() {
        if ((bold || italic || underline || strikethrough) && link != null) {
            throw new IllegalArgumentException("You may not format links with bold/italic/underline/strikethrough");
        }
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
        output.append(this.text);
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

    public Message parent() {
        return this.parent;
    }
}
