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

package com.samczsun.skype4j;

import com.samczsun.skype4j.internal.SkypeImpl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * This class is used to construct a new {@link Skype} instance.
 */
public class SkypeBuilder {

    private final String username;
    private final String password;

    private Set<String> resources = new HashSet<>();

    private Logger customLogger;

    /**
     * Construct a SkypeBuilder with the given username and password
     *
     * @param username The username
     * @param password The password
     */
    public SkypeBuilder(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Subscribe to all known resources
     *
     * @return The same SkypeBuilder
     */
    public SkypeBuilder withAllResources() {
        resources.addAll(Arrays.asList("/v1/users/ME/conversations/ALL/properties", "/v1/users/ME/conversations/ALL/messages", "/v1/users/ME/contacts/ALL", "/v1/threads/ALL"));
        return this;
    }

    /**
     * Subscribe to a resource which has not been added into the API
     *
     * @param resource The resource to subscribe to
     * @return The same SkypeBuilder
     */
    public SkypeBuilder withResource(String resource) {
        resources.add(resource);
        return this;
    }

    /**
     * Use a custom logger for this Skype instance
     *
     * @param logger The custom logger to use
     * @return The same SkypeBuilder
     */
    public SkypeBuilder withLogger(Logger logger) {
        this.customLogger = logger;
        return this;
    }

    /**
     * Build the Skype instance!
     *
     * @return The Skype instance
     */
    public Skype build() {
        if (resources.isEmpty()) {
            throw new IllegalArgumentException("No resources selected");
        }
        return new SkypeImpl(username, password, resources, customLogger);
    }
}
