package com.azuriom.azlink.common.utils;

import java.lang.management.ManagementFactory;

public final class SystemUtils {

    private SystemUtils() {
        throw new UnsupportedOperationException();
    }

    public static double getCpuUsage() {
        if (ManagementFactory.getOperatingSystemMXBean() instanceof com.sun.management.OperatingSystemMXBean) {
            return ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getProcessCpuLoad() * 100.0;
        }
        return -1;
    }

    public static double getMemoryUsage() {
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024.0 / 1024.0;
    }
}
