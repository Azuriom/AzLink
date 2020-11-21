package com.azuriom.azlink.common.tasks;

import java.util.concurrent.TimeUnit;

public class TpsTask implements Runnable {

    private static final long TPS_TIME = TimeUnit.SECONDS.toNanos(20);

    private double tps = 20;
    private int currentTick = 0;

    private long lastTickTime = 0;

    @Override
    public void run() {
        this.currentTick++;

        if (this.currentTick % 20 != 0) {
            return;
        }

        long currentNanoTime = System.nanoTime();

        if (this.lastTickTime == 0) {
            this.lastTickTime = currentNanoTime;
            return;
        }

        this.tps = TPS_TIME / (double) (currentNanoTime - this.lastTickTime);

        this.lastTickTime = currentNanoTime;
    }

    public double getTps() {
        return this.tps;
    }

    public int getCurrentTick() {
        return this.currentTick;
    }
}
