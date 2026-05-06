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
import org.mindrot.jbcrypt.BCrypt;

/**
 * Manages the database connectivity using singleton pattern.
 * Instantiates the SqLite connection and builds the schemas.
 * Use-cases: System Initialization.
 */
public class DatabaseConfig {

    private static final String DATABASE_URL = "jdbc:postgresql://aws-1-ap-northeast-1.pooler.supabase.com:5432/postgres";
    private static final String DB_USERNAME = "postgres.dpappiijfilkfdzqkwkt";
    private static final String DB_PASSWORD = "pB@847-iZ54";
    private static DatabaseConfig instance;
    private ConnectionSource connectionSource;

    /**
     * Private constructor to restrict external object creation.
     * Calls the initialization method right away.
     * Use-case: System Initialization.
     */
    private DatabaseConfig() {
        try {
            // Now passing the URL, Username, and Password to connect to Supabase
            connectionSource = new JdbcConnectionSource(DATABASE_URL, DB_USERNAME, DB_PASSWORD);
            initializeDatabase();
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
        }
    }

    /**
     * Gets the sol instance of the configuration manager.
     * Ensures only one instance of the Db connections exists.
     * Use-case: System Initialization, Data Relations.
     */
    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    /**
     * Provides the underlying ORMlit connections source to repositories.
     * Use-case: Data Relations.
     */
    public ConnectionSource getConnectionSource() {
        return connectionSource;
    }

    /**
     * Auto generates all the required sql tables from our java classes.
     * Use the ormlit table utils to safely create if it don't exist.
     * Use-case: System Initialization.
     */
    private void initializeDatabase() throws SQLException {
        // Create all table schemas from models safely
        TableUtils.createTableIfNotExists(connectionSource, User.class);
        TableUtils.createTableIfNotExists(connectionSource, Course.class);
        TableUtils.createTableIfNotExists(connectionSource, Lecture.class);
        TableUtils.createTableIfNotExists(connectionSource, Transaction.class);
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

                // Hashing the password before setting it
                String plainTextPassword = "adminpassword";
                String hashedPassword = BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
                admin.setPassword(hashedPassword);

                admin.setRole("ADMIN");
                admin.setAccountStatus("ACTIVE");
                userDao.create(admin);
                System.out.println("Super Admin seeded with hashed password.");
            } else {
                admin.setRole("ADMIN");
                userDao.update(admin);
            }
        } catch (SQLException e) {
            System.err.println("Seeding error: " + e.getMessage());
        }
    }

    /**
     * Closes the DB connection when the application shutdowns.
     * Prevents memory leaks and locs on the sqlite fla.
     * Use-case: System Shutdown.
     */
    public void close() throws Exception {
        if (connectionSource != null) {
            connectionSource.close();
        }
    }
}
