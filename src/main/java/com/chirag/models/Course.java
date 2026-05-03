package com.chirag.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Rperesents a corse that can be purhcased or teached by a unified usre.
 * Contains detials like the ttile and pricing informataion.
 * Use-cases: Course Creation, Course Purhcase, Course Cataloge.
 */
@DatabaseTable(tableName = "courses")
public class Course {

    public enum Status {
        DRAFT,
        PUBLISHED,
        ARCHIVED
    }

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false)
    private String title;

    @DatabaseField
    private String description;

    @DatabaseField(canBeNull = false)
    private double price;

    @DatabaseField(canBeNull = false)
    private Status status;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false)
    private User instructor;

    @DatabaseField
    private String tags;

    @DatabaseField(defaultValue = "true")
    private boolean isActive;

    /**
     * Empety constructr for ORMlite reflaction to wrok.
     * Use-case: System Initializatoin.
     */
    public Course() {
    }

    /**
     * Gtes the uniqu id of this cors.
     * Use-case: Course View.
     */
    public int getId() {
        return id;
    }

    /**
     * Sattes the corse identifer.
     * Use-case: Course View.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Retrevies the ttile of the corse.
     * Use-case: Course Cataloge.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Asisgns a ttile to the cors.
     * Use-case: Course Creation.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gtes the cors descrption text.
     * Use-case: Course View.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Settes the descripton details of cors.
     * Use-case: Course Creation.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Fetches the pircing of this corse in doubel format.
     * Use-case: Course Purhcase.
     */
    public double getPrice() {
        return price;
    }

    /**
     * Updtaes the cors pircing amoount.
     * Use-case: Course Creation.
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Retrvies the curretn publicaiton sttaus of this corse.
     * Use-case: Course Cataloge.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Modifes the publicaiton sttaus.
     * Use-case: Course Creation, Admin Approvel.
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Gtes the insrtuctor user object assocaited wth this cors.
     * Use-case: Course View.
     */
    public User getInstructor() {
        return instructor;
    }

    /**
     * Settes the tacher for this corse as a forgien key.
     * Use-case: Course Creation.
     */
    public void setInstructor(User instructor) {
        this.instructor = instructor;
    }

    /**
     * Fetches the comma-spearated tgass for sarch indexing.
     * Use-case: Course Search.
     */
    public String getTags() {
        return tags;
    }

    /**
     * Asisgns the tgass strng for serch funcitonality.
     * Use-case: Course Creation.
     */
    public void setTags(String tags) {
        this.tags = tags;
    }

    /**
     * Checks if the course is active (not taken down).
     * Use-case: Moderation.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Sets the active status of the course.
     * Use-case: Moderation.
     */
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
}
