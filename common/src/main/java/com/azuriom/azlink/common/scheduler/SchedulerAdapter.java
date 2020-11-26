package com.azuriom.azlink.common.scheduler;

import java.util.concurrent.TimeUnit;

public interface SchedulerAdapter {

    void executeSync(Runnable runnable);

    void executeAsync(Runnable runnable);

    CancellableTask executeAsyncLater(Runnable runnable, long delay, TimeUnit unit);

    CancellableTask executeAsyncRepeating(Runnable runnable, long delay, long interval, TimeUnit unit);

    default void shutdown() throws Exception {

    }
}
