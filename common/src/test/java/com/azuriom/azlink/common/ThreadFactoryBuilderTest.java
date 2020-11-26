package com.azuriom.azlink.common;

import com.azuriom.azlink.common.scheduler.ThreadFactoryBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ThreadFactoryBuilderTest {

    private final Runnable voidRunnable = () -> {};

    @Test
    public void testThreadFactoryBuilder() {
        ThreadFactoryBuilder factory = new ThreadFactoryBuilder().name("thread-factory-test-%t").priority(2);

        Thread thread0 = factory.newThread(this.voidRunnable);
        Thread thread1 = factory.newThread(this.voidRunnable);

        assertEquals("thread-factory-test-0", thread0.getName());
        assertEquals("thread-factory-test-1", thread1.getName());
        assertEquals(2, thread1.getPriority());
        assertFalse(thread0.isDaemon());

        ThreadFactoryBuilder daemonFactory = factory.daemon();

        Thread daemonThread = daemonFactory.newThread(this.voidRunnable);

        assertTrue(daemonThread.isDaemon());
    }
}
