package com.azuriom.azlink.common.data;

public class WorldData {

    private final double tps;
    private final int chunks;
    private final int entities;

    public WorldData(double tps, int chunks, int entities) {
        this.tps = tps;
        this.chunks = chunks;
        this.entities = entities;
    }

    public double getTps() {
        return tps;
    }

    public int getChunks() {
        return chunks;
    }

    public int getEntities() {
        return entities;
    }
}
