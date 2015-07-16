package com.samczsun.skype4j;

import com.samczsun.skype4j.internal.SkypeImpl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SkypeBuilder {

    private final String username;
    private final String password;

    private Set<String> resources = new HashSet<>();
    private boolean lazyLoadChats = true;
    private boolean loadMessages = false;

    public SkypeBuilder(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public SkypeBuilder withAllResources() {
        resources.addAll(Arrays.asList("/v1/users/ME/conversations/ALL/properties", "/v1/users/ME/conversations/ALL/messages", "/v1/users/ME/contacts/ALL", "/v1/threads/ALL"));
        return this;
    }

    public SkypeBuilder withConversationProperties() {
        resources.add("/v1/users/ME/conversations/ALL/properties");
        return this;
    }

    public SkypeBuilder withConversationMessages() {
        resources.add("/v1/users/ME/conversations/ALL/messages");
        return this;
    }

    public SkypeBuilder withContacts() {
        resources.add("/v1/users/ME/contacts/ALL");
        return this;
    }

    public SkypeBuilder withThreads() {
        resources.add("/v1/threads/ALL");
        return this;
    }

    public SkypeBuilder loadChats() {
        this.lazyLoadChats = false;
        return this;
    }

    public SkypeBuilder loadMessages() {
        this.loadMessages = true;
        return this;
    }

    public Skype build() {
        return new SkypeImpl(username, password);
    }
}
