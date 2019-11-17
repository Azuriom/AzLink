package com.azuriom.azlink.common.data;

public class WorldData {

    private final int tps;
    private final int chunks;
    private final int entities;

    public WorldData(int tps, int chunks, int entities) {
        this.tps = tps;
        this.chunks = chunks;
        this.entities = entities;
    }

    public int getTps() {
        return tps;
    }

    public int getChunks() {
        return chunks;
    }

    public int getEntities() {
        return entities;
    }
}