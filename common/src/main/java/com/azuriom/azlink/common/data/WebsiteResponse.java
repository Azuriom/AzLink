package com.azuriom.azlink.common.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebsiteResponse {

    private final Map<String, List<String>> commands = new HashMap<>();

    public WebsiteResponse(Map<String, List<String>> commands) {
        this.commands.putAll(commands);
    }

    public Map<String, List<String>> getCommands() {
        return this.commands;
    }
}
