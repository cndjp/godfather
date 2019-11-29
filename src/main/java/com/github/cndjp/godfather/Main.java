package com.github.cndjp.godfather;

import java.io.IOException;
import java.net.URL;

import com.github.cndjp.godfather.event.Event;
import com.github.cndjp.godfather.preview.PreviewServer;
import com.github.cndjp.godfather.preview.renderer.Cards;

/**
 * Hello world!
 *
 */
public class Main {

    public static void main(String[] args) throws IOException, GodfatherException {
        new Cards().event(Event.getEvent(new URL("https://cnd.connpass.com/event/154414/"))).render();
        PreviewServer server = PreviewServer.getInstance();
        Runtime.getRuntime().addShutdownHook(new Thread(
                () -> {
                    server.stop();
                    Cards.flashCards();
                }
        ));
        server.getInstance().start(8080);
    }

}