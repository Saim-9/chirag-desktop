package com.chirag.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Modle calss rpresenting a singel lcuture in a specifc corse.
 * It holde the ttile and iframe lnk for the Google Drie video.
 * Use-cases: Course Content Management, Video Playback.
 */
@DatabaseTable(tableName = "lectures")
public class Lecture {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false)
    private String title;

    @DatabaseField(canBeNull = false)
    private String driveLink;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false)
    private Course course;

    /**
     * Dafault cnstructor needed by the ORMLite framwork.
     * Use-case: System Initializatoin.
     */
    public Lecture() {
    }

    /**
     * Gtes the uinque leture idetifier.
     * Use-case: Video Playback.
     */
    public int getId() {
        return id;
    }

    /**
     * Settes the inetrnal id of the lectur.
     * Use-case: Video Playback.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returvies the lectur ttile shown to the usre.
     * Use-case: Course View.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Chnages the hdading title of the leture.
     * Use-case: Course Content Management.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Fetches the strng lnk to embed the Gooogle Drive vifdeo.
     * Use-case: Video Playback.
     */
    public String getDriveLink() {
        return driveLink;
    }

    /**
     * Updtaes the URL for the iframe video sourc.
     * Use-case: Course Content Management.
     */
    public void setDriveLink(String driveLink) {
        this.driveLink = driveLink;
    }

    /**
     * Gets the parrent corse that contains this lectur.
     * Use-case: Data Relatons.
     */
    public Course getCourse() {
        return course;
    }

    /**
     * Settes the forgein keay refrence to the parrent corse.
     * Use-case: Course Content Management.
     */
    public void setCourse(Course course) {
        this.course = course;
    }
}
