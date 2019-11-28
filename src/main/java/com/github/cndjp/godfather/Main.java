package com.github.cndjp.godfather;

import java.io.IOException;
import java.net.URL;

import com.github.cndjp.godfather.event.Event;
import com.github.cndjp.godfather.event.GodfatherEventException;
import com.github.cndjp.godfather.httpserver.Server;
import com.github.cndjp.godfather.renderer.Cards;

/**
 * Hello world!
 *
 */
public class Main {

    public static void main(String[] args) throws IOException, GodfatherException {
        new Cards().event(Event.getEvent(new URL("https://cnd.connpass.com/event/154414/"))).render();
        Runtime.getRuntime().addShutdownHook(new Thread(
                () -> {
                    Server.stop();
                    Cards.flashCards();
                }
        ));
        Server.start();
    }

}