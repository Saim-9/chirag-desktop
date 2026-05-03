package com.chirag.utils;

import com.chirag.models.Course;
import com.chirag.models.Lecture;
import com.chirag.models.Transaction;
import com.chirag.models.User;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
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
        try {
            TableUtils.dropTable(connectionSource, User.class, true);
        } catch (Exception e) {}
        TableUtils.createTableIfNotExists(connectionSource, User.class);
        
        try {
            TableUtils.dropTable(connectionSource, Course.class, true);
        } catch (Exception e) {}
        TableUtils.createTableIfNotExists(connectionSource, Course.class);

        TableUtils.createTableIfNotExists(connectionSource, Lecture.class);
        try {
            TableUtils.dropTable(connectionSource, Transaction.class, true);
        } catch (Exception e) {
        }
        TableUtils.createTableIfNotExists(connectionSource, Transaction.class);
        
        try {
            TableUtils.dropTable(connectionSource, com.chirag.models.Enrollment.class, true);
        } catch (Exception e) {
        }
        TableUtils.createTableIfNotExists(connectionSource, com.chirag.models.Enrollment.class);
        
        TableUtils.createTableIfNotExists(connectionSource, com.chirag.models.Review.class);
        TableUtils.createTableIfNotExists(connectionSource, com.chirag.models.Report.class);
        
        seedAdmin();
    }

    /**
     * Seeds the Super Admin user if they don't exist.
     * Use-case: Bootstrap Seeder.
     */
    private void seedAdmin() {
        try {
            Dao<User, Integer> userDao = DaoManager.createDao(connectionSource, User.class);
            User admin = userDao.queryBuilder().where().eq("email", "saim@test.com").queryForFirst();
            if (admin == null) {
                admin = new User();
                admin.setName("Super Admin");
                admin.setEmail("saim@test.com");
                admin.setPassword("adminpassword");
                admin.setRole("ADMIN");
                admin.setAccountStatus("ACTIVE");
                userDao.create(admin);
                System.out.println("Super Admin seeded.");
            } else {
                admin.setRole("ADMIN");
                userDao.update(admin);
            }
        } catch (SQLException e) {
            System.err.println("Seeding error: " + e.getMessage());
        }
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
