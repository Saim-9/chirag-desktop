package com.chirag.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Entity class for the enrollment table.
 * Manages the link between students and courses.
 * Use-cases: Course Purchase, Course View, Consume Content.
 */
@DatabaseTable(tableName = "enrollments")
public class Enrollment {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(foreign = true, columnName = "user_id")
    private User user;

    @DatabaseField(foreign = true, columnName = "course_id")
    private Course course;

    @DatabaseField(defaultValue = "false")
    private boolean isCompleted;

    @DatabaseField(defaultValue = "")
    private String completedLectureIds;

    public Enrollment() {
        // ORMLite requires a no-argument constructor
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public String getCompletedLectureIds() {
        return completedLectureIds != null ? completedLectureIds : "";
    }

    public void setCompletedLectureIds(String completedLectureIds) {
        this.completedLectureIds = completedLectureIds;
    }

    /**
     * Calculates the progress percentage for this enrollment.
     * Use-case: Consume Content.
     */
    public double calculateProgressPercentage(int totalLectures) {
        if (isCompleted) {
            return 100.0;
        }
        if (totalLectures <= 0) {
            return 0.0;
        }
        String completedIds = getCompletedLectureIds();
        int completedCount = 0;
        if (completedIds != null && !completedIds.trim().isEmpty()) {
            completedCount = completedIds.split(",").length;
        }
        return ((double) completedCount / totalLectures) * 100.0;
    }
}
