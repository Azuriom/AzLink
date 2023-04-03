package com.azuriom.azlink.bukkit.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import java.util.function.Consumer;

public class Compatibility {

    public static <T> T callMethod(Class<?> clazz, Object object, String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            return (T) clazz.getDeclaredMethod(methodName, parameterTypes).invoke(object, args);
        } catch (Throwable t) {
            throw new IllegalStateException(t);
        }
    }

    public static <T> T callMethod(Object object, String methodName, Class<?>[] parameterTypes, Object... args) {
        return callMethod(object.getClass(), object, methodName, parameterTypes, args);
    }

    public static <T> T callMethod(Class<?> clazz, String methodName) {
        return callMethod(clazz, null, methodName, new Class[]{});
    }

    private static boolean methodExist(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            clazz.getDeclaredMethod(methodName, parameterTypes);
            return true;
        } catch (Throwable ignored) {}
        return false;
    }

    public static boolean isFolia() {
        return methodExist(Bukkit.class, "getGlobalRegionScheduler");
    }

    public static void runTask(Plugin plugin, Runnable runnable) {
        if (isFolia()) {
            Object globalRegionScheduler = callMethod(Bukkit.class, "getGlobalRegionScheduler");
            callMethod(globalRegionScheduler, "run", new Class[]{Plugin.class, Consumer.class}, plugin, (Consumer<?>) (task) -> runnable.run());
            return;
        }
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    public static void runTaskAsync(Plugin plugin, Runnable runnable) {
        if (isFolia()) {
            Object asyncScheduler = callMethod(Bukkit.class, "getAsyncScheduler");
            callMethod(asyncScheduler, "runNow", new Class[]{Plugin.class, Consumer.class}, plugin, (Consumer<?>) (task) -> runnable.run());
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    public static void runTaskAtFixedRate(Plugin plugin, Runnable runnable, long initialDelayTicks, long periodTicks) {
        if (isFolia()) {
            Object globalRegionScheduler = callMethod(Bukkit.class, "getGlobalRegionScheduler");
            callMethod(globalRegionScheduler, "runAtFixedRate", new Class[]{Plugin.class, Consumer.class, long.class, long.class},
                       plugin, (Consumer<?>) (task) -> runnable.run(), initialDelayTicks, periodTicks);
            return;
        }
        Bukkit.getScheduler().runTaskTimer(plugin, runnable, initialDelayTicks, periodTicks);
    }

}
