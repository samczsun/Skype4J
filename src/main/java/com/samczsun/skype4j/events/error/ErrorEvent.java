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

package com.samczsun.skype4j.events.error;

import com.samczsun.skype4j.events.Event;

/**
 * Represents an error which has occured within the internal API
 * Most likely this error was caused through some action performed on the API, hence it is disclosed to you
 * Some errors are non-important - they can be safely ignored.
 * Others should be handled properly as they signal the API will no longer function at all
 */
public abstract class ErrorEvent extends Event {
    public abstract Throwable getError();
}
