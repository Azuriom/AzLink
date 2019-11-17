package com.azuriom.azlink.common.data;

public class SystemData {

    private final double memory;
    private final double cpu;

    public SystemData(double memory, double cpu) {
        this.memory = memory;
        this.cpu = cpu;
    }

    public double getMemory() {
        return memory;
    }

    public double getCpu() {
        return cpu;
    }
}