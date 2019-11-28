package com.github.cndjp.godfather.httpserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.github.cndjp.godfather.renderer.Cards;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class GeneratedFileRequestHandler implements HttpHandler {
    // TODO Try with resources.
    // TODO Use skelton pattern.
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println(exchange.getRequestURI().getPath());
        FileInputStream in = new FileInputStream(Cards.getRenderdFile());

        byte[] response = null;
        if (in == null) {
            response = "<h1>404 Not Found</h1>No resource found for request".getBytes();
            exchange.sendResponseHeaders(404, 0);
        } else {
            response = HttpServerUtils.convertFile(in);
            in.close();
            exchange.sendResponseHeaders(200, response.length);
        }
        OutputStream output = exchange.getResponseBody();
        output.write(response);
        output.close();
    }

}