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

import org.unbescape.html.HtmlEscape;

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
        return new PlainText(HtmlEscape.escapeHtml5(text));
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

    public static PlainText rawHtml(String html) {
        return new PlainText(html);
    }
}
