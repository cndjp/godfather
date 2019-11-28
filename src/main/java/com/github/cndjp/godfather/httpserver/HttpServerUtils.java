package com.github.cndjp.godfather.httpserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

class HttpServerUtils {

    static String getFinalPath(URI uri) {
        String[] pathElements = uri.getPath().split("/");
        return pathElements[pathElements.length - 1];
    }

    static byte[] convertFile(InputStream in) throws IOException {
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