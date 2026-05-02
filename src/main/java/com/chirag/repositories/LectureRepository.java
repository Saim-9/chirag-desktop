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
}
