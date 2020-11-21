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
        return this.tps;
    }

    public int getChunks() {
        return this.chunks;
    }

    public int getEntities() {
        return this.entities;
    }
}
