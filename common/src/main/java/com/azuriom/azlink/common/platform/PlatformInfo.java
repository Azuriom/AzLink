package com.azuriom.azlink.common.platform;

import java.util.Objects;

public class PlatformInfo {

    private final String name;
    private final String version;

    public PlatformInfo(String name, String version) {
        this.name = Objects.requireNonNull(name, "name");
        this.version = Objects.requireNonNull(version, "version");
    }

    public String getName() {
        return this.name;
    }

    public String getVersion() {
        return this.version;
    }

    @Override
    public String toString() {
        return "PlatformInfo{name='" + this.name + "', version='" + this.version + "'}";
    }
}
