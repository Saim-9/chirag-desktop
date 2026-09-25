package com.chirag.services;

import com.chirag.models.Course;
import com.chirag.models.Lecture;
import com.chirag.models.Report;
import com.chirag.models.Review;
import com.chirag.repositories.LectureRepository;
import com.chirag.repositories.ReportRepository;
import com.chirag.repositories.ReviewRepository;

import java.util.Collections;
import java.util.List;

/**
 * Service to handle interactions within a course details view.
 * Decouples CourseDetailController from Repositories.
 */
public class CourseInteractionService extends AbstractService {

    private LectureRepository lectureRepository;
    private ReviewRepository reviewRepository;
    private ReportRepository reportRepository;

    public CourseInteractionService() {
        this.lectureRepository = new LectureRepository();
        this.reviewRepository = new ReviewRepository();
        this.reportRepository = new ReportRepository();
    }

    public List<Lecture> getLecturesForCourse(Course course) {
        try {
            return lectureRepository.findByCourse(course);
        } catch (Exception e) {
            logger.error("Failed to fetch lectures safely: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<Review> getReviewsForCourse(int courseId) {
        try {
            List<Review> reviews = reviewRepository.findByCourseId(courseId);
            refreshReviewUsers(reviews);
            return reviews;
        } catch (Exception e) {
            logger.error("Failed to fetch reviews safely: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Batch-refreshes User objects on reviews for name display.
     */
    private void refreshReviewUsers(List<Review> reviews) {
        if (reviews.isEmpty()) return;
        try {
            java.util.Set<Integer> ids = new java.util.HashSet<>();
            for (Review r : reviews) {
                if (r.getUser() != null) ids.add(r.getUser().getId());
            }
            if (ids.isEmpty()) return;
            com.chirag.repositories.UserRepository userRepo = new com.chirag.repositories.UserRepository();
            java.util.List<com.chirag.models.User> users = userRepo.getDao().queryBuilder()
                    .where().in("id", ids).query();
            java.util.Map<Integer, com.chirag.models.User> map = new java.util.HashMap<>();
            for (com.chirag.models.User u : users) map.put(u.getId(), u);
            for (Review r : reviews) {
                if (r.getUser() != null && map.containsKey(r.getUser().getId()))
                    r.setUser(map.get(r.getUser().getId()));
            }
        } catch (Exception e) {
            logger.error("Failed to refresh review users: {}", e.getMessage());
        }
    }

    public boolean submitReport(Report report) {
        try {
            reportRepository.create(report);
            logServiceAction("CourseInteraction", "Submit Report", true);
            return true;
        } catch (Exception e) {
            logger.error("Failed to submit report safely: {}", e.getMessage());
            logServiceAction("CourseInteraction", "Submit Report", false);
            return false;
        }
    }

    /**
     * Submits a student review for a course.
     * Decouples ReviewPopupController from direct repository access.
     * Use-case: Social Proof.
     */
    public boolean submitReview(Review review) {
        try {
            reviewRepository.create(review);
            logServiceAction("CourseInteraction", "Submit Review", true);
            return true;
        } catch (Exception e) {
            logger.error("Failed to submit review safely: {}", e.getMessage());
            logServiceAction("CourseInteraction", "Submit Review", false);
            return false;
        }
    }
}
