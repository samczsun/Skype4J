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

package com.samczsun.skype4j.events.chat.sent;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.events.chat.ChatEvent;
import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.formatting.Text;
import com.samczsun.skype4j.user.User;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;

public class LocationReceivedEvent extends ChatEvent {
    private User sender;
    private LocationInfo info;

    public LocationReceivedEvent(Chat chat, User sender, LocationInfo info) {
        super(chat);
        this.sender = sender;
        this.info = info;
    }

    public User getSender() {
        return this.sender;
    }

    public LocationInfo getLocation() {
        return this.info;
    }

    public static class LocationInfo {
        private String base64;
        private String text;
        private double latitude = Double.NaN;
        private double longitude = Double.NaN;
        private int zoomLevel = -1;
        private String sty; //TODO what is this?
        private String ss; //TODO what is this?
        public LocationInfo(String parse, String text) throws IllegalArgumentException {
            this.base64 = parse;
            this.text = text;
            try {
                String[] args = new String(DatatypeConverter.parseBase64Binary(parse), "UTF-8").split("&");
                for (String s : args) {
                    String[] data = s.split("=");
                    if (data[0].equalsIgnoreCase("cp")) {
                        String[] coords = data[1].split("~");
                        latitude = Double.parseDouble(coords[0]);
                        longitude = Double.parseDouble(coords[1]);
                    } else if (data[0].equalsIgnoreCase("lvl")) {
                        zoomLevel = Integer.parseInt(data[1]);
                    } else if (data[0].equalsIgnoreCase("sty")) {
                        sty = data[1];
                    } else if (data[0].equalsIgnoreCase("ss")) {
                        ss = data[1];
                    } else {
                        throw new IllegalArgumentException("Unknown argument type " + data[0]);
                    }
                }

                if (Double.isNaN(latitude) || Double.isNaN(longitude) || zoomLevel == -1 || sty == null || ss == null) {
                    throw new IllegalArgumentException("Missing certain parameters in args");
                }
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException("UTF-8 is not supported by your Java installation");
            }
        }

        public double getLatitude() {
            return this.latitude;
        }

        public double getLongitude() {
            return this.longitude;
        }

        public int getZoomLevel() {
            return this.zoomLevel;
        }

        public String getSty() {
            return this.sty;
        }

        public String getSs() {
            return this.ss;
        }

        public String getText() {
            return this.text;
        }

        public Message toMessage() {
            return Message.create().with(Text.rich().withLink("https://www.bing.com/maps/" + this.base64).with(Text.plain(this.text)));
        }
    }
}
