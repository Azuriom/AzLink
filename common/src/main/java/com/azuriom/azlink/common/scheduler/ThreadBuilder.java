package com.azuriom.azlink.common.scheduler;

import java.util.concurrent.Executors;

public class ThreadBuilder {

    private final Thread thread;

    public ThreadBuilder(Runnable runnable) {
        this(Executors.defaultThreadFactory().newThread(runnable));
    }

    public ThreadBuilder(Thread thread) {
        this.thread = thread;
    }

    public ThreadBuilder name(String name) {
        thread.setName(name);
        return this;
    }

    public ThreadBuilder daemon() {
        return daemon(true);
    }

    public ThreadBuilder daemon(boolean daemon) {
        thread.setDaemon(daemon);
        return this;
    }

    public ThreadBuilder setPriority(int priority) {
        thread.setPriority(priority);
        return this;
    }

    public Thread build() {
        return thread;
    }
}
