package com.chirag.services;

import com.chirag.models.User;
import com.chirag.repositories.UserRepository;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;


/**
 * Service class containing business logic for user management.
 * It validates data before sending it to repository.
 * Use-cases: User Registration, User Login.
 */
public class UserService extends AbstractService {

    private UserRepository userRepository;

    /**
     * Constructor injects the user repository as dependency.
     * Use-case: System Initialization.
     */
    public UserService() {
        this.userRepository = new UserRepository();
    }

    /**
     * Registers a new user after checking if emai already exists.
     * Returns false if email is taken.
     * Use-case: User Registration.
     */
    public boolean registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            logger.warn("Registration blocked: Email already registered");
            return false;
        }
        try {
            // Hash password using encapsulated DRY helper
            user.setPassword(hashPassword(user.getPassword()));

            user.setRole("USER");
            user.setAccountStatus("ACTIVE");
            userRepository.create(user);
            logServiceAction("User", "Register", true);
            return true;
        }

        catch (SQLException e)
        {
            logger.error("Couldn't create user: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Registers a new Admin user. Only called from the Admin Dashboard.
     * Intercepts and hashes the password securely.
     * Use-case: Role-Based Access Control Expansion.
     */
    public boolean registerNewAdmin(User newAdmin) {
        if (userRepository.findByEmail(newAdmin.getEmail()) != null) {
            logger.warn("Admin registration blocked: Email already registered");
            return false;
        }
        try {

            // Hash password using encapsulated DRY helper
            newAdmin.setPassword(hashPassword(newAdmin.getPassword()));

            // Force the Admin role and status
            newAdmin.setRole("ADMIN");
            newAdmin.setAccountStatus("ACTIVE");

            userRepository.create(newAdmin);
            return true;
        } catch (SQLException e) {
            logger.error("Couldn't create admin: {}", e.getMessage());
            return false;
        }
    }


    /**
     * Authenticates the user by comparing the row password.
     * Use-case: User Login.
     */
    public User authenticate(String email, String rawPassword)
    {
        User user = userRepository.findByEmail(email);
        if (user != null)
        {
            // BCrypt checks the raw input against the hashed string in the database
            if (BCrypt.checkpw(rawPassword, user.getPassword()))
            {
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
            logger.error("Failed to update user balance: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Encapsulates password hashing logic (DRY Principle).
     */
    private String hashPassword(String plainPassword) {
        return org.mindrot.jbcrypt.BCrypt.hashpw(plainPassword, org.mindrot.jbcrypt.BCrypt.gensalt());
    }
}
