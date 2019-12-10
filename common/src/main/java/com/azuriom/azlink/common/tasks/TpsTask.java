package com.azuriom.azlink.common.tasks;

import java.util.concurrent.TimeUnit;

public class TpsTask implements Runnable {

    private static final long TPS_TIME = TimeUnit.SECONDS.toNanos(20);

    private double tps = 20;
    private int tick = 0;

    private long lastTickTime = 0;

    @Override
    public void run() {
        tick++;

        if (tick % 20 != 0) {
            return;
        }

        long currentNanoTime = System.nanoTime();

        if (lastTickTime == 0) {
            lastTickTime = currentNanoTime;
            return;
        }

        tps = TPS_TIME / (double) (currentNanoTime - lastTickTime);

        lastTickTime = currentNanoTime;
    }

    public double getTps() {
        return tps;
    }

    public int getTick() {
        return tick;
    }
}
