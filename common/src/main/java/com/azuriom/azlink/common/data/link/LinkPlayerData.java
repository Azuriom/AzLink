package com.azuriom.azlink.common.data.link;

import java.util.Map;
import java.util.UUID;

public class LinkPlayerData {

    private final UUID uuid;
    private final String name;
    private final Map<String, Object> data;

    public LinkPlayerData(UUID uuid, String name, Map<String, Object> data) {
        this.uuid = uuid;
        this.name = name;
        this.data = data;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
