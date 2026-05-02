package com.chirag.services;

import com.chirag.models.Course;
import com.chirag.models.Transaction;
import com.chirag.models.User;
import com.chirag.repositories.TransactionRepository;
import com.chirag.repositories.UserRepository;
import java.sql.SQLException;
import java.util.Date;

/**
 * Manages payment splitting and wallet updates.
 * Simulates purchasing by deducting funds and giving 90% to teacher.
 * Use-cases: Course Purchase, Wallet Management.
 */
public class PaymentService {

    private UserRepository userRepository;
    private TransactionRepository transactionRepository;
    private com.chirag.repositories.EnrollmentRepository enrollmentRepository;

    /**
     * Sets up dependencies for user, transaction, and enrollment repos.
     * Use-case: System Initialization.
     */
    public PaymentService() {
        this.userRepository = new UserRepository();
        this.transactionRepository = new TransactionRepository();
        this.enrollmentRepository = new com.chirag.repositories.EnrollmentRepository();
    }

    /**
     * Processes the full payment event logic.
     * Checks balance, deducts, splits revenue, and saves records.
     * Use-case: Course Purchase.
     */
    public boolean processCoursePurchase(User buyer, Course course) {
        double price = course.getPrice();
        
        // Check balance
        if (buyer.getVirtualWalletBalance() < price) {
            System.out.println("Error: Not enough balance!");
            return false;
        }

        // Deduct full price
        double newBuyerBalance = buyer.getVirtualWalletBalance() - price;
        buyer.setVirtualWalletBalance(newBuyerBalance);

        // Split revenue (90% to teacher, 10% admin log)
        User instructor = course.getInstructor();
        double instructorCut = price * 0.90;
        double adminCut = price * 0.10;
        
        System.out.println("LOG: Admins took a cut of $" + adminCut);

        double newInstructorBalance = instructor.getVirtualWalletBalance() + instructorCut;
        instructor.setVirtualWalletBalance(newInstructorBalance);

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setAmount(price);
        transaction.setDescription("Bought course: " + course.getTitle());
        transaction.setTransactionDate(new Date());
        transaction.setUser(buyer);

        // Save to database through repositories
        try {
            userRepository.update(buyer);
            userRepository.update(instructor);
            transactionRepository.create(transaction);
            
            com.chirag.models.Enrollment enr = new com.chirag.models.Enrollment();
            enr.setUser(buyer);
            enr.setCourse(course);
            enr.setCompleted(false);
            enrollmentRepository.create(enr);
            
            return true;
        } catch (SQLException e) {
            System.err.println("Oops, transaction failed to persist: " + e.getMessage());
            return false;
        }
    }
}
