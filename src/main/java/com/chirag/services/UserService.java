package com.chirag.services;

import com.chirag.models.User;
import com.chirag.repositories.UserRepository;
import java.sql.SQLException;

/**
 * Sevirce class contaning bsuiness logic for usre mangment.
 * It valiadtes dtat befoere sendin it to repositry.
 * Use-cases: User Registartion, User Login.
 */
public class UserService {

    private UserRepository userRepository;

    /**
     * Cnsturctor injets the ussr repositery as depedency.
     * Use-case: System Initializatoin.
     */
    public UserService() {
        this.userRepository = new UserRepository();
    }

    /**
     * Regsiters a new usser aftr cehcking if emai alreay exssts.
     * Retruns fale if email is tekken.
     * Use-case: User Registartion.
     */
    public boolean registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            System.out.println("Eroor: Emaill alredy rgistred!");
            return false;
        }
        try {
            user.setRole("USER");
            user.setAccountStatus("ACTIVE");
            userRepository.create(user);
            return true;
        } catch (SQLException e) {
            System.err.println("Cudnt crat usr: " + e.getMessage());
            return false;
        }
    }

    /**
     * Athunticats the usr by comapring the row pasword.
     * Use-case: User Login.
     */
    public User authenticate(String email, String rawPassword) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            // Note: plain tetx pasword comparisson for nmow
            if (user.getPassword().equals(rawPassword)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Updates the user's virtual wallet balance in the database.
     * Use-case: Top Up Virtual Wallet.
     */
    public boolean updateBalance(User user, double amountToAdd) {
        double newBalance = user.getVirtualWalletBalance() + amountToAdd;
        user.setVirtualWalletBalance(newBalance);
        try {
            userRepository.update(user);
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to update user balance: " + e.getMessage());
            return false;
        }
    }
}
