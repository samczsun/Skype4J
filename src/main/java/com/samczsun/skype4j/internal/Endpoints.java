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

package com.samczsun.skype4j.internal;

public class Endpoints {
    public static final String ACCEPT_CONTACT_REQUEST = "https://api.skype.com/users/self/contacts/auth-request/%s/accept";

    public static final String GET_JOIN_URL = "https://api.scheduler.skype.com/threads";
    public static final String CHAT_INFO_URL = "https://%sclient-s.gateway.messenger.live.com/v1/threads/%s/?view=msnp24Equivalent";
    public static final String SEND_MESSAGE_URL = "https://%sclient-s.gateway.messenger.live.com/v1/users/ME/conversations/%s/messages";
    public static final String MODIFY_MEMBER_URL = "https://%sclient-s.gateway.messenger.live.com/v1/threads/%s/members/8:%s";
    public static final String MODIFY_PROPERTY_URL = "https://%sclient-s.gateway.messenger.live.com/v1/threads/%s/properties?name=%s";
    public static final String ADD_MEMBER_URL = "https://client-s.gateway.messenger.live.com/v1/threads/%s/members/8:%s";
}
