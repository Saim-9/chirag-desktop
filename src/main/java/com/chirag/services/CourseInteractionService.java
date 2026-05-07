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
            System.err.println("Failed to fetch lectures safely: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<Review> getReviewsForCourse(int courseId) {
        try {
            return reviewRepository.findByCourseId(courseId);
        } catch (Exception e) {
            System.err.println("Failed to fetch reviews safely: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean submitReport(Report report) {
        try {
            reportRepository.create(report);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to submit report safely: " + e.getMessage());
            return false;
        }
    }
}
