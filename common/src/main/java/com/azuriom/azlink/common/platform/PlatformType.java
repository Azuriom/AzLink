package com.azuriom.azlink.common.platform;

public enum PlatformType {

    BUKKIT("Bukkit"),
    BUNGEE("BungeeCord"),
    SPONGE("Sponge"),
    VELOCITY("Velocity"),
    NUKKIT("Nukkit");

    private final String name;

    PlatformType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
