package com.azuriom.azlink.velocity;

import com.azuriom.azlink.common.scheduler.CancellableTask;
import com.azuriom.azlink.common.scheduler.SchedulerAdapter;
import com.velocitypowered.api.scheduler.ScheduledTask;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class VelocitySchedulerAdapter implements SchedulerAdapter {

    private final Executor executor = this::executeAsync;

    private final AzLinkVelocityPlugin plugin;

    public VelocitySchedulerAdapter(AzLinkVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void executeSync(Runnable runnable) {
        this.executeAsync(runnable);
    }

    @Override
    public void executeAsync(Runnable runnable) {
        this.plugin.getProxy().getScheduler().buildTask(this.plugin, runnable).schedule();
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
        ScheduledTask task = this.plugin.getProxy().getScheduler()
                .buildTask(this.plugin, runnable)
                .delay(delay, unit)
                .schedule();

        return new CancellableVelocityTask(task);
    }

    @Override
    public CancellableTask executeAsyncRepeating(Runnable runnable, long delay, long interval, TimeUnit unit) {
        ScheduledTask task = this.plugin.getProxy().getScheduler()
                .buildTask(this.plugin, runnable)
                .delay(delay, unit)
                .repeat(interval, unit)
                .schedule();

        return new CancellableVelocityTask(task);
    }

    private static class CancellableVelocityTask implements CancellableTask {

        private final ScheduledTask task;

        public CancellableVelocityTask(ScheduledTask task) {
            this.task = task;
        }

        @Override
        public void cancel() {
            this.task.cancel();
        }
    }
}
