package com.azuriom.azlink.common.http.server;

public interface HttpServer {

    int DEFAULT_PORT = 25588;

    void start();

    void stop();
}
