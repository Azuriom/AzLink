package com.azuriom.azlink.common.scheduler;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple ThreadFactory builder implementation.
 * Inspired by the Guava ThreadFactoryBuilder.
 */
public class ThreadFactoryBuilder implements ThreadFactory {

    private final AtomicInteger threadCount = new AtomicInteger();

    private final ThreadFactory originalThreadFactory;

    private String name;
    private int priority;
    private boolean daemon;

    public ThreadFactoryBuilder() {
        this(Executors.defaultThreadFactory());
    }

    public ThreadFactoryBuilder(ThreadFactory threadFactory) {
        this.originalThreadFactory = threadFactory;
    }

    public ThreadFactoryBuilder name(String name) {
        this.name = Objects.requireNonNull(name, "name");
        return this;
    }

    public ThreadFactoryBuilder priority(int priority) {
        if (priority > Thread.MAX_PRIORITY || priority < Thread.MIN_PRIORITY) {
            throw new IllegalArgumentException("Priority must be between " + Thread.MIN_PRIORITY + " and " + Thread.MAX_PRIORITY + " : " + priority);
        }

        this.priority = priority;
        return this;
    }

    public ThreadFactoryBuilder daemon(boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    public ThreadFactoryBuilder daemon() {
        return daemon(true);
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = originalThreadFactory.newThread(runnable);

        if (name != null) {
            thread.setName(name.replace("%t", Integer.toString(threadCount.getAndIncrement())));
        }

        if (priority > 0) {
            thread.setPriority(priority);
        }

        if (daemon) {
            thread.setDaemon(true);
        }

        return thread;
    }
}
