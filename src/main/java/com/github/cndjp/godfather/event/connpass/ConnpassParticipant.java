package com.github.cndjp.godfather.event.connpass;

import java.net.URL;
import com.github.cndjp.godfather.event.Participant;
import com.github.cndjp.godfather.event.ParticipationStatus;

public class ConnpassParticipant implements Participant {

    private final String id;

    private final String displayName;

    private final URL imageSource;

    private final ParticipationStatus participationStatus;

    public ConnpassParticipant(final String id, final String displayName,
                               final URL imageSource, final ParticipationStatus participationStatus) {
        this.id = id;
        this.displayName = displayName;
        this.imageSource = imageSource;
        this.participationStatus = participationStatus;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public URL getImageSource() {
        return this.imageSource;
    }

    @Override
    public ParticipationStatus getParticipationStatus() {
        return this.participationStatus;
    }

    @Override
    public String toString() {
        return "ConnpassParticipant{" +
                "id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                ", imageSource=" + imageSource +
                ", participationStatus=" + participationStatus +
                '}';
    }

}