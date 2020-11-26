package com.azuriom.azlink.common.utils;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

public final class SystemUtils {

    private static final MBeanServer BEAN_SERVER;
    private static final ObjectName OS_OBJECT;

    static {
        try {
            BEAN_SERVER = ManagementFactory.getPlatformMBeanServer();
            OS_OBJECT = ObjectName.getInstance("java.lang:type=OperatingSystem");
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }

    private SystemUtils() {
        throw new UnsupportedOperationException();
    }

    public static double getCpuUsage() throws Exception {
        return (double) BEAN_SERVER.getAttribute(OS_OBJECT, "ProcessCpuLoad") * 100.0;
    }

    public static double getMemoryUsage() {
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024.0 / 1024.0;
    }
}
