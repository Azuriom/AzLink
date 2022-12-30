package com.azuriom.azlink.common.scheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public interface SchedulerAdapter {

    default void executeSync(Runnable runnable) {
        syncExecutor().execute(runnable);
    }

    default void executeAsync(Runnable runnable) {
        asyncExecutor().execute(runnable);
    }

    Executor syncExecutor();

    Executor asyncExecutor();

    CancellableTask executeAsyncLater(Runnable runnable, long delay, TimeUnit unit);

    CancellableTask executeAsyncRepeating(Runnable runnable, long delay, long interval, TimeUnit unit);

    default void shutdown() throws Exception {

    }
}
