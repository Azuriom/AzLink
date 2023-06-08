package com.azuriom.azlink.bukkit.integrations;

import com.azuriom.azlink.common.scheduler.JavaSchedulerAdapter;
import com.azuriom.azlink.common.scheduler.SchedulerAdapter;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.Executor;

public class FoliaSchedulerAdapter extends JavaSchedulerAdapter {

    private FoliaSchedulerAdapter(Executor syncExecutor, Executor asyncExecutor) {
        super(syncExecutor, asyncExecutor);
    }

    public static void scheduleSyncTask(Plugin plugin, Runnable task, long delay, long interval) {
        plugin.getServer()
                .getGlobalRegionScheduler()
                .runAtFixedRate(plugin, t -> task.run(), delay, interval);
    }

    public static SchedulerAdapter create(Plugin plugin) {
        plugin.getLogger().info("Folia support enabled successfully.");

        return new FoliaSchedulerAdapter(
                r -> plugin.getServer().getGlobalRegionScheduler().execute(plugin, r),
                r -> plugin.getServer().getAsyncScheduler().runNow(plugin, t -> r.run())
        );
    }
}
