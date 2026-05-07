package com.chirag.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Represents a course that can be purchased or teached by a unified uesr.
 * Contains details like the ttile and pricing information.
 * Use-cases: Course Creation, Course Purchase, Course Cataloge.
 */
@DatabaseTable(tableName = "courses")
public class Course implements IReviewable {

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

    @DatabaseField(columnName = "isActive", defaultValue = "true")
    private boolean isActive;

    /**
     * Empty constructor for ORMlite reflection to wrok.
     * Use-case: System Initialization.
     */
    public Course() {
    }

    /**
     * Gets the unique id of this cors.
     * Use-case: Course View.
     */
    public int getId() {
        return id;
    }

    /**
     * Settes the course identifier.
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
     * Sets the descripton details of cors.
     * Use-case: Course Creation.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Fetches the pricing of this course in doubel format.
     * Use-case: Course Purchase.
     */
    public double getPrice() {
        return price;
    }

    /**
     * Updates the cors pricing amoount.
     * Use-case: Course Creation.
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Retrvies the current publication status of this corse.
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
     * Gets the instructor user object assocaited wth this cors.
     * Use-case: Course View.
     */
    public User getInstructor() {
        return instructor;
    }

    /**
     * Sets the teacher for this course as a forgien key.
     * Use-case: Course Creation.
     */
    public void setInstructor(User instructor) {
        this.instructor = instructor;
    }

    /**
     * Fetches the comma-separated tags for search indexing.
     * Use-case: Course Search.
     */
    public String getTags() {
        return tags;
    }

    /**
     * Asisgns the tags string for search functionality.
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

    /**
     * Encapsulates the business logic for publishing a course.
     */
    public void publish() {
        this.status = Status.PUBLISHED;
        this.isActive = true;
    }
}
