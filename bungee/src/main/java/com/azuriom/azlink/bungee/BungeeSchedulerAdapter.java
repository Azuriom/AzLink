package com.azuriom.azlink.bungee;

import com.azuriom.azlink.common.scheduler.CancellableTask;
import com.azuriom.azlink.common.scheduler.SchedulerAdapter;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class BungeeSchedulerAdapter implements SchedulerAdapter {

    private final Executor executor = this::executeAsync;

    private final AzLinkBungeePlugin plugin;

    public BungeeSchedulerAdapter(AzLinkBungeePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void executeSync(Runnable runnable) {
        this.executeAsync(runnable);
    }

    @Override
    public void executeAsync(Runnable runnable) {
        this.plugin.getProxy().getScheduler().runAsync(this.plugin, runnable);
    }

    @Override
    public Executor syncExecutor() {
        return this.executor;
    }

    @Override
    public Executor asyncExecutor() {
        return this.executor;
    }

    @Override
    public CancellableTask executeAsyncLater(Runnable runnable, long delay, TimeUnit unit) {
        ScheduledTask task = this.plugin.getProxy()
                .getScheduler()
                .schedule(this.plugin, runnable, delay, unit);

        return new CancellableBungeeTask(task);
    }

    @Override
    public CancellableTask executeAsyncRepeating(Runnable runnable, long delay, long interval, TimeUnit unit) {
        ScheduledTask task = this.plugin.getProxy()
                .getScheduler()
                .schedule(this.plugin, runnable, delay, interval, unit);

        return new CancellableBungeeTask(task);
    }

    private static class CancellableBungeeTask implements CancellableTask {

        private final ScheduledTask task;

        public CancellableBungeeTask(ScheduledTask task) {
            this.task = task;
        }

        @Override
        public void cancel() {
            this.task.cancel();
        }
    }
}
