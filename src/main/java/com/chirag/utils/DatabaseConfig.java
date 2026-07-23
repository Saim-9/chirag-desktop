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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the database connectivity using singleton pattern.
 * Loads credentials from config.properties or environment variables.
 * Use-cases: System Initialization.
 */
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    private static DatabaseConfig instance;
    private ConnectionSource connectionSource;

    // Loaded from config.properties or environment variables
    private String databaseUrl;
    private String dbUsername;
    private String dbPassword;
    private String adminEmail;
    private String adminPassword;
    private String adminName;

    /**
     * Private constructor to restrict external object creation.
     * Loads configuration, then initializes DB connection.
     * Use-case: System Initialization.
     */
    private DatabaseConfig() {
        loadConfig();
        try {
            // Now passing the URL, Username, and Password to connect to Supabase
            connectionSource = new JdbcConnectionSource(databaseUrl, dbUsername, dbPassword);
            initializeDatabase();
        } catch (SQLException e) {
            logger.error("Failed to connect to database: {}", e.getMessage());
        }
    }

    /**
     * Loads database and admin credentials from config.properties file.
     * Falls back to environment variables if the file is not found.
     * Use-case: System Initialization.
     */
    private void loadConfig() {
        java.util.Properties props = new java.util.Properties();
        try (java.io.InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                props.load(input);
                databaseUrl = props.getProperty("db.url");
                dbUsername = props.getProperty("db.username");
                dbPassword = props.getProperty("db.password");
                adminEmail = props.getProperty("admin.email", "saim@test.com");
                adminPassword = props.getProperty("admin.password", "adminpassword");
                adminName = props.getProperty("admin.name", "Super Admin");
                logger.info("Loaded database configuration from config.properties");
            } else {
                // Fallback to environment variables
                databaseUrl = System.getenv("CHIRAG_DB_URL");
                dbUsername = System.getenv("CHIRAG_DB_USERNAME");
                dbPassword = System.getenv("CHIRAG_DB_PASSWORD");
                adminEmail = System.getenv().getOrDefault("CHIRAG_ADMIN_EMAIL", "saim@test.com");
                adminPassword = System.getenv().getOrDefault("CHIRAG_ADMIN_PASSWORD", "adminpassword");
                adminName = System.getenv().getOrDefault("CHIRAG_ADMIN_NAME", "Super Admin");
                logger.info("config.properties not found, using environment variables");
            }
        } catch (java.io.IOException e) {
            logger.error("Error loading config.properties: {}", e.getMessage());
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
     * Returns the configured super admin email.
     * Used to identify the super admin for elevated privileges.
     * Use-case: Super Admin Management.
     */
    public String getSuperAdminEmail() {
        return adminEmail;
    }

    /**
     * Auto generates all the required sql tables from our java classes.
     * Use the ormlit table utils to safely create if it don't exist.
     * Use-case: System Initialization.
     */
    private void initializeDatabase() throws SQLException {
        // Create all table schemas from models safely
        // Individual try-catch blocks because PostgreSQL may already have
        // sequences/tables from a previous run and createTableIfNotExists
        // can still fail on sequence creation conflicts.
        safeCreateTable(User.class);
        safeCreateTable(Course.class);
        safeCreateTable(Lecture.class);
        safeCreateTable(Transaction.class);
        safeCreateTable(com.chirag.models.Enrollment.class);
        safeCreateTable(com.chirag.models.Review.class);
        safeCreateTable(com.chirag.models.Report.class);

        // Add any columns that were added to models after initial table creation
        migrateSchema();
        
        seedAdmin();
    }

    /**
     * Attempts to create a table, logging a warning if it already exists.
     * Prevents sequence-already-exists errors from crashing the startup.
     */
    private void safeCreateTable(Class<?> clazz) {
        try {
            TableUtils.createTableIfNotExists(connectionSource, clazz);
        } catch (SQLException e) {
            logger.warn("Table creation skipped for {}: {}", clazz.getSimpleName(), e.getMessage());
        }
    }

    /**
     * Adds any columns that may be missing from existing tables.
     * PostgreSQL supports ADD COLUMN IF NOT EXISTS to safely handle this.
     * Use-case: System Initialization (schema migration).
     */
    private void migrateSchema() {
        String[] migrations = {
            // courses table — imageUrl and isActive may be missing
            "ALTER TABLE \"courses\" ADD COLUMN IF NOT EXISTS \"imageUrl\" VARCHAR(255)",
            "ALTER TABLE \"courses\" ADD COLUMN IF NOT EXISTS \"isActive\" BOOLEAN DEFAULT true",
            // enrollments table — completedLectureIds and isCompleted may be missing
            "ALTER TABLE \"enrollments\" ADD COLUMN IF NOT EXISTS \"isCompleted\" BOOLEAN DEFAULT false",
            "ALTER TABLE \"enrollments\" ADD COLUMN IF NOT EXISTS \"completedLectureIds\" VARCHAR(255) DEFAULT ''",
            // transactions table — platformFee and netAmount may be missing
            "ALTER TABLE \"transactions\" ADD COLUMN IF NOT EXISTS \"platformFee\" NUMERIC DEFAULT 0",
            "ALTER TABLE \"transactions\" ADD COLUMN IF NOT EXISTS \"netAmount\" NUMERIC DEFAULT 0",
            // reports table — complaintText may be missing
            "ALTER TABLE \"reports\" ADD COLUMN IF NOT EXISTS \"complaintText\" VARCHAR(255)",
        };

        try {
            Dao<User, Integer> dao = DaoManager.createDao(connectionSource, User.class);
            for (String sql : migrations) {
                try {
                    dao.executeRawNoArgs(sql);
                    logger.debug("Migration applied: {}", sql);
                } catch (SQLException e) {
                    logger.warn("Migration skipped: {}", e.getMessage());
                }
            }
        } catch (SQLException e) {
            logger.error("Could not create DAO for migrations: {}", e.getMessage());
        }
        logger.info("Schema migration check completed");
    }

    /**
     * Seeds the Super Admin user if they don't exist.
     * Credentials loaded from config.properties or env variables.
     * Use-case: Bootstrap Seeder.
     */
    private void seedAdmin() {
        try {
            Dao<User, Integer> userDao = DaoManager.createDao(connectionSource, User.class);
            User admin = userDao.queryBuilder().where().eq("email", adminEmail).queryForFirst();
            if (admin == null) {
                admin = new User();
                admin.setName(adminName);
                admin.setEmail(adminEmail);

                // Hashing the password before setting it
                String hashedPassword = BCrypt.hashpw(adminPassword, BCrypt.gensalt());
                admin.setPassword(hashedPassword);

                admin.setRole("ADMIN");
                admin.setAccountStatus("ACTIVE");
                userDao.create(admin);
                logger.info("Super Admin seeded with hashed password");
            }
            // No longer force-resetting the role on every startup
        } catch (SQLException e) {
            logger.error("Seeding error: {}", e.getMessage());
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
