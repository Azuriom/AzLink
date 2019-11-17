package com.azuriom.azlink.common.data;

import java.util.HashMap;
import java.util.Map;

public class WebsiteResponse {

    private final Map<String, String> commands = new HashMap<>();

    public WebsiteResponse(Map<String, String> commands) {
        this.commands.putAll(commands);
    }

    public Map<String, String> getCommands() {
        return commands;
    }
}
