package com.azuriom.azlink.common.data;

import java.util.List;

public class ServerData {

    private final PlatformData platform;
    private final String version;

    private final List<PlayerData> players;
    private final int maxPlayers;

    private final SystemData system;
    private final WorldData worlds;

    private final boolean full;

    public ServerData(PlatformData platform, String version, List<PlayerData> players, int maxPlayers, SystemData system, WorldData worlds, boolean full) {
        this.platform = platform;
        this.version = version;
        this.players = players;
        this.maxPlayers = maxPlayers;
        this.system = system;
        this.worlds = worlds;
        this.full = full;
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

    public boolean isFull() {
        return full;
    }
}
