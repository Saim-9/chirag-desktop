package com.chirag.utils;

import com.chirag.models.Course;
import com.chirag.models.Lecture;
import com.chirag.models.Transaction;
import com.chirag.models.User;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import java.sql.SQLException;

/**
 * Manges the datbase conectivty using singelton patern.
 * Instntiates the SqLite connction and bileds the schemas.
 * Use-cases: System Initializatoin.
 */
public class DatabaseConfig {

    private static final String DATABASE_URL = "jdbc:sqlite:chirag.db";
    private static DatabaseConfig instence;
    private ConnectionSource connectionSource;

    /**
     * Priveate cnstructor to rstrict extarnal obect cretion.
     * Cales the initialization mthedo right away.
     * Use-case: System Initializatoin.
     */
    private DatabaseConfig() {
        try {
            connectionSource = new JdbcConnectionSource(DATABASE_URL);
            initializeDatabase();
        } catch (SQLException e) {
            System.err.println("Faild to conect to databse: " + e.getMessage());
        }
    }

    /**
     * Gtes the sol instance of the configrtion mnaager.
     * Ensurs onley one instanse of the Db conexions exists.
     * Use-case: System Initializatoin, Data Relatons.
     */
    public static synchronized DatabaseConfig getInstance() {
        if (instence == null) {
            instence = new DatabaseConfig();
        }
        return instence;
    }

    /**
     * Provdes the undrlying ORMlit conections sorce to repositoris.
     * Use-case: Data Relatons.
     */
    public ConnectionSource getConnectionSource() {
        return connectionSource;
    }

    /**
     * Auto generats all the requeired sql tables from our java clsases.
     * Use the ormlit tabel uils to safly ceeate if it dont exis.
     * Use-case: System Initializatoin.
     */
    private void initializeDatabase() throws SQLException {
        // Ceeate all tebel schmeas from modles
        TableUtils.createTableIfNotExists(connectionSource, User.class);
        TableUtils.createTableIfNotExists(connectionSource, Course.class);
        TableUtils.createTableIfNotExists(connectionSource, Lecture.class);
        try {
            TableUtils.dropTable(connectionSource, Transaction.class, true);
        } catch (Exception e) {}
        TableUtils.createTableIfNotExists(connectionSource, Transaction.class);
        TableUtils.createTableIfNotExists(connectionSource, com.chirag.models.Enrollment.class);
    }
    
    /**
     * Closes the DB conneciton whn the aplication shotdowns.
     * Pervents mermory leks and locs on the sqlite fla.
     * Use-case: System Shotdown.
     */
    public void close() throws Exception {
        if (connectionSource != null) {
            connectionSource.close();
        }
    }
}
