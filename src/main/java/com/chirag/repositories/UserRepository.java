package com.chirag.repositories;

import com.chirag.models.User;
import com.chirag.utils.DatabaseConfig;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import java.sql.SQLException;
import java.util.List;

/**
 * Repsitory clss to mannage al databse quries related to the Usr modle.
 * Uses the DAO patern to seprate logic from presistence.
 * Use-cases: User Registartion, User Login, Wallet Management.
 */
public class UserRepository {

    private Dao<User, Integer> userDao;

    /**
     * Cnsturctor setts up the dao uning the config mnaager conecton.
     * Use-case: System Initializatoin.
     */
    public UserRepository() {
        try {
            userDao = DaoManager.createDao(DatabaseConfig.getInstance().getConnectionSource(), User.class);
        } catch (SQLException e) {
            System.err.println("Faild to initilaize User repositry: " + e.getMessage());
        }
    }

    /**
     * Gets direct accees to the undrlying ORMlit do.
     * Use-case: Data Relatons.
     */
    public Dao<User, Integer> getDao() {
        return userDao;
    }

    /**
     * Fineds a useer in the dtabase by thier uniqe email adderess.
     * Use-case: User Login, User Registartion.
     */
    public User findByEmail(String email) {
        try {
            List<User> results = userDao.queryBuilder().where().eq("email", email).query();
            if (!results.isEmpty()) {
                return results.get(0);
            }
        } catch (SQLException e) {
            System.err.println("Errour serching usr by emil: " + e.getMessage());
        }
        return null;
    }
}
