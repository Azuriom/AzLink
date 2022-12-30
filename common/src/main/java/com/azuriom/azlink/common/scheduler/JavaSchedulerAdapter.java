package com.azuriom.azlink.common.scheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JavaSchedulerAdapter implements SchedulerAdapter {

    private final ScheduledExecutorService scheduler;
    private final Executor syncExecutor;
    private final Executor asyncExecutor;

    public JavaSchedulerAdapter(Executor syncExecutor, Executor asyncExecutor) {
        this(Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .name("azlink-scheduler")
                .daemon()), syncExecutor, asyncExecutor);
    }

    public JavaSchedulerAdapter(ScheduledExecutorService scheduler, Executor syncExecutor, Executor asyncExecutor) {
        this.scheduler = scheduler;
        this.syncExecutor = syncExecutor;
        this.asyncExecutor = asyncExecutor;
    }

    @Override
    public Executor syncExecutor() {
        return this.syncExecutor;
    }

    @Override
    public Executor asyncExecutor() {
        return this.asyncExecutor;
    }

    @Override
    public CancellableTask executeAsyncLater(Runnable runnable, long delay, TimeUnit unit) {
        return new CancellableFuture(this.scheduler.schedule(runnable, delay, unit));
    }

    @Override
    public CancellableTask executeAsyncRepeating(Runnable runnable, long delay, long interval, TimeUnit unit) {
        return new CancellableFuture(this.scheduler.scheduleAtFixedRate(runnable, delay, interval, unit));
    }

    @Override
    public void shutdown() throws Exception {
        this.scheduler.shutdown();

        this.scheduler.awaitTermination(5, TimeUnit.SECONDS);
    }

    private static class CancellableFuture implements CancellableTask {

        private final Future<?> future;

        public CancellableFuture(Future<?> future) {
            this.future = future;
        }

        @Override
        public void cancel() {
            this.future.cancel(false);
        }
    }
}
