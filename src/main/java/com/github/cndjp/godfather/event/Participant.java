package com.github.cndjp.godfather.event;

import java.net.URL;

public interface Participant {

    String getId();

    String getDisplayName();

    URL getImageSource();

    ParticipationStatus getParticipationStatus();

}