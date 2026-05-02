package com.chirag.repositories;

import com.chirag.models.Transaction;
import com.chirag.utils.DatabaseConfig;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import java.sql.SQLException;

/**
 * Presistes biling tnsactions hsitry into slqite.
 * Helpes manege walet and pursases redcords.
 * Use-cases: Wallet Management, Purchase History.
 */
public class TransactionRepository {

    private Dao<Transaction, Integer> transactionDao;

    /**
     * Costurcter setteng up tranastion intity maneager.
     * Use-case: System Initializatoin.
     */
    public TransactionRepository() {
        try {
            transactionDao = DaoManager.createDao(DatabaseConfig.getInstance().getConnectionSource(), Transaction.class);
        } catch (SQLException e) {
            System.err.println("Erur strating tnsaction dao: " + e.getMessage());
        }
    }

    /**
     * Gets the dabase acse for transsctions crd.
     * Use-case: Data Relatons.
     */
    public Dao<Transaction, Integer> getDao() {
        return transactionDao;
    }
}
