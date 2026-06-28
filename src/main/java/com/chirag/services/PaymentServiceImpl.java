package com.chirag.services;

import com.chirag.models.Course;
import com.chirag.models.Transaction;
import com.chirag.models.User;
import com.chirag.repositories.TransactionRepository;
import com.chirag.repositories.UserRepository;
import com.chirag.utils.UserSession;
import java.sql.SQLException;
import java.util.Date;

/**
 * Demonstrates the Facade Pattern, Strategy Pattern (via IPaymentService),
 * and Inheritance (via AbstractService).
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
     * Processes the full payment event logic.
     * Checks balance, deducts, splits revenue, and saves records.
     * Use-case: Course Purchase.
     */
    @Override
    public boolean processCoursePurchase(User buyer, Course course) {
        // Check for existing enrollment first (duplicate guard)
        com.chirag.models.Enrollment existingEnrollment = enrollmentRepository.findByUserAndCourse(buyer, course);
        if (existingEnrollment != null) {
            System.out.println("User is already enrolled in this course.");
            logServiceAction("Payment", "Purchase Course (Duplicate)", false);
            return false;
        }

        double price = course.getPrice();

        // Check balance
        if (buyer.getVirtualWalletBalance() < price) {
            System.out.println("Error: Not enough balance!");
            logServiceAction("Payment", "Purchase Course", false); // INHERITANCE LOG
            return false;
        }

        User instructor = course.getInstructor();
        boolean isCircular = (buyer.getId() == instructor.getId());

        double instructorCut = price * 0.90;
        double adminCut = price * 0.10;

        // Ensure updateBalance accurately reflects the final wallet delta
        if (isCircular) {
            // Net effect: user only loses the 10% admin cut
            userService.updateBalance(buyer, -adminCut);
        } else {
            userService.updateBalance(buyer, -price);
            userService.updateBalance(instructor, instructorCut);
        }

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setAmount(price);
        transaction.setPlatformFee(adminCut);
        transaction.setNetAmount(instructorCut);
        transaction.setDescription("Bought course: " + course.getTitle());
        transaction.setTransactionDate(new Date());
        transaction.setBuyer(buyer);
        transaction.setInstructor(instructor);

        // Save to database through repositories
        try {
            transactionRepository.create(transaction);

            com.chirag.models.Enrollment enr = new com.chirag.models.Enrollment();
            enr.setUser(buyer);
            enr.setCourse(course);
            enr.setCompleted(false);
            enrollmentRepository.create(enr);

            // Refresh UserSession from DB directly
            UserSession.setCurrentUser(userRepository.getDao().queryForId(buyer.getId()));

            logServiceAction("Payment", "Purchase Course", true); // INHERITANCE LOG
            return true;
        } catch (SQLException e) {
            System.err.println("Oops, transaction failed to persist: " + e.getMessage());
            logServiceAction("Payment", "Purchase Course", false); // INHERITANCE LOG
            return false;
        }
    }
}