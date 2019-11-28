package com.github.cndjp.godfather.httpserver;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Server {

    private static HttpServer server = null;

    public static void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/resources/", new ResourceRequestHandler());
        server.createContext("/generated/", new GeneratedFileRequestHandler());
        server.start();
    }

    public static void stop() {
        if (server == null) {
            return;
        }
        server.stop(0);
    }

}
