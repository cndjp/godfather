package com.github.cndjp.godfather.preview;

import com.github.cndjp.godfather.preview.renderer.Cards;
import com.github.cndjp.godfather.exception.GodfatherException.GodfatherPreviewException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;

public class PreviewServer {

    private HttpServer httpServer;

    // singleton
    private static PreviewServer instance = null;

    public static PreviewServer getInstance() {
        if (instance == null) {
            instance = new PreviewServer();
        }
        return instance;
    }

    // uninstanciable
    private PreviewServer() {}

    public void start(int port) throws GodfatherPreviewException {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            httpServer.createContext("/", new ResourceRequestHandler());
            httpServer.createContext("/renderd/", new GeneratedFileRequestHandler());
            httpServer.start();
        } catch (IOException e) {
            throw new GodfatherPreviewException(e.getMessage());
        }
    }

    public void stop() {
        if (httpServer == null) {
            return;
        }
        httpServer.stop(0);
    }

    static abstract class GodFatherRequestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            InputStream in = getResponseStream(exchange);
            byte[] response = null;
            if (in == null) {
                response = "<h1>404 Not Found</h1>No resource found for request".getBytes();
                exchange.sendResponseHeaders(404, 0);
            } else {
                response = convertFile(in);
                in.close();
                exchange.sendResponseHeaders(200, response.length);
            }
            OutputStream output = exchange.getResponseBody();
            output.write(response);
            output.close();
        }

        abstract InputStream getResponseStream(HttpExchange exchange);
    }

    static class ResourceRequestHandler extends GodFatherRequestHandler {
        @Override
        InputStream getResponseStream(HttpExchange exchange) {
            String resource = getFinalPath(exchange.getRequestURI());
            if ("/".equals(resource)) {
                resource = "/index.html";
            }
            InputStream in = getClass().getResourceAsStream(resource);
            return in;
        }
    }

    static class GeneratedFileRequestHandler extends GodFatherRequestHandler {
        @Override
        InputStream getResponseStream(HttpExchange exchange) {
            String resource = getFinalPath(exchange.getRequestURI());
            if (!Cards.getRenderdFile().getPath().endsWith(resource.substring(1))) {
                return null;
            }
            FileInputStream in = null;
            try {
                in = new FileInputStream(Cards.getRenderdFile());
            } catch (FileNotFoundException e) {
                /**
                 * It's a mechanically generated path, so it never becomes File Not Found.
                 */
                throw new IllegalStateException(e);
            }
            return in;
        }
    }

    static private String getFinalPath(URI uri) {
        if ("/".equals(uri.getPath())) {
            return "/";
        }
        String[] pathElements = uri.getPath().split("/");
        return "/" + pathElements[pathElements.length - 1];
    }

    static private byte[] convertFile(InputStream in) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (true) {
            int len = in.read(buffer);
            if (len < 0) {
                break;
            }
            bout.write(buffer, 0, len);
        }
        return bout.toByteArray();
    }

}
