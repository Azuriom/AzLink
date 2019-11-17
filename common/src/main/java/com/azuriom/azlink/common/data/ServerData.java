package com.azuriom.azlink.common.data;

import com.azuriom.azlink.common.PlatformType;

import java.util.List;

public class ServerData {

    private final PlatformData platform;
    private final String version;

    private final List<PlayerData> players;
    private final int maxPlayers;

    private final SystemData system;
    private final WorldData worlds;

    public ServerData(PlatformData platform, String version, List<PlayerData> players, int maxPlayers, SystemData system, WorldData worlds) {
        this.platform = platform;
        this.version = version;
        this.players = players;
        this.maxPlayers = maxPlayers;
        this.system = system;
        this.worlds = worlds;
    }

    public PlatformData getPlatform() {
        return platform;
    }

    public String getVersion() {
        return version;
    }

    public List<PlayerData> getPlayers() {
        return players;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public SystemData getSystem() {
        return system;
    }

    public WorldData getWorlds() {
        return worlds;
    }
}
