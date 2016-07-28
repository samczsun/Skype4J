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

package com.samczsun.skype4j.internal.chat;

import com.samczsun.skype4j.chat.P2PChat;
import com.samczsun.skype4j.chat.messages.ChatMessage;
import com.samczsun.skype4j.exceptions.ChatNotFoundException;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.handler.ErrorHandler;
import com.samczsun.skype4j.formatting.IMoji;
import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.internal.SkypeImpl;
import com.samczsun.skype4j.internal.participants.ParticipantImpl;
import com.samczsun.skype4j.internal.participants.UserImpl;
import com.samczsun.skype4j.participants.Participant;
import com.samczsun.skype4j.participants.info.Contact;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class ChatP2P extends ChatImpl implements P2PChat {

    public ChatP2P(SkypeImpl skype, String identity) throws ConnectionException, ChatNotFoundException {
        super(skype, identity);
    }

    @Override
    public void load() throws ConnectionException, ChatNotFoundException {
        // Nothing to do
    }

    @Override
    public ChatMessage sendMessage(Message message) throws ConnectionException {
        throw new UnsupportedOperationException("Unsupported operation on a P2P chat");
    }

    @Override
    public ChatMessage sendMessage(String plainMessage) throws ConnectionException {
        throw new UnsupportedOperationException("Unsupported operation on a P2P chat");
    }

    @Override
    public void sendContact(Contact contact) throws ConnectionException {
        throw new UnsupportedOperationException("Unsupported operation on a P2P chat");
    }

    @Override
    public void sendImage(BufferedImage image, String imageType, String imageName) throws ConnectionException, IOException {
        throw new UnsupportedOperationException("Unsupported operation on a P2P chat");
    }

    @Override
    public void sendImage(File image) throws ConnectionException, IOException {
        throw new UnsupportedOperationException("Unsupported operation on a P2P chat");
    }

    @Override
    public void sendFile(File file) throws ConnectionException {
        throw new UnsupportedOperationException("Unsupported operation on a P2P chat");
    }

    @Override
    public void sendMoji(IMoji moji) throws ConnectionException {
        throw new UnsupportedOperationException("Unsupported operation on a P2P chat");
    }

    @Override
    public ParticipantImpl getParticipant(String id) {
        throw new UnsupportedOperationException("Unsupported operation on a P2P chat");
    }

    @Override
    public UserImpl getSelf() {
        throw new UnsupportedOperationException("Unsupported operation on a P2P chat");
    }

    @Override
    public Collection<Participant> getAllParticipants() {
        throw new UnsupportedOperationException("Unsupported operation on a P2P chat");
    }

    @Override
    public List<ChatMessage> getAllMessages() {
        throw new UnsupportedOperationException("Unsupported operation on a P2P chat");
    }

    @Override
    public void alertsOff() throws ConnectionException {
        throw new UnsupportedOperationException("Unsupported operation on a P2P chat");
    }

    @Override
    public void alertsOn() throws ConnectionException {
        throw new UnsupportedOperationException("Unsupported operation on a P2P chat");
    }

    @Override
    public void alertsOn(String keyword) throws ConnectionException {
        throw new UnsupportedOperationException("Unsupported operation on a P2P chat");
    }

    @Override
    public void addUser(String username) throws ConnectionException {
        throw new UnsupportedOperationException("Unsupported operation on a P2P chat");
    }

    @Override
    public void removeUser(String username) {
        throw new UnsupportedOperationException("Unsupported operation on a P2P chat");
    }

    @Override
    public List<ChatMessage> loadMoreMessages(int amount) throws ConnectionException {
        throw new UnsupportedOperationException("Unsupported operation on a P2P chat");
    }

    @Override
    public void startTyping(ErrorHandler handler) {
        throw new UnsupportedOperationException("Unsupported operation on a P2P chat");
    }

    @Override
    public void stopTyping() {
        throw new UnsupportedOperationException("Unsupported operation on a P2P chat");
    }

    @Override
    public void sync() throws ConnectionException {
        throw new UnsupportedOperationException("Unsupported operation on a P2P chat");
    }
}
