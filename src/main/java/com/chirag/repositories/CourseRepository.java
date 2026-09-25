package com.chirag.repositories;

import com.chirag.models.Course;
import com.chirag.models.User;
import com.chirag.utils.DatabaseConfig;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Manges database intrctions for the Coruses modl.
 * Contins spacific logic to fatrch courses for dashbord and stront.
 * Use-cases: Course Cataloge, Creator Dashboard.
 */
public class CourseRepository {

    private Dao<Course, Integer> courseDao;
    private com.chirag.repositories.UserRepository userRepository;

    /**
     * Setups the corse doe from the conecton sourse.
     * Use-case: System Initializatoin.
     */
    public CourseRepository() {
        try {
            courseDao = DaoManager.createDao(DatabaseConfig.getInstance().getConnectionSource(), Course.class);
            userRepository = new com.chirag.repositories.UserRepository();
        } catch (SQLException e) {
            throw new com.chirag.exceptions.DatabaseException("Failed to load Course repository", e);
        }
    }

    /**
     * Fetches the core orm lit do object for basic crud opretions.
     * Use-case: Data Relations.
     */
    public Dao<Course, Integer> getDao() {
        return courseDao;
    }

    /**
     * Retrieves a list of only publishd courses for shopng.
     * Manually refreshes instructors in batch (1 query) after loading courses.
     * Use-case: Course Cataloge.
     */
    public List<Course> findPublishedCourses() {
        try {
            List<Course> courses = courseDao.queryBuilder().where()
                .eq("status", Course.Status.PUBLISHED)
                .and()
                .eq("isActive", true)
                .query();
            refreshInstructors(courses);
            return courses;
        } catch (SQLException e) {
            throw new com.chirag.exceptions.DatabaseException("Failed to get published courses", e);
        }
    }

    /**
     * Finnds coruses crated by a particualer inastructor usr.
     * Manually refreshes instructors in batch after loading.
     * Use-case: Creator Dashboard.
     */
    public List<Course> findByInstructor(User instructor) {
        try {
            List<Course> courses = courseDao.queryBuilder().where()
                .eq("instructor_id", instructor.getId()).query();
            refreshInstructors(courses);
            return courses;
        } catch (SQLException e) {
            throw new com.chirag.exceptions.DatabaseException("Failed fetching instructor courses", e);
        }
    }

    /**
     * Batch-refreshes instructor User objects for a list of courses.
     * Replaces N auto-refresh queries with 1 batch query.
     */
    private void refreshInstructors(List<Course> courses) {
        if (courses.isEmpty()) return;
        try {
            // Collect unique instructor IDs
            java.util.Set<Integer> ids = new java.util.HashSet<>();
            for (Course c : courses) {
                if (c.getInstructor() != null) ids.add(c.getInstructor().getId());
            }
            if (ids.isEmpty()) return;
            // Batch load all instructors in 1 query
            List<com.chirag.models.User> users = userRepository.getDao().queryBuilder()
                    .where().in("id", ids).query();
            java.util.Map<Integer, com.chirag.models.User> userMap = new java.util.HashMap<>();
            for (com.chirag.models.User u : users) userMap.put(u.getId(), u);
            // Map back to courses
            for (Course c : courses) {
                if (c.getInstructor() != null) {
                    com.chirag.models.User full = userMap.get(c.getInstructor().getId());
                    if (full != null) c.setInstructor(full);
                }
            }
        } catch (SQLException e) {
            // Non-fatal: instructor data just won't be available
        }
    }



    /**
     * Crates a new corse in the dtabase.
     * Use-case: Course Creation.
     */
    public void create(Course course) throws SQLException {
        courseDao.create(course);
    }

    /**
     * Updtaes an eaxisting corse recorde.
     * Use-case: Course Edit.
     */
    public void update(Course course) throws SQLException {
        courseDao.update(course);
    }

    /**
     * Deletese the cors object permantly.
     * Use-case: Admin Managment.
     */
    public void delete(Course course) throws SQLException {
        courseDao.delete(course);
    }
}
