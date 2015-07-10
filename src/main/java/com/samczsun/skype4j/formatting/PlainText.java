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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlainText plainText = (PlainText) o;

        return !(value != null ? !value.equals(plainText.value) : plainText.value != null);

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}