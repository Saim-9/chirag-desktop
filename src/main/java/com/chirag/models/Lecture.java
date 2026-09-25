package com.chirag.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Modle class rpresenting a single lecture in a specifc course.
 * It holde the ttile and iframe lnk for the Google Drie/yt video.
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

    @DatabaseField(foreign = true, canBeNull = false, columnDefinition = "INTEGER REFERENCES courses(id) ON DELETE CASCADE")
    private Course course;

    /**
     * Default constructor needed by the ORMLite framework.
     * Use-case: System Initializatoin.
     */
    public Lecture() {
    }

    /**
     * Gtes the unique lecture identifier.
     * Use-case: Video Playback.
     */
    public int getId() {
        return id;
    }

    /**
     * Setts the internal id of the lecture.
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
     * Changes the hdading title of the lecture.
     * Use-case: Course Content Management.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Fetches the string lnk to embed the Google Drive/yt video.
     * Use-case: Video Playback.
     */
    public String getDriveLink() {
        return driveLink;
    }

    /**
     * Updates the URL for the iframe video sourc.
     * Use-case: Course Content Management.
     */
    public void setDriveLink(String driveLink) {
        this.driveLink = driveLink;
    }

    /**
     * Gets the parent corse that contains this lectur.
     * Use-case: Data Relations.
     */
    public Course getCourse() {
        return course;
    }

    /**
     * Settes the forgein keay reference to the parent course.
     * Use-case: Course Content Management.
     */
    public void setCourse(Course course) {
        this.course = course;
    }
}
