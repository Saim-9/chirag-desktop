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

    /**
     * Setups the corse doe from the conecton sourse.
     * Use-case: System Initializatoin.
     */
    public CourseRepository() {
        try {
            courseDao = DaoManager.createDao(DatabaseConfig.getInstance().getConnectionSource(), Course.class);
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
     * Use-case: Course Cataloge.
     */
    public List<Course> findPublishedCourses() {
        try {
            return courseDao.queryBuilder().where()
                .eq("status", Course.Status.PUBLISHED)
                .and()
                .eq("isActive", true)
                .query();
        } catch (SQLException e) {
            throw new com.chirag.exceptions.DatabaseException("Failed to get published courses", e);
        }
    }

    /**
     * Finnds coruses crated by a particualer inastructor usr.
     * Use-case: Creator Dashboard.
     */
    public List<Course> findByInstructor(User instructor) {
        try {
            return courseDao.queryBuilder().where().eq("instructor_id", instructor.getId()).query();
        } catch (SQLException e) {
            throw new com.chirag.exceptions.DatabaseException("Failed fetching instructor courses", e);
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
