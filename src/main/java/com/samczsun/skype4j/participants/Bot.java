/*
 * Copyright 2016 Sam Sun <me@samczsun.com>
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

package com.samczsun.skype4j.participants;

import com.samczsun.skype4j.participants.info.BotInfo;

/*
 * Represents a Bot in a chat.
 *
 * Note that while currently, Bots cannot be added to group chats, the feature is being planned.
 * As such, a Bot represents a single instance of a Bot in a chat. To get information about the bot you must
 * use {@link BotInfo}
 */
public interface Bot extends Participant {

    /*
     * Gets information and metadata associated with this Bot. Multiple Bot instances may point to the same BotInfo instance
     *
     * @returns The info pertaining to this Bot
     */
    BotInfo getBotInfo();
}
