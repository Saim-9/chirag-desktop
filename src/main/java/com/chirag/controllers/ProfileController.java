package com.chirag.controllers;

import com.chirag.models.Transaction;
import com.chirag.models.User;
import com.chirag.services.DashboardService;
import com.chirag.services.UserService;
import com.chirag.utils.SceneManager;
import com.chirag.utils.ToastNotification;
import com.chirag.utils.UserSession;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

/**
 * Controls the user profile screen.
 * Allows users to view/edit their profile, change password, and see transactions.
 * Use-cases: Profile View, Profile Update, Password Reset.
 */
public class ProfileController {

    @FXML private TextField nameField;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;
    @FXML private Label statusLabel;
    @FXML private Label walletLabel;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ListView<String> transactionsList;

    private UserService userService;
    private DashboardService dashboardService;

    public ProfileController() {
        this.userService = new UserService();
        this.dashboardService = new DashboardService();
    }

    /**
     * Populates all profile fields with current user data.
     * Loads transactions asynchronously.
     * Use-case: Profile View.
     */
    @FXML
    public void initialize() {
        User user = UserSession.getCurrentUser();
        if (user != null) {
            nameField.setText(user.getName());
            emailLabel.setText(user.getEmail());
            roleLabel.setText(user.getRole());
            walletLabel.setText("$" + String.format("%.2f", user.getVirtualWalletBalance()));

            String status = user.getAccountStatus();
            statusLabel.setText(status);
            if ("ACTIVE".equals(status)) {
                statusLabel.setStyle("-fx-text-fill: #2D6A4F; -fx-font-weight: bold;");
            } else {
                statusLabel.setStyle("-fx-text-fill: #E63946; -fx-font-weight: bold;");
            }

            loadTransactionsAsync(user);
        }
    }

    /**
     * Loads transaction history on background thread.
     */
    private void loadTransactionsAsync(User user) {
        Task<List<Transaction>> task = new Task<>() {
            @Override
            protected List<Transaction> call() {
                return dashboardService.getRecentTransactions(user, 20);
            }
        };
        task.setOnSucceeded(e -> {
            transactionsList.getItems().clear();
            for (Transaction t : task.getValue()) {
                boolean isIncome = (t.getInstructor() != null && t.getInstructor().getId() == user.getId()
                        && (t.getBuyer() == null || t.getBuyer().getId() != user.getId()));
                if (isIncome) {
                    transactionsList.getItems().add("+$" + String.format("%.2f", t.getNetAmount()) + " — " + t.getDescription());
                } else {
                    transactionsList.getItems().add("-$" + String.format("%.2f", t.getAmount()) + " — " + t.getDescription());
                }
            }
            if (task.getValue().isEmpty()) {
                transactionsList.getItems().add("No transactions yet");
            }

            // Color-code transactions
            transactionsList.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        if (item.startsWith("+$")) {
                            setStyle("-fx-text-fill: #2D6A4F; -fx-font-weight: bold;");
                        } else if (item.startsWith("-$")) {
                            setStyle("-fx-text-fill: #E63946; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: #8D99AE; -fx-font-style: italic;");
                        }
                    }
                }
            });
        });
        new Thread(task, "profile-transactions-loader").start();
    }

    /**
     * Saves the updated user name.
     * Use-case: Profile Update.
     */
    @FXML
    public void handleSaveName(ActionEvent event) {
        String newName = nameField.getText().trim();
        if (newName.isEmpty()) {
            ToastNotification.warning("Name cannot be empty");
            return;
        }

        User user = UserSession.getCurrentUser();
        user.setName(newName);
        try {
            new com.chirag.repositories.UserRepository().update(user);
            UserSession.setCurrentUser(user);
            ToastNotification.success("Name updated successfully!");
        } catch (Exception e) {
            ToastNotification.error("Failed to update name");
        }
    }

    /**
     * Handles password change with validation.
     * Verifies current password, checks new password strength, and confirms match.
     * Use-case: Password Reset.
     */
    @FXML
    public void handleChangePassword(ActionEvent event) {
        String currentPw = currentPasswordField.getText();
        String newPw = newPasswordField.getText();
        String confirmPw = confirmPasswordField.getText();

        User user = UserSession.getCurrentUser();

        // Validate current password
        if (!BCrypt.checkpw(currentPw, user.getPassword())) {
            ToastNotification.error("Current password is incorrect");
            return;
        }

        // Validate new password strength
        if (newPw.length() < 6) {
            ToastNotification.warning("New password must be at least 6 characters");
            return;
        }

        // Validate passwords match
        if (!newPw.equals(confirmPw)) {
            ToastNotification.error("New passwords do not match");
            return;
        }

        // Hash and save
        user.setPassword(BCrypt.hashpw(newPw, BCrypt.gensalt()));
        try {
            new com.chirag.repositories.UserRepository().update(user);
            UserSession.setCurrentUser(user);
            ToastNotification.success("Password updated successfully!");

            // Clear fields
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
        } catch (Exception e) {
            ToastNotification.error("Failed to update password");
        }
    }

    /**
     * Navigates back to the dashboard.
     * Use-case: View Navigation.
     */
    @FXML
    public void goBack(ActionEvent event) {
        SceneManager.getInstance().switchScene("DashboardView.fxml");
    }
}
