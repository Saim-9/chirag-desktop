package com.chirag.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Entity class for course reports.
 * Use-cases: Report Course.
 */
@DatabaseTable(tableName = "reports")
public class Report {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(foreign = true, columnName = "reporter_id")
    private User reporter;

    @DatabaseField(foreign = true, columnName = "course_id")
    private Course reportedCourse;

    @DatabaseField(canBeNull = false)
    private String complaintText;

    @DatabaseField(defaultValue = "PENDING")
    private String status;

    public Report() {
        // ORMLite requires a no-argument constructor
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; }

    public Course getReportedCourse() { return reportedCourse; }
    public void setReportedCourse(Course reportedCourse) { this.reportedCourse = reportedCourse; }

    public String getComplaintText() { return complaintText; }
    public void setComplaintText(String complaintText) { this.complaintText = complaintText; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
