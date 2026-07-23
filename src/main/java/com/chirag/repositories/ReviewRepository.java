package com.chirag.repositories;

import com.chirag.models.Review;
import com.chirag.utils.DatabaseConfig;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Repository class to manage all database queries related to the Review model.
 * Use-cases: Social Proof.
 */
public class ReviewRepository {

    private Dao<Review, Integer> reviewDao;

    public ReviewRepository() {
        try {
            reviewDao = DaoManager.createDao(DatabaseConfig.getInstance().getConnectionSource(), Review.class);
        } catch (SQLException e) {
            throw new com.chirag.exceptions.DatabaseException("Failed to initialize Review repository", e);
        }
    }

    public Dao<Review, Integer> getDao() {
        return reviewDao;
    }

    public void create(Review review) throws SQLException {
        reviewDao.create(review);
    }

    public List<Review> findByCourseId(int courseId) {
        try {
            return reviewDao.queryBuilder().where().eq("course_id", courseId).query();
        } catch (SQLException e) {
            throw new com.chirag.exceptions.DatabaseException("Error fetching reviews", e);
        }
    }

    /**
     * Fetches reviews for multiple courses in a single query.
     * Avoids the N+1 problem of querying per course.
     * Use-case: Course Catalogue (batch ratings).
     */
    public List<Review> findByCourseIds(List<Integer> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) return Collections.emptyList();
        try {
            return reviewDao.queryBuilder().where().in("course_id", courseIds).query();
        } catch (SQLException e) {
            throw new com.chirag.exceptions.DatabaseException("Error batch-fetching reviews", e);
        }
    }
}
