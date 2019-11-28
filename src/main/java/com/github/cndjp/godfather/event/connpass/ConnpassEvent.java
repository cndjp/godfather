package com.github.cndjp.godfather.event.connpass;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import com.github.cndjp.godfather.event.Participant;
import com.github.cndjp.godfather.event.Event;
import com.github.cndjp.godfather.event.GodfatherEventException;
import com.github.cndjp.godfather.event.ParticipationStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ConnpassEvent extends Event {

    /**
     *
     */
    private static final URL IMAGE_SOURCE_DEFAULT;
    static {
        try {
            IMAGE_SOURCE_DEFAULT = new URL("https://connpass.com/static/img/common/user_no_image_180.png");
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     *
     */
    private Document eventRootDocument = null;

    private Document getEventRootDocument() throws GodfatherEventException {
        try {
            if (eventRootDocument != null) {
                return eventRootDocument;
            }
            return eventRootDocument = Jsoup.connect(getUrl().toString()).get();
        } catch (IOException e) {
            throw new GodfatherEventException(e.getMessage());
        }
    }

    /**
     * Constructor
     *
     * @param url
     */
    public ConnpassEvent(URL url) {
        super(url);
    }

    @Override
    public String getTitle() throws GodfatherEventException {
        return getEventRootDocument().select("meta[itemprop=name]").attr("content");
    }

    @Override
    public List<Participant> getAllParticipants() throws GodfatherEventException {
        List<Participant> allParticipant = new ArrayList<Participant>();
        for (ParticipationStatus status : ParticipationStatus.values()) {
            allParticipant.addAll(getParticipants(status));
        }
        return allParticipant;
    }

    @Override
    public List<Participant> getAllParticipantsWithoutCancelled() throws GodfatherEventException {
        List<Participant> allParticipant = new ArrayList<Participant>();
        for (ParticipationStatus status : ParticipationStatus.values()) {
            if (!ParticipationStatus.CANCELLED.equals(status)) {
                allParticipant.addAll(getParticipants(status));
            }
        }
        return allParticipant;
    }

    @Override
    public List<Participant> getParticipants(ParticipationStatus status) throws GodfatherEventException {
        try {
            Document document = Jsoup.connect(getParticipantsListUrl()).get();
            Elements userTableElements;
            switch (status) {
                case ORGANIZER:
                    userTableElements = document.select("div[class=concerned_area mb_30]");
                    break;
                case PARTICIPANT:
                    userTableElements = document.select("div[class=participation_table_area mb_20]");
                    break;
                case WAITLISTED:
                    userTableElements = document.select("div[class=waitlist_table_area mb_20]");
                    break;
                case CANCELLED:
                    userTableElements = document.select("div[class=cancelled_table_area mb_20]");
                    break;
                default:
                    throw new IllegalStateException();
            }
            return getParticipants(userTableElements, status);
        } catch (IOException e) {
            throw new GodfatherEventException(e.getMessage());
        }
    }

    private List<Participant> getParticipants(
            Elements userTableElements, ParticipationStatus status) throws GodfatherEventException {
        List<Participant> participants = new ArrayList<>();
        try {
            Elements userTableElementsConsideringPagination = new Elements();
            for (Element table : userTableElements) {
                Elements paginatedUserListLink = table.select("tr.empty td[colspan=2] a");
                if (paginatedUserListLink.isEmpty()) {
                    userTableElementsConsideringPagination.add(table);
                    continue;
                }
                String paginatedUserListUrl = paginatedUserListLink.first().attr("href");
                if (paginatedUserListUrl == null || !paginatedUserListUrl.contains("/ptype/")) {
                    userTableElementsConsideringPagination.add(table);
                    continue;
                }
                Document page1 = Jsoup.connect(paginatedUserListUrl).get();
                userTableElementsConsideringPagination.add(page1);
                int participantsCount = Integer.parseInt(
                        page1.select("span.participants_count").text().replace("人", ""));
                int lastPage = participantsCount / 100 + 1;
                for (int i = 2; i <= lastPage; i++) {
                    Document pagex = Jsoup.connect(paginatedUserListUrl + "?page=" + i).get();
                    userTableElementsConsideringPagination.add(pagex);
                }
            }
            Elements users = userTableElementsConsideringPagination.select("td.user .user_info");
            int i = 0;
            for (Element user : users) {
                String displayName = user.select("p.display_name a").text();
                Document userHome = Jsoup.connect(
                        user.select("p.display_name a").attr("href")).get();
                URL imageSource = IMAGE_SOURCE_DEFAULT;
                Elements images = userHome.select("div[id=side_area] div[class=mb_20 text_center] a.image_link");
                if (!images.isEmpty()) {
                    for (Element image : images) {
                        String href = image.attr("href");
                        if (href.contains("/user/")) {
                            imageSource = new URL(href);
                            break;
                        }
                    }
                }
                System.out.println(displayName + "(" + status.toString() + "): " + ++i + "/" + users.size());
                participants.add(new ConnpassParticipant(displayName, displayName, imageSource, status));
            }
        } catch (IOException e) {
            throw new GodfatherEventException(e.getMessage());
        }
        return participants;
    }

    /**
     *
     */
    private String getParticipantsListUrl() {
        String eventUrl = getUrl().toString();
        if (eventUrl.endsWith("/")) {
            return eventUrl + "participation/";
        } else {
            return eventUrl + "/participation/";
        }
    }

}