package com.github.cndjp.godfather;

import java.net.URL;

import com.github.cndjp.godfather.event.Event;
import com.github.cndjp.godfather.preview.PreviewServer;
import com.github.cndjp.godfather.preview.renderer.Cards;
import picocli.CommandLine;

/**
 *
 */
@CommandLine.Command(version = {
        "Godfather 1.0.0-SNAPSHOT",
        "Be aware of this version is in Alpha.",
        "(c) 2019 Cloud Native Developers JP" })
public class Godfather {

    @CommandLine.Option(names = {"-e", "--event-url"}, required = true, paramLabel = "EVENT_URL",
            description = "Event URL (e.g. https://cnd.connpass.com/event/154414/)")
    private URL eventUrl;

    @CommandLine.Option(names = {"-v", "--version"}, versionHelp = true, description = "display version info")
    private boolean versionInfoRequested = false;

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    private boolean usageHelpRequested = false;

    public static void exec(String[] args) {
        Godfather godfather = new Godfather();
        CommandLine commandLine = new CommandLine(godfather);
        commandLine.parseArgs(args);
        if (commandLine.isUsageHelpRequested()) {
            commandLine.usage(System.out);
            return;
        }
        if (commandLine.isVersionHelpRequested()) {
            commandLine.printVersionHelp(System.out);
            return;
        }
        godfather.doIt();
    }

    private void doIt() {
        PreviewServer server = PreviewServer.getInstance();
        try {
            new Cards().event(Event.getEvent(eventUrl)).render();
            Runtime.getRuntime().addShutdownHook(new Thread(
                    () -> {
                        server.stop();
                        Cards.flashCards();
                    }
            ));
            server.start(8080);
        } catch (GodfatherException e) {
            // TODO log
            e.printStackTrace();
        }
    }

}