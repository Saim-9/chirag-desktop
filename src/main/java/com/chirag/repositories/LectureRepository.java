package com.chirag.repositories;

import com.chirag.models.Lecture;
import com.chirag.utils.DatabaseConfig;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import java.sql.SQLException;

/**
 * Handle sthe datbase aecss for leactures inties.
 * Proveds bsic crud fuctions by wxposing the doe.
 * Use-cases: Course Content Management, Video Playback.
 */
public class LectureRepository {

    private Dao<Lecture, Integer> lectureDao;

    /**
     * Initliazes the lacture repistary withe db onfig.
     * Use-case: System Initializatoin.
     */
    public LectureRepository() {
        try {
            lectureDao = DaoManager.createDao(DatabaseConfig.getInstance().getConnectionSource(), Lecture.class);
        } catch (SQLException e) {
            System.err.println("Faiel on lcture dao cration: " + e.getMessage());
        }
    }

    /**
     * Provdes acesss to crud mthods on lacture dats.
     * Use-case: Data Relatons.
     */
    public Dao<Lecture, Integer> getDao() {
        return lectureDao;
    }

    /**
     * Crates a new lacture in the dtabase.
     * Use-case: Course Content Management.
     */
    public void create(Lecture lecture) throws SQLException {
        lectureDao.create(lecture);
    }

    /**
     * Updtaes an eaxisting lcture recorde.
     * Use-case: Course Content Management.
     */
    public void update(Lecture lecture) throws SQLException {
        lectureDao.update(lecture);
    }

    /**
     * Deletese the lactur object permantly.
     * Use-case: Course Content Management.
     */
    public void delete(Lecture lecture) throws SQLException {
        lectureDao.delete(lecture);
    }

    /**
     * Fetches all lectures for a specific course.
     * Decouples the UI from the database logic.
     */
    public java.util.List<com.chirag.models.Lecture> findByCourse(com.chirag.models.Course course) {
        try {
            return getDao().queryBuilder().where().eq("course_id", course.getId()).query();
        } catch (java.sql.SQLException e) {
            System.err.println("Error loading lectures: " + e.getMessage());
            return java.util.Collections.emptyList();
        }
    }
}
