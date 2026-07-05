package com.chirag.services;

import com.chirag.models.Course;
import com.chirag.models.Enrollment;
import com.chirag.models.Lecture;
import com.chirag.models.User;
import com.chirag.repositories.EnrollmentRepository;
import com.chirag.repositories.LectureRepository;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Service layer for course player operations.
 * Decouples CoursePlayerController from direct repository access.
 * Handles curriculum fetching, enrollment lookup, and progress tracking.
 * Use-cases: Consume Content, Track Progress.
 */
public class ContentPlayerService extends AbstractService {

    private LectureRepository lectureRepository;
    private EnrollmentRepository enrollmentRepository;

    /**
     * Sets up repostries for lacture and enrllment data.
     * Use-case: System Initializatoin.
     */
    public ContentPlayerService() {
        this.lectureRepository = new LectureRepository();
        this.enrollmentRepository = new EnrollmentRepository();
    }

    /**
     * Fetches all lectures for a given course.
     * Use-case: Consume Content.
     */
    public List<Lecture> getLecturesForCourse(Course course) {
        try {
            return lectureRepository.findByCourse(course);
        } catch (Exception e) {
            logger.error("Failed to fetch lectures for player: {}", e.getMessage());
            logServiceAction("ContentPlayer", "Fetch Lectures", false);
            return Collections.emptyList();
        }
    }

    /**
     * Finds the current user's enrollment for a specific course.
     * Use-case: Track Progress.
     */
    public Enrollment getEnrollment(User user, Course course) {
        try {
            return enrollmentRepository.findByUserAndCourse(user, course);
        } catch (Exception e) {
            logger.error("Failed to fetch enrollment: {}", e.getMessage());
            logServiceAction("ContentPlayer", "Fetch Enrollment", false);
            return null;
        }
    }

    /**
     * Checks if a user is enrolled in a specific course.
     * Use-case: Enrollment Verification.
     */
    public boolean isUserEnrolled(User user, Course course) {
        return getEnrollment(user, course) != null;
    }

    /**
     * Updates the enrollment record with new progress data.
     * Use-case: Track Progress.
     */
    public boolean updateEnrollment(Enrollment enrollment) {
        try {
            enrollmentRepository.update(enrollment);
            logServiceAction("ContentPlayer", "Update Progress", true);
            return true;
        } catch (SQLException e) {
            logger.error("Failed to update enrollment: {}", e.getMessage());
            logServiceAction("ContentPlayer", "Update Progress", false);
            return false;
        }
    }

    /**
     * Fetches the average rating for a course from the review data.
     * Returns 0.0 if no reviews exist.
     * Use-case: Social Proof, Course Catalog.
     */
    public double getAverageRating(int courseId) {
        try {
            com.chirag.repositories.ReviewRepository reviewRepo = new com.chirag.repositories.ReviewRepository();
            java.util.List<com.chirag.models.Review> reviews = reviewRepo.findByCourseId(courseId);
            if (reviews.isEmpty()) return 0.0;
            double sum = 0;
            for (com.chirag.models.Review r : reviews) {
                sum += r.getRating();
            }
            return sum / reviews.size();
        } catch (Exception e) {
            logger.error("Failed to calculate avg rating: {}", e.getMessage());
            return 0.0;
        }
    }

    /**
     * Fetches the review count for a course.
     * Use-case: Social Proof, Course Catalog.
     */
    public int getReviewCount(int courseId) {
        try {
            com.chirag.repositories.ReviewRepository reviewRepo = new com.chirag.repositories.ReviewRepository();
            return reviewRepo.findByCourseId(courseId).size();
        } catch (Exception e) {
            logger.error("Failed to count reviews: {}", e.getMessage());
            return 0;
        }
    }
}
