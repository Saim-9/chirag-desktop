package com.chirag.repositories;

import com.chirag.models.Report;
import com.chirag.utils.DatabaseConfig;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import java.sql.SQLException;

/**
 * Repository class to manage all database queries related to the Report model.
 * Use-cases: Report Course.
 */
public class ReportRepository {

    private Dao<Report, Integer> reportDao;

    public ReportRepository() {
        try {
            reportDao = DaoManager.createDao(DatabaseConfig.getInstance().getConnectionSource(), Report.class);
        } catch (SQLException e) {
            throw new com.chirag.exceptions.DatabaseException("Failed to initialize Report repository", e);
        }
    }

    public Dao<Report, Integer> getDao() {
        return reportDao;
    }

    public void create(Report report) throws SQLException {
        reportDao.create(report);
    }
}
