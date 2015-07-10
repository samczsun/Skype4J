package com.samczsun.skype4j.formatting;

/**
 * Created by sam on 2015-07-09.
 */
public class PlainText extends Text {

    private String value;

    PlainText(String value) {
        this.value = value;
    }

    public String write() {
        return this.value;
    }

    public String toString() {
        return this.write();
    }
}