package com.samczsun.skype4j.formatting;

import java.util.ArrayList;

import org.apache.commons.lang3.StringEscapeUtils;

public class Message {
    public static Text fromLegacy(String rawText) {
        return Message.text(rawText);
    }

    private Message() {
    }

    protected ArrayList<Text> components = new ArrayList<>();

    public static Text text(String text) {
        Message message = new Message();
        Text t = new Text(message, StringEscapeUtils.escapeHtml4(text));
        message.components.add(t);
        return t;
    }
    
    public static Text unsafeText(String text) {
        Message message = new Message();
        Text t = new Text(message, text);
        message.components.add(t);
        return t;
    }

    public static Text text(String text, Object... args) {
        return text(String.format(text, args));
    }

    public static Text text(byte text) {
        return text(Integer.toString(text));
    }

    public static Text text(char text) {
        return text(String.valueOf(text));
    }

    public static Text text(short text) {
        return text(Integer.toString(text));
    }

    public static Text text(double text) {
        return text(Double.toString(text));
    }

    public static Text text(float text) {
        return text(Float.toString(text));
    }

    public static Text text(int text) {
        return text(Integer.toString(text));
    }

    public static Text text(long text) {
        return text(Long.toString(text));
    }

    public static Text text(Object text) {
        return text(text.toString());
    }

    public static Text newLine() {
        return text("\n");
    }

    public String write() {
        StringBuilder result = new StringBuilder();
        for (Text t : components) {
            result.append(t);
        }
        return result.toString();
    }

    public String toString() {
        return this.write();
    }
}
