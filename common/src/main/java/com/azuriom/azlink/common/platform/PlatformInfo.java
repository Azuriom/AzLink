package com.azuriom.azlink.common.platform;

public class PlatformInfo {

    private final String name;
    private final String version;

    public PlatformInfo(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "PlatformInfo{name='" + name + "', version='" + version + "'}";
    }
}
