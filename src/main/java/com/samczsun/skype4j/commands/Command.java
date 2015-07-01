package com.samczsun.skype4j.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.User;

public abstract class Command {
    private List<String> aliases = new ArrayList<String>();
    private String name;

    public Command(String mainCommand, String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
        this.name = mainCommand;
    }

    public void load() {

    }

    public void save() {

    }

    public abstract void onCommand(User sender, Chat chat, String command, String[] args);

    public boolean isCorrectCommand(String command) {
        if (command.equalsIgnoreCase(name))
            return true;
        for (String s : aliases) {
            if (command.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public List<String> getAliases() {
        return Collections.unmodifiableList(aliases);
    }
}
