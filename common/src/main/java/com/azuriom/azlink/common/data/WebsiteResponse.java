package com.azuriom.azlink.common.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebsiteResponse {

    private final Map<String, List<String>> commands = new HashMap<>();
    private final List<UserInfo> users = new ArrayList<>();

    public WebsiteResponse(Map<String, List<String>> commands) {
        this.commands.putAll(commands);
    }

    public Map<String, List<String>> getCommands() {
        return this.commands;
    }

    public List<UserInfo> getUsers() {
        return users;
    }
}
