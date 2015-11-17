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
