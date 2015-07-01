package com.samczsun.skype4j;

import java.io.FileInputStream;

import com.samczsun.skype4j.PendingLogin.Client;
import com.samczsun.skype4j.events.EventHandler;
import com.samczsun.skype4j.events.Listener;
import com.samczsun.skype4j.events.chat.message.MessageReceivedEvent;

public class Test {
    public static void main(String[] args) throws Exception {
        String[] creds = StreamUtils.readFully(new FileInputStream("credentials")).split(":");
        Skype skype = SkypeClient.create(creds[0], creds[1]).client(Client.WEB).login();
        skype.getEventDispatcher().registerListener(new Listener() {
            @EventHandler
            public void onUserAdd(MessageReceivedEvent e) {
                System.out.println("Got message " + e.getMessage().getMessage() + " in " + e.getChat().getIdentity());
            }
        });
        skype.subscribe();
        System.out.println("Done");
    }
}
