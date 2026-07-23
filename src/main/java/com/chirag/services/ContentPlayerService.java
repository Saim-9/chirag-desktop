package com.chirag.services;

import com.chirag.models.Course;
import com.chirag.models.Enrollment;
import com.chirag.models.Lecture;
import com.chirag.models.Review;
import com.chirag.models.User;
import com.chirag.repositories.EnrollmentRepository;
import com.chirag.repositories.LectureRepository;
import com.chirag.repositories.ReviewRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service layer for course player operations.
 * Decouples CoursePlayerController from direct repository access.
 * Handles curriculum fetching, enrollment lookup, and progress tracking.
 * Use-cases: Consume Content, Track Progress.
 */
public class ContentPlayerService extends AbstractService {

    private LectureRepository lectureRepository;
    private EnrollmentRepository enrollmentRepository;
    private ReviewRepository reviewRepository;

    /**
     * Sets up repostries for lacture and enrllment data.
     * Use-case: System Initializatoin.
     */
    public ContentPlayerService() {
        this.lectureRepository = new LectureRepository();
        this.enrollmentRepository = new EnrollmentRepository();
        this.reviewRepository = new ReviewRepository();
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
            List<Review> reviews = reviewRepository.findByCourseId(courseId);
            if (reviews.isEmpty()) return 0.0;
            double sum = 0;
            for (Review r : reviews) {
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
            return reviewRepository.findByCourseId(courseId).size();
        } catch (Exception e) {
            logger.error("Failed to count reviews: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Batch-fetches ratings for multiple courses in a single DB query.
     * Returns a map of courseId -> [averageRating, reviewCount].
     * This replaces N individual queries with 1 query for the marketplace.
     * Use-case: Course Catalogue (performance optimization).
     */
    public Map<Integer, double[]> getRatingsMap(List<Integer> courseIds) {
        Map<Integer, double[]> result = new HashMap<>();
        // Default all to zero
        for (int id : courseIds) {
            result.put(id, new double[]{0.0, 0});
        }
        try {
            List<Review> allReviews = reviewRepository.findByCourseIds(courseIds);
            // Group reviews by course ID
            Map<Integer, List<Review>> grouped = new HashMap<>();
            for (Review r : allReviews) {
                int cId = r.getCourse().getId();
                grouped.computeIfAbsent(cId, k -> new ArrayList<>()).add(r);
            }
            // Calculate averages
            for (Map.Entry<Integer, List<Review>> entry : grouped.entrySet()) {
                List<Review> reviews = entry.getValue();
                double sum = 0;
                for (Review r : reviews) sum += r.getRating();
                result.put(entry.getKey(), new double[]{sum / reviews.size(), reviews.size()});
            }
        } catch (Exception e) {
            logger.error("Failed to batch-fetch ratings: {}", e.getMessage());
        }
        return result;
    }
}
