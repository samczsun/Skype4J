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

import org.jsoup.helper.Validate;
import org.unbescape.html.HtmlEscape;

/**
 * Represents a text component in a message
 */
public abstract class Text {
    /**
     * A convenience PlainText object representing a newline
     */
    public static final Text NEW_LINE = plain(System.getProperty("line.separator"));
    /**
     * A convenience PlainText object representing a blank value
     */
    public static final Text BLANK = plain("");

    /**
     * Get the HTML of this text
     *
     * @return The HTML
     */
    public abstract String write();

    /**
     * The equivilant of calling {@code Text#write}
     *
     * @return The HTML of this text
     */
    public String toString() {
        return this.write();
    }

    /**
     * Creates a new RichText component
     *
     * @param text The plain text to wrap in the RichText object
     * @return A new RichText object
     */
    public static RichText rich(String text) {
        return new RichText(text);
    }

    /**
     * Creates a new RichText component
     *
     * @param format The text to format
     * @param params The arguments to use
     * @return A new RichText object
     */
    public static RichText rich(String format, Object... params) {
        Validate.notNull(format, "Format was null");
        Validate.notNull(params, "Parameters were null. If you don't want to pass any, consider plain(String)");
        return rich(String.format(format, params));
    }

    /**
     * Creates a new PlainText component with the given text
     *
     * @param format The text to format
     * @param params The arguments to use
     * @return The PlainText object representing the text
     */
    public static PlainText plain(String format, Object... params) {
        Validate.notNull(format, "Format was null");
        Validate.notNull(params, "Parameters were null. If you don't want to pass any, consider plain(String)");
        return plain(String.format(format, params));
    }

    /**
     * Creates a new PlainText component with the given text
     *
     * @param text The text to use
     * @return The PlainText object representing the text
     */
    public static PlainText plain(String text) {
        Validate.notNull(text, "The message cannot be null");
        return new PlainText(HtmlEscape.escapeHtml5Xml(text));
    }

    /**
     * Creates a new PlainText component with the given byte
     *
     * @param text The byte to use
     * @return The PlainText object representing the byte
     */
    public static PlainText plain(byte text) {
        return plain(Integer.toString(text));
    }

    /**
     * Creates a new PlainText component with the given char
     *
     * @param text The char to use
     * @return The PlainText object representing the char
     */
    public static PlainText plain(char text) {
        return plain(String.valueOf(text));
    }

    /**
     * Creates a new PlainText component with the given short
     *
     * @param text The short to use
     * @return The PlainText object representing the short
     */
    public static PlainText plain(short text) {
        return plain(Integer.toString(text));
    }

    /**
     * Creates a new PlainText component with the given double
     *
     * @param text The double to use
     * @return The PlainText object representing the double
     */
    public static PlainText plain(double text) {
        return plain(Double.toString(text));
    }

    /**
     * Creates a new PlainText component with the given float
     *
     * @param text The float to use
     * @return The PlainText object representing the float
     */
    public static PlainText plain(float text) {
        return plain(Float.toString(text));
    }

    /**
     * Creates a new PlainText component with the given int
     *
     * @param text The int to use
     * @return The PlainText object representing the int
     */
    public static PlainText plain(int text) {
        return plain(Integer.toString(text));
    }

    /**
     * Creates a new PlainText component with the given long
     *
     * @param text The long to use
     * @return The PlainText object representing the long
     */
    public static PlainText plain(long text) {
        return plain(Long.toString(text));
    }

    /**
     * Creates a new PlainText component with the given object
     *
     * @param text The object to use
     * @return The PlainText object representing the object
     */
    public static PlainText plain(Object text) {
        Validate.notNull(text, "The message cannot be null");
        return plain(text.toString());
    }

    /**
     * Creates a new PlainText component using the given HTML
     *
     * @param html The HTML to use
     * @return The PlainText object representing the HTML
     */
    public static PlainText html(String html) {
        return new PlainText(html);
    }
}