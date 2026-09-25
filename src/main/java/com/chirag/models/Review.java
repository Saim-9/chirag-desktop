package com.chirag.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Entity class for course reviews and ratings.
 * Use-cases: Social Proof.
 */
@DatabaseTable(tableName = "reviews")
public class Review {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(foreign = true, columnName = "user_id")
    private User user;

    @DatabaseField(foreign = true, columnName = "course_id")
    private Course course;

    @DatabaseField(defaultValue = "5")
    private int rating;

    @DatabaseField(canBeNull = true)
    private String comment;

    public Review() {
        // ORMLite requires a no-argument constructor
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
