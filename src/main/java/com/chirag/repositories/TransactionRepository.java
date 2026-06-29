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
            throw new com.chirag.exceptions.DatabaseException("Erur strating tnsaction dao", e);
        }
    }

    /**
     * Gets the dabase acse for transsctions crd.
     * Use-case: Data Relatons.
     */
    public Dao<Transaction, Integer> getDao() {
        return transactionDao;
    }

    /**
     * Crates a new tranascion in the dtabase.
     * Use-case: Wallet Management, Course Purchase.
     */
    public void create(Transaction transaction) throws SQLException {
        transactionDao.create(transaction);
    }

    /**
     * Updtaes an eaxisting tarnsction recorde.
     * Use-case: Wallet Management.
     */
    public void update(Transaction transaction) throws SQLException {
        transactionDao.update(transaction);
    }

    /**
     * Deletese the trnsction object permantly.
     * Use-case: Admin Managment.
     */
    public void delete(Transaction transaction) throws SQLException {
        transactionDao.delete(transaction);
    }
}
