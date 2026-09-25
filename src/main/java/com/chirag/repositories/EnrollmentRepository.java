package com.chirag.repositories;

import com.chirag.models.Course;
import com.chirag.models.Enrollment;
import com.chirag.models.User;
import com.chirag.utils.DatabaseConfig;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DAO repository for Enrollment models.
 * Handles insertion and fetching from the database.
 * Use-cases: Course Purchase, Manage Creator Dashboard.
 */
public class EnrollmentRepository {

    private static final Logger logger = LoggerFactory.getLogger(EnrollmentRepository.class);

    private Dao<Enrollment, Integer> enrollmentDao;

    /**
     * Sets up the connection and creates the dao object.
     * Use-case: System Initialization.
     */
    public EnrollmentRepository() {
        try {
            enrollmentDao = DaoManager.createDao(DatabaseConfig.getInstance().getConnectionSource(), Enrollment.class);
        } catch (SQLException e) {
            throw new com.chirag.exceptions.DatabaseException("Failed to initialize enrollment Dao", e);
        }
    }

    /**
     * Exposes the direct DAO for advanced queries.
     * Use-case: Consume Content.
     */
    public Dao<Enrollment, Integer> getDao() {
        return enrollmentDao;
    }

    /**
     * Saves a new enrollment record.
     * Use-case: Course Purchase.
     */
    public void create(Enrollment enrollment) throws SQLException {
        enrollmentDao.create(enrollment);
    }

    /**
     * Saves an updated enrollment record.
     * Use-case: Consume Content.
     */
    public void update(Enrollment enrollment) throws SQLException {
        enrollmentDao.update(enrollment);
    }

    /**
     * Fetches all courses for a given user.
     * Manually refreshes Course (and its instructor) in batch.
     * Use-case: Manage Creator Dashboard.
     */
    public List<Enrollment> findByUser(User user) {
        try {
            List<Enrollment> enrollments = enrollmentDao.queryBuilder()
                    .where().eq("user_id", user.getId()).query();
            refreshCourses(enrollments);
            return enrollments;
        } catch (SQLException e) {
            logger.error("Failed to query enrollments: {}", e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Batch-refreshes Course objects (and their instructors) for enrollments.
     * 2 batch queries replace N×3 auto-refresh queries.
     */
    private void refreshCourses(List<Enrollment> enrollments) {
        if (enrollments.isEmpty()) return;
        try {
            // Collect unique course IDs
            java.util.Set<Integer> courseIds = new java.util.HashSet<>();
            for (Enrollment e : enrollments) {
                if (e.getCourse() != null) courseIds.add(e.getCourse().getId());
            }
            if (courseIds.isEmpty()) return;

            // Batch load all courses in 1 query
            Dao<Course, Integer> courseDao = DaoManager.createDao(
                    DatabaseConfig.getInstance().getConnectionSource(), Course.class);
            List<Course> courses = courseDao.queryBuilder()
                    .where().in("id", courseIds).query();
            java.util.Map<Integer, Course> courseMap = new java.util.HashMap<>();
            for (Course c : courses) courseMap.put(c.getId(), c);

            // Batch load all instructors for those courses in 1 query
            java.util.Set<Integer> instructorIds = new java.util.HashSet<>();
            for (Course c : courses) {
                if (c.getInstructor() != null) instructorIds.add(c.getInstructor().getId());
            }
            java.util.Map<Integer, User> userMap = new java.util.HashMap<>();
            if (!instructorIds.isEmpty()) {
                Dao<User, Integer> userDao = DaoManager.createDao(
                        DatabaseConfig.getInstance().getConnectionSource(), User.class);
                List<User> users = userDao.queryBuilder()
                        .where().in("id", instructorIds).query();
                for (User u : users) userMap.put(u.getId(), u);
            }

            // Wire instructor into course, course into enrollment
            for (Course c : courses) {
                if (c.getInstructor() != null) {
                    User full = userMap.get(c.getInstructor().getId());
                    if (full != null) c.setInstructor(full);
                }
            }
            for (Enrollment e : enrollments) {
                if (e.getCourse() != null) {
                    Course full = courseMap.get(e.getCourse().getId());
                    if (full != null) e.setCourse(full);
                }
            }
        } catch (SQLException ex) {
            logger.error("Failed to refresh enrollment courses: {}", ex.getMessage());
        }
    }

    /**
     * Checks if a user is already enrolled in a specifc course.
     * Returns the Enrollment if found, null othrwise.
     * Use-case: Course Purchase (duplicate guard).
     */
    public Enrollment findByUserAndCourse(User user, Course course) {
        try {
            return enrollmentDao.queryBuilder().where()
                    .eq("user_id", user.getId())
                    .and()
                    .eq("course_id", course.getId())
                    .queryForFirst();
        } catch (SQLException e) {
            logger.error("Failed to check existing enrollment: {}", e.getMessage());
            return null;
        }
    }
}
