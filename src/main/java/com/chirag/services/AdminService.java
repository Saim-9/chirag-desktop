package com.chirag.services;

import com.chirag.models.*;
import com.chirag.repositories.*;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Service layer for all admin operations.
 * Decouples AdminDashboardController from direct repository access.
 * Demonstrates INHERITANCE by extending AbstractService.
 * Use-cases: View Revenue, Manage Users, Resolve Reports.
 */
public class AdminService extends AbstractService {

    private TransactionRepository transactionRepository;
    private ReportRepository reportRepository;
    private UserRepository userRepository;
    private CourseRepository courseRepository;

    /**
     * Sets up all required repositories for admin ops.
     * Use-case: System Initialization.
     */
    public AdminService() {
        this.transactionRepository = new TransactionRepository();
        this.reportRepository = new ReportRepository();
        this.userRepository = new UserRepository();
        this.courseRepository = new CourseRepository();
    }

    /**
     * Fetches all transactions for revenue analytics.
     * Use-case: View Revenue.
     */
    public List<Transaction> getAllTransactions() {
        try {
            return transactionRepository.getDao().queryForAll();
        } catch (SQLException e) {
            System.err.println("Failed to fetch transactions: " + e.getMessage());
            logServiceAction("Admin", "Fetch Transactions", false);
            return Collections.emptyList();
        }
    }

    /**
     * Fetches all pending reports for moderation.
     * Use-case: Moderation.
     */
    public List<Report> getPendingReports() {
        try {
            return reportRepository.getDao().queryBuilder()
                    .where().eq("status", "PENDING").query();
        } catch (SQLException e) {
            System.err.println("Failed to fetch reports: " + e.getMessage());
            logServiceAction("Admin", "Fetch Reports", false);
            return Collections.emptyList();
        }
    }

    /**
     * Dismisses a report by deleting it from the database.
     * Use-case: Moderation.
     */
    public boolean dismissReport(Report report) {
        try {
            reportRepository.getDao().delete(report);
            logServiceAction("Admin", "Dismiss Report #" + report.getId(), true);
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to dismiss report: " + e.getMessage());
            logServiceAction("Admin", "Dismiss Report", false);
            return false;
        }
    }

    /**
     * Takes down a reported course by deactivating it and resolving the report.
     * Use-case: Moderation.
     */
    public boolean takeDownCourse(Report report) {
        try {
            Course course = report.getReportedCourse();
            course.setActive(false);
            courseRepository.getDao().update(course);

            report.setStatus("RESOLVED");
            reportRepository.getDao().update(report);

            logServiceAction("Admin", "Take Down Course: " + course.getTitle(), true);
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to take down course: " + e.getMessage());
            logServiceAction("Admin", "Take Down Course", false);
            return false;
        }
    }

    /**
     * Fetches all non-admin users for user management.
     * Use-case: User Management.
     */
    public List<User> getAllRegularUsers() {
        try {
            return userRepository.getDao().queryBuilder()
                    .where().ne("role", "ADMIN").query();
        } catch (SQLException e) {
            System.err.println("Failed to fetch users: " + e.getMessage());
            logServiceAction("Admin", "Fetch Users", false);
            return Collections.emptyList();
        }
    }

    /**
     * Suspends a user and cascading-deactivates all their courses.
     * Use-case: User Management.
     */
    public boolean suspendUser(User user) {
        try {
            user.setAccountStatus("SUSPENDED");
            userRepository.getDao().update(user);

            // Cascading soft delete: deactivate all courses by this instructor
            List<Course> courses = courseRepository.getDao().queryBuilder()
                    .where().eq("instructor_id", user.getId()).query();
            for (Course c : courses) {
                c.setActive(false);
                courseRepository.getDao().update(c);
            }

            logServiceAction("Admin", "Suspend User: " + user.getEmail(), true);
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to suspend user: " + e.getMessage());
            logServiceAction("Admin", "Suspend User", false);
            return false;
        }
    }

    /**
     * Checks if the given user is the Super Admin (the one seeded at startup).
     * Compares against the email stored in config.properties.
     * Use-case: Super Admin Privilege Escalation.
     */
    public boolean isSuperAdmin(User user) {
        if (user == null) return false;
        String superAdminEmail = com.chirag.utils.DatabaseConfig.getInstance().getSuperAdminEmail();
        return superAdminEmail != null && superAdminEmail.equalsIgnoreCase(user.getEmail());
    }

    /**
     * Fetches all admin users except the currently logged-in super admin.
     * Only the super admin should be able to call this.
     * Use-case: Super Admin Management.
     */
    public List<User> getOtherAdmins(User currentAdmin) {
        try {
            return userRepository.getDao().queryBuilder()
                    .where()
                    .eq("role", "ADMIN")
                    .and()
                    .ne("id", currentAdmin.getId())
                    .query();
        } catch (SQLException e) {
            System.err.println("Failed to fetch admin users: " + e.getMessage());
            logServiceAction("Admin", "Fetch Admins", false);
            return Collections.emptyList();
        }
    }

    /**
     * Deletes an admin user permanently from the database.
     * Only the super admin is authorized to perform this action.
     * Use-case: Super Admin Management.
     */
    public boolean deleteAdmin(User admin) {
        try {
            userRepository.getDao().delete(admin);
            logServiceAction("Admin", "Delete Admin: " + admin.getEmail(), true);
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to delete admin: " + e.getMessage());
            logServiceAction("Admin", "Delete Admin", false);
            return false;
        }
    }
}
