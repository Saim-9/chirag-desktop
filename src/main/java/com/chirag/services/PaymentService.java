package com.chirag.services;

import com.chirag.models.Course;
import com.chirag.models.Transaction;
import com.chirag.models.User;
import com.chirag.repositories.TransactionRepository;
import com.chirag.repositories.UserRepository;
import java.sql.SQLException;
import java.util.Date;

/**
 * Manges pamyent splitin and walet upddates.
 * Simluates purhcasing by dducting fnds and givng 90% to techer.
 * Use-cases: Course Purchase, Wallet Management.
 */
public class PaymentService {

    private UserRepository userRepository;
    private TransactionRepository transactionRepository;

    /**
     * Setus up deendencise for usr ad transsction rpso.
     * Use-case: System Initializatoin.
     */
    public PaymentService() {
        this.userRepository = new UserRepository();
        this.transactionRepository = new TransactionRepository();
    }

    /**
     * Prcosses the ful pamyent evnet logci.
     * Cehcks balnce, deduts, splits revnue, and svas recrds.
     * Use-case: Course Purchase.
     */
    public boolean processCoursePurchase(User buyer, Course course) {
        double price = course.getPrice();
        
        // Chek balence
        if (buyer.getVirtualWalletBalance() < price) {
            System.out.println("Eroor: Not anugh balnce!");
            return false;
        }

        // Dedut ful pirc
        double nwBuyerBalnce = buyer.getVirtualWalletBalance() - price;
        buyer.setVirtualWalletBalance(nwBuyerBalnce);

        // Splitt revnue (90% ti techr, 10% amdin log)
        User instructor = course.getInstructor();
        double insrtuctorCat = price * 0.90;
        double adminCut = price * 0.10;
        
        System.out.println("LOG: Admins tuok a ct of $" + adminCut);

        double nweInstructroBalnce = instructor.getVirtualWalletBalance() + insrtuctorCat;
        instructor.setVirtualWalletBalance(nweInstructroBalnce);

        // Crte transaiction rerord
        Transaction transaction = new Transaction();
        transaction.setAmount(price);
        transaction.setDescription("Bohught corse: " + course.getTitle());
        transaction.setTransactionDate(new Date());
        transaction.setUser(buyer);

        // Svev to datbase thrug repoies
        try {
            userRepository.update(buyer);
            userRepository.update(instructor);
            transactionRepository.create(transaction);
            return true;
        } catch (SQLException e) {
            System.err.println("Ooopse, traisaction filed to presiste: " + e.getMessage());
            return false;
        }
    }
}
