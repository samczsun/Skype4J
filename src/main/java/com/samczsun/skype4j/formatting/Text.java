package com.samczsun.skype4j.formatting;

/**
 * Created by sam on 2015-07-09.
 */
public abstract class Text {
    public static final Text NEW_LINE = plain('\n');
    public static final Text BLANK = plain("");

    public abstract String write();

    public static RichText rich() {
        return new RichText();
    }

    public static PlainText plain(String text) {
        return new PlainText(text);
    }

    public static PlainText plain(byte text) {
        return plain(Integer.toString(text));
    }

    public static PlainText plain(char text) {
        return plain(String.valueOf(text));
    }

    public static PlainText plain(short text) {
        return plain(Integer.toString(text));
    }

    public static PlainText plain(double text) {
        return plain(Double.toString(text));
    }

    public static PlainText plain(float text) {
        return plain(Float.toString(text));
    }

    public static PlainText plain(int text) {
        return plain(Integer.toString(text));
    }

    public static PlainText plain(long text) {
        return plain(Long.toString(text));
    }

    public static PlainText plain(Object text) {
        return plain(text.toString());
    }

}
