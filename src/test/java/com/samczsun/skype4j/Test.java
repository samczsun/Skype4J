package com.samczsun.skype4j;

import java.io.FileInputStream;

import com.samczsun.skype4j.PendingLogin.Client;
import com.samczsun.skype4j.chat.User;
import com.samczsun.skype4j.events.EventHandler;
import com.samczsun.skype4j.events.Listener;
import com.samczsun.skype4j.events.chat.ChatJoinedEvent;
import com.samczsun.skype4j.events.chat.TopicChangeEvent;
import com.samczsun.skype4j.events.chat.message.MessageReceivedEvent;
import com.samczsun.skype4j.events.chat.user.MultiUserAddEvent;
import com.samczsun.skype4j.events.chat.user.RoleUpdateEvent;
import com.samczsun.skype4j.events.chat.user.UserAddEvent;

public class Test {
    public static void main(String[] args) throws Exception {
        String[] creds = StreamUtils.readFully(new FileInputStream("credentials")).split(":");
        Skype skype = SkypeClient.create(creds[0], creds[1]).client(Client.WEB).login();
        skype.getEventDispatcher().registerListener(new Listener() {
            @EventHandler
            public void onUserAdd(MessageReceivedEvent e) {
                System.out.println("Got message " + e.getMessage().getText() + " in " + e.getChat().getIdentity());
            }

            @EventHandler
            public void onTopicChange(TopicChangeEvent e) {
                System.out.println("Topic changed in " + e.getChat().getIdentity());
            }

            @EventHandler
            public void onUserAdd(UserAddEvent e) {
                if (e instanceof MultiUserAddEvent) {
                    System.out.println("Users added in " + e.getChat().getIdentity() + ":");
                    for (User u : ((MultiUserAddEvent) e).getAllUsers()) {
                        System.out.println(u.getUsername());
                    }
                } else {
                    System.out.println("User " + e.getUser().getUsername() + " added in " + e.getChat().getIdentity());
                }
            }

            @EventHandler
            public void onRoleUpdate(RoleUpdateEvent e) {
                System.out.println(e.getUser().getUsername() + "'s role updated in " + e.getChat().getIdentity());
            }

            @EventHandler
            public void onChatJoin(ChatJoinedEvent e) {
                System.out.println("Joined new chat " + e.getChat().getIdentity());
            }
        });
        skype.subscribe();
        System.out.println("Done");
    }
}
