package com.github.cndjp.godfather.httpserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ResourceRequestHandler implements HttpHandler {

    // TODO: Try with resources.
    // TODO Use skelton pattern.
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println(exchange.getRequestURI().getPath());

        String resource = "/" + HttpServerUtils.getFinalPath(exchange.getRequestURI());
        InputStream in = getClass().getResourceAsStream(resource); 

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