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

package com.samczsun.skype4j.chat.objects;

/**
 * Represents a file which has been sent by a user in the chat
 * Note that downloading files is not supported on Skype for Web, even if the chat is cloud-based
 */
public interface ReceivedFile {

    /**
     * Get the name of the file which has been sent
     * @return The name of the file
     */
    String getName();

    /**
     * Get the size of the file
     * @return The filesize
     */
    long getSize();

    /**
     * Get the tid. Not sure what this does
     * @return The tid
     */
    long getTid();
}
