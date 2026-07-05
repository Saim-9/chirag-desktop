package com.chirag.services;

import com.chirag.models.Course;
import com.chirag.models.Transaction;
import com.chirag.models.User;
import com.chirag.repositories.TransactionRepository;
import com.chirag.repositories.UserRepository;
import com.chirag.utils.DatabaseConfig;
import com.chirag.utils.UserSession;
import java.sql.SQLException;
import java.util.Date;
import com.j256.ormlite.misc.TransactionManager;

/**
 * Demonstrates the Facade Pattern, Strategy Pattern (via IPaymentService),
 * and Inheritance (via AbstractService).
 * Purchase flow is now atomic — wrapped in a database transaction with rollback.
 */
public class PaymentServiceImpl extends AbstractService implements IPaymentService {
    private UserRepository userRepository;
    private TransactionRepository transactionRepository;
    private com.chirag.repositories.EnrollmentRepository enrollmentRepository;
    private UserService userService;

    /**
     * Sets up dependencies for user, transaction, and enrollment repos.
     * Use-case: System Initialization.
     */
    public PaymentServiceImpl() {
        this.userRepository = new UserRepository();
        this.transactionRepository = new TransactionRepository();
        this.enrollmentRepository = new com.chirag.repositories.EnrollmentRepository();
        this.userService = new UserService();
    }

    /**
     * Processes the full payment event logic atomically.
     * All database operations are wrapped in a transaction — if any step fails,
     * all changes are rolled back automatically (balance, transaction record, enrollment).
     * Use-case: Course Purchase.
     */
    @Override
    public boolean processCoursePurchase(User buyer, Course course) {
        // Check for existing enrollment first (duplicate guard)
        com.chirag.models.Enrollment existingEnrollment = enrollmentRepository.findByUserAndCourse(buyer, course);
        if (existingEnrollment != null) {
            logger.info("User is already enrolled in this course.");
            logServiceAction("Payment", "Purchase Course (Duplicate)", false);
            return false;
        }

        double price = course.getPrice();

        // Check balance
        if (buyer.getVirtualWalletBalance() < price) {
            logger.warn("Purchase blocked: Not enough balance!");
            logServiceAction("Payment", "Purchase Course", false); // INHERITANCE LOG
            return false;
        }

        // Execute the entire purchase atomically using ORMLite TransactionManager
        try {
            TransactionManager.callInTransaction(
                DatabaseConfig.getInstance().getConnectionSource(),
                () -> {
                    User instructor = course.getInstructor();
                    boolean isCircular = (buyer.getId() == instructor.getId());

                    double instructorCut = price * 0.90;
                    double adminCut = price * 0.10;

                    // Step 1: Deduct balance
                    if (isCircular) {
                        // Net effect: user only loses the 10% admin cut
                        buyer.setVirtualWalletBalance(buyer.getVirtualWalletBalance() - adminCut);
                    } else {
                        buyer.setVirtualWalletBalance(buyer.getVirtualWalletBalance() - price);
                        instructor.setVirtualWalletBalance(instructor.getVirtualWalletBalance() + instructorCut);
                        userRepository.getDao().update(instructor);
                    }
                    userRepository.getDao().update(buyer);

                    // Step 2: Create transaction record
                    Transaction transaction = new Transaction();
                    transaction.setAmount(price);
                    transaction.setPlatformFee(adminCut);
                    transaction.setNetAmount(instructorCut);
                    transaction.setDescription("Bought course: " + course.getTitle());
                    transaction.setTransactionDate(new Date());
                    transaction.setBuyer(buyer);
                    transaction.setInstructor(instructor);
                    transactionRepository.create(transaction);

                    // Step 3: Create enrollment
                    com.chirag.models.Enrollment enr = new com.chirag.models.Enrollment();
                    enr.setUser(buyer);
                    enr.setCourse(course);
                    enr.setCompleted(false);
                    enrollmentRepository.create(enr);

                    return null; // TransactionManager requires a return value
                }
            );

            // Refresh UserSession from DB directly (outside the transaction)
            UserSession.setCurrentUser(userRepository.getDao().queryForId(buyer.getId()));

            logServiceAction("Payment", "Purchase Course", true); // INHERITANCE LOG
            return true;

        } catch (SQLException e) {
            logger.error("Purchase transaction ROLLED BACK: {}", e.getMessage());
            // On failure, reload the buyer from DB to ensure in-memory state is correct
            try {
                User freshBuyer = userRepository.getDao().queryForId(buyer.getId());
                if (freshBuyer != null) {
                    buyer.setVirtualWalletBalance(freshBuyer.getVirtualWalletBalance());
                    UserSession.setCurrentUser(freshBuyer);
                }
            } catch (SQLException refreshEx) {
                logger.error("Failed to refresh buyer state after rollback: {}", refreshEx.getMessage());
            }
            logServiceAction("Payment", "Purchase Course (ROLLBACK)", false); // INHERITANCE LOG
            return false;
        }
    }
}