package com.azuriom.azlink.common.scheduler;

@FunctionalInterface
public interface CancellableTask {

    void cancel();

}
