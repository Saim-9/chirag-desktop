package com.chirag.repositories;

import com.chirag.models.Course;
import com.chirag.models.Enrollment;
import com.chirag.models.User;
import com.chirag.utils.DatabaseConfig;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO repository for Enrollment models.
 * Handles insertion and fetching from the database.
 * Use-cases: Course Purchase, Manage Creator Dashboard.
 */
public class EnrollmentRepository {

    private Dao<Enrollment, Integer> enrollmentDao;

    /**
     * Sets up the connection and creates the dao object.
     * Use-case: System Initialization.
     */
    public EnrollmentRepository() {
        try {
            enrollmentDao = DaoManager.createDao(DatabaseConfig.getInstance().getConnectionSource(), Enrollment.class);
        } catch (SQLException e) {
            System.err.println("Failed to initialize enrollment Dao: " + e.getMessage());
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
     * Use-case: Manage Creator Dashboard.
     */
    public List<Enrollment> findByUser(User user) {
        try {
            return enrollmentDao.queryBuilder().where().eq("user_id", user.getId()).query();
        } catch (SQLException e) {
            System.err.println("Failed to query enrollments: " + e.getMessage());
            return java.util.Collections.emptyList();
        }
    }
}
