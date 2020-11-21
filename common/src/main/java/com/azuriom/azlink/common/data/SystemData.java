package com.azuriom.azlink.common.data;

public class SystemData {

    private final double ram;
    private final double cpu;

    public SystemData(double ram, double cpu) {
        this.ram = ram;
        this.cpu = cpu;
    }

    public double getRam() {
        return this.ram;
    }

    public double getCpu() {
        return this.cpu;
    }
}
