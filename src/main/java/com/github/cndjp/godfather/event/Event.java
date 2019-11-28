package com.github.cndjp.godfather.event;

import com.github.cndjp.godfather.event.connpass.ConnpassEvent;

import java.net.URL;
import java.util.List;

public abstract class Event {

    private final URL url;

    public static Event getEvent(URL url) throws GodfatherEventException {
        if (url == null) {
            throw new NullPointerException();
        }
        if (isValidConnpassUrl(url)) {
            return new ConnpassEvent(url);
        }
        throw new GodfatherEventException("Specified URL doesn't represent valid event.");
    }

    protected Event(URL url) {
        this.url = url;
    }

    public URL getUrl() {
        return url;
    }

    abstract public String getTitle() throws GodfatherEventException;

    abstract public List<Participant> getAllParticipants() throws GodfatherEventException;

    abstract public List<Participant> getAllParticipantsWithoutCancelled() throws GodfatherEventException;

    abstract public List<Participant> getParticipants(ParticipationStatus participationStatus) throws GodfatherEventException;

    private static boolean isValidConnpassUrl(URL url) {
        if (url.getHost().endsWith(".connpass.com") && url.getPath().matches("/event/[0-9]+/?$")) {
            return true;
        }
        return false;
    }

}