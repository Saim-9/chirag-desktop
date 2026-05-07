package com.chirag.services;

import com.chirag.models.Course;
import com.chirag.repositories.CourseRepository;
import java.sql.SQLException;
import java.util.List;

/**
 * Service layer for course business rules.
 * Handles course publishing and marketplace queries.
 * Demonstrates INHERITANCE by extending AbstractService.
 */
public class CourseService extends AbstractService {

    private CourseRepository courseRepository;

    /**
     * Constructor to initialize the course repository.
     * Use-case: System Initialization.
     */
    public CourseService() {
        this.courseRepository = new CourseRepository();
    }

    /**
     * Publishes the course so students can buy it and saves lectures.
     * Defaults the status to PUBLISHED and saves.
     * Use-case: Course Creation.
     */
    public boolean publishCourse(Course course, List<com.chirag.models.Lecture> lectures) {
        // Use the OOP method
        course.publish();

        try {
            // Save the course first
            courseRepository.create(course);
            com.chirag.repositories.LectureRepository lectureRepo = new com.chirag.repositories.LectureRepository();

            // Save the lectures
            for (com.chirag.models.Lecture lec : lectures) {
                lec.setCourse(course);
                lectureRepo.create(lec);
            }

            // INHERITANCE IN ACTION: Using the protected method from AbstractService
            logServiceAction("Course", "Publish", true);
            return true;

        } catch (SQLException e) {
            System.err.println("Failed to publish course. Executing Rollback... Error: " + e.getMessage());
            // If lectures fail, delete the course so we don't have an empty ghost course.
            try {
                if (course.getId() != 0) {
                    courseRepository.getDao().delete(course);
                }
            } catch (Exception rollbackEx) {
                System.err.println("Critical Error during rollback.");
            }

            // INHERITANCE IN ACTION
            logServiceAction("Course", "Publish Rollback", false);
            return false;
        }
    }

    /**
     * Fetches all published courses to be shown on the marketplace.
     * Use-case: Course Catalog.
     */
    public List<Course> getMarketplaceCourses() {
        return courseRepository.findPublishedCourses();
    }

    /**
     * Updates an existing course's metadata in the database.
     * Uses ORMLite to overwrite the record.
     * Use-case: Edit Course.
     */
    public boolean updateCourse(Course course) {
        try {
            courseRepository.update(course);

            // INHERITANCE IN ACTION
            logServiceAction("Course", "Update", true);
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to update course: " + e.getMessage());

            // INHERITANCE IN ACTION
            logServiceAction("Course", "Update", false);
            return false;
        }
    }
}