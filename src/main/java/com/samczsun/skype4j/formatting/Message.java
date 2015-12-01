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

import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a formatted message
 */
public class Message {
    private final List<Text> components = new ArrayList<>();

    private Message() {
    }

    /**
     * Create a new message
     *
     * @return The message object
     */
    public static Message create() {
        return new Message();
    }

    /**
     * Add a child to this message
     *
     * @param text The child
     * @return The same message instance
     */
    public Message with(Text text) {
        this.components.add(text);
        return this;
    }

    /**
     * Get the HTML of this text
     *
     * @return The HTML
     */
    public String write() {
        StringBuilder result = new StringBuilder();
        for (Text t : this.components) {
            result.append(t);
        }
        return result.toString();
    }

    /**
     * The equivilant of calling {@code Text#write}
     *
     * @return The HTML of this text
     */
    public String toString() {
        return this.write();
    }

    /**
     * Get the value of this message as plaintext
     *
     * @return The plaintext value of this object
     */
    public String asPlaintext() {
        return Jsoup.parse(write()).text();
    }

    /**
     * Get the child component at the given index
     *
     * @param index The index of the child
     * @return The child component
     */
    public Text child(int index) {
        return this.components.get(index);
    }

    /**
     * Get all the children of this message
     *
     * @return A view of all the children
     */
    public List<Text> children() {
        return Collections.unmodifiableList(this.components);
    }

    /**
     * Parse a message from raw HTML
     *
     * @param text The HTML to parse from
     * @return The message object
     */
    public static Message fromHtml(String text) {
        final Message parsed = create();
        parsed.with(RichText.fromHtml(text));
        return parsed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return o.toString().equals(toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
