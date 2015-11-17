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
