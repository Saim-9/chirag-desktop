package com.chirag.services;

import com.chirag.models.Course;
import com.chirag.models.Enrollment;
import com.chirag.models.Transaction;
import com.chirag.models.User;
import com.chirag.repositories.CourseRepository;
import com.chirag.repositories.EnrollmentRepository;
import com.chirag.repositories.LectureRepository;
import com.chirag.repositories.TransactionRepository;
import java.util.Collections;
import java.util.List;

/**
 * Facade service for dashboard data aggregation.
 * Handles fetching courses, enrollments, and transactions, decoupling the UI from DAOs.
 */
public class DashboardService extends AbstractService {

    private CourseRepository courseRepository;
    private EnrollmentRepository enrollmentRepository;
    private TransactionRepository transactionRepository;
    private LectureRepository lectureRepository;

    public DashboardService() {
        this.courseRepository = new CourseRepository();
        this.enrollmentRepository = new EnrollmentRepository();
        this.transactionRepository = new TransactionRepository();
        this.lectureRepository = new LectureRepository();
    }

    public List<Course> getInstructorCourses(User instructor) {
        try {
            return courseRepository.findByInstructor(instructor);
        } catch (com.chirag.exceptions.DatabaseException e) {
            logger.error("Failed to fetch instructor courses: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<Enrollment> getEnrolledCourses(User user) {
        return enrollmentRepository.findByUser(user);
    }

    public int getTotalLecturesForCourse(int courseId) {
        try {
            return lectureRepository.getDao().queryBuilder()
                    .where().eq("course_id", courseId).query().size();
        } catch (Exception e) {
            logger.error("Error fetching total lectures: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Batch-fetches lecture counts for multiple courses in a single query.
     * Returns a map of courseId -> lectureCount.
     * Use-case: Dashboard (performance optimization).
     */
    public java.util.Map<Integer, Integer> getLectureCountsMap(java.util.List<Integer> courseIds) {
        java.util.Map<Integer, Integer> result = new java.util.HashMap<>();
        for (int id : courseIds) {
            result.put(id, 0);
        }
        if (courseIds.isEmpty()) return result;
        try {
            java.util.List<com.chirag.models.Lecture> allLectures = lectureRepository.getDao()
                    .queryBuilder().where().in("course_id", courseIds).query();
            for (com.chirag.models.Lecture l : allLectures) {
                int cId = l.getCourse().getId();
                result.merge(cId, 1, Integer::sum);
            }
        } catch (Exception e) {
            logger.error("Error batch-fetching lecture counts: {}", e.getMessage());
        }
        return result;
    }

    public List<Transaction> getRecentTransactions(User user, int limit) {
        try {
            com.j256.ormlite.stmt.QueryBuilder<Transaction, Integer> qb = transactionRepository.getDao().queryBuilder();
            qb.orderBy("transactionDate", false);
            qb.where().eq("buyer_id", user.getId()).or().eq("instructor_id", user.getId());

            List<Transaction> transactions = qb.query();
            return transactions.subList(0, Math.min(limit, transactions.size()));
        } catch (Exception e) {
            logger.error("Failed to load transactions: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
