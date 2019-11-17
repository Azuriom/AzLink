package com.azuriom.azlink.common.scheduler;

@FunctionalInterface
public interface Cancellable {

    void cancel();

}
