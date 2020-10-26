package com.azuriom.azlink.common;

import com.azuriom.azlink.common.scheduler.ThreadFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ThreadFactoryBuilderTest {

    private final Runnable voidRunnable = () -> {};

    @Test
    public void testThreadFactoryBuilder() {
        ThreadFactoryBuilder factory = new ThreadFactoryBuilder().name("thread-factory-test-%t").priority(2);

        Thread thread0 = factory.newThread(voidRunnable);
        Thread thread1 = factory.newThread(voidRunnable);

        Assertions.assertEquals("thread-factory-test-0", thread0.getName());
        Assertions.assertEquals("thread-factory-test-1", thread1.getName());
        Assertions.assertEquals(2, thread1.getPriority());
        Assertions.assertFalse(thread0.isDaemon());

        ThreadFactoryBuilder daemonFactory = factory.daemon();

        Thread daemonThread = daemonFactory.newThread(voidRunnable);

        Assertions.assertTrue(daemonThread.isDaemon());
    }
}
