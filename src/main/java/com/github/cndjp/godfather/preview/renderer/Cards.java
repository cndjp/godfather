package com.github.cndjp.godfather.renderer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.github.cndjp.godfather.GodfatherException;
import com.github.cndjp.godfather.event.Event;
import com.github.cndjp.godfather.event.GodfatherEventException;
import com.github.cndjp.godfather.event.Participant;
import com.github.cndjp.godfather.event.ParticipationStatus;
import com.github.cndjp.godfather.event.connpass.ConnpassParticipant;

public class Cards {
    /**
     * Name of a directory where a config parameter file and logs are stored.
     */
    private static final String CONFIG_DIR_NAME = ".godfather";
    /**
     * Name of a config parameter file.
     */
    private static final String OUTPUT_FILE_NAME = "cards.html";
    /**
     * File object of a directory where a config parameter file and logs are stored.
     */
    private static final File outputDir = new File(System.getProperty("user.home") + "/" + CONFIG_DIR_NAME);
    /**
     * File object of a config parameter file.
     */
    private static final File outputFile = new File(outputDir.getAbsolutePath() + "/" + OUTPUT_FILE_NAME);
    /**
     *
     */
    private Event event;
    /**
     *
     */
    private List<ParticipationStatus> participationStatusList = new ArrayList<>();

    public Cards event(final Event event) {
        this.event = event;
        return this;
    }

    public Cards addParticipationStatus(ParticipationStatus status) {
        this.participationStatusList.add(status);
        return this;
    }

    public void render() throws GodfatherException {
        if (this.event == null) {
            throw new GodfatherRendererException("insufficient parameter.");
        }
        if (!outputDir.exists() || !outputDir.isDirectory()) {
            outputDir.mkdir();
        }
        FileWriter file = null;
        PrintWriter pw = null;
        try {
            file = new FileWriter(outputFile, true);
            pw = new PrintWriter(new BufferedWriter(file));
            write(pw);
            pw.close();
        } catch (IOException e) {
            throw new GodfatherRendererException(e.getMessage());
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    private void write(PrintWriter pw) throws GodfatherEventException {
//        List<Participant> participants = event.getAllParticipantsWithoutCancelled();
        List<Participant> participants = event.getParticipants(ParticipationStatus.WAITLISTED);
        if (participants.size() % 2 == 1) {
            participants.add(new ConnpassParticipant("", "", null, ParticipationStatus.CANCELLED));
        }

        // TODO: デザイン要素はcssに分離してプラグインできるようにする
        pw.println("<div class=\"container border\">");
        for (int i = 0; i < participants.size(); i += 2) {
            pw.println("    <div class=\"row align-items-center border\">");
            pw.println("        <div class=\"col-md-6 py-2 bg-info text-light\">");
            pw.println("            <h4 class=\"text-center\">" + event.getTitle() + "</h4>");
            pw.println("        </div>");
            pw.println("        <div class=\"col-md-6 py-2 bg-info text-light border-left\">");
            pw.println("            <h4 class=\"text-center\">" + event.getTitle() + "</h4>");
            pw.println("        </div>");
            pw.println("    </div>");
            pw.println("    <div class=\"row align-items-center border\">");
            pw.println("        <div class=\"col-md-2\">");
            pw.println("            <img src=\"" + participants.get(i).getImageSource() + "\"");
            pw.println("                 class=\"rounded\"");
            pw.println("                 width=\"160\" height=\"160\"");
            pw.println("                 style=\"margin:20px 5px; object-fit:cover\"/>");
            pw.println("        </div>");
            pw.println("        <div class=\"col-md-4 text-dark\">");
            pw.println("            <h2 class=\"text-center\">" + participants.get(i).getDisplayName() + "</h2>");
            pw.println("        </div>");
            pw.println("        <div class=\"col-md-2 border-left\">");
            pw.println("            <img src=\"" + participants.get(i + 1).getImageSource() + "\"");
            pw.println("                 class=\"rounded\"");
            pw.println("                 width=\"160\" height=\"160\"");
            pw.println("                 style=\"margin:20px 5px; object-fit:cover\"/>");
            pw.println("        </div>");
            pw.println("        <div class=\"col-md-4 text-dark\">");
            pw.println("            <h2 class=\"text-center\">" + participants.get(i + 1).getDisplayName() + "</h2>");
            pw.println("        </div>");
            pw.println("    </div>");
            if ((i + 1 + 1) % 10 == 0) {
                pw.println("    <div style=\"page-break-before:always\" />");
            }
        }
        pw.println("</div>");
    }

    /**
     * Remove config file form file system.
     */
    public static void flashCards() {
        if (outputFile.exists()) {
            outputFile.delete();
        }
    }

    public static File getRenderdFile() {
        return outputFile;
    }

}