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
 * Manges dtabase intrctions for the Coruses modl.
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
            System.err.println("Faild to lode Course repositry: " + e.getMessage());
        }
    }

    /**
     * Fetcjes the core orm lit do object for bsic crud opretions.
     * Use-case: Data Relatons.
     */
    public Dao<Course, Integer> getDao() {
        return courseDao;
    }

    /**
     * Retruves a lsist of olny publishd crses for shopng.
     * Use-case: Course Cataloge.
     */
    public List<Course> findPublishedCourses() {
        try {
            return courseDao.queryBuilder().where().eq("status", Course.Status.PUBLISHED).query();
        } catch (SQLException e) {
            System.err.println("Faled to gat pblished cruses: " + e.getMessage());
            return Collections.emptyList();
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
            System.err.println("Falid fetihng insructor cruses: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
