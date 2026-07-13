package com.chirag.controllers;

import com.chirag.models.User;
import com.chirag.repositories.UserRepository;
import com.chirag.utils.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

/**
 * Simulated password recovery flow for academic purposes.
 * Verifies the email exists in the database, then allows direct password reset.
 * In production, this would send a reset email/OTP — simulated here.
 * Use-cases: Password Reset.
 */
public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private VBox resetSection;
    @FXML private Button verifyBtn;
    @FXML private Label statusLabel;

    private UserRepository userRepository;
    private User foundUser;

    public ForgotPasswordController() {
        this.userRepository = new UserRepository();
    }

    /**
     * Verifies the email exists in the database.
     * If found, reveals the password reset form.
     * Use-case: Password Reset (Step 1: Verify Identity).
     */
    @FXML
    public void handleVerifyEmail(ActionEvent event) {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            statusLabel.setText("Please enter your email address");
            statusLabel.setStyle("-fx-text-fill: #C0392B;");
            return;
        }

        foundUser = userRepository.findByEmail(email);
        if (foundUser != null) {
            // Email exists — show reset form (simulated email verification)
            statusLabel.setText("[Done] Email verified! (Simulated — in production, a reset link would be emailed)");
            statusLabel.setStyle("-fx-text-fill: #27714A;");

            // Reveal the reset section
            resetSection.setVisible(true);
            resetSection.setManaged(true);
            verifyBtn.setVisible(false);
            verifyBtn.setManaged(false);
            emailField.setDisable(true);
        } else {
            statusLabel.setText("No account found with this email address");
            statusLabel.setStyle("-fx-text-fill: #C0392B;");
        }
    }

    /**
     * Resets the password after validation.
     * Use-case: Password Reset (Step 2: Set New Password).
     */
    @FXML
    public void handleReset(ActionEvent event) {
        String newPw = newPasswordField.getText();
        String confirmPw = confirmPasswordField.getText();

        if (newPw.length() < 6) {
            statusLabel.setText("Password must be at least 6 characters");
            statusLabel.setStyle("-fx-text-fill: #C0392B;");
            return;
        }

        if (!newPw.equals(confirmPw)) {
            statusLabel.setText("Passwords do not match");
            statusLabel.setStyle("-fx-text-fill: #C0392B;");
            return;
        }

        // Hash and update
        foundUser.setPassword(BCrypt.hashpw(newPw, BCrypt.gensalt()));
        try {
            userRepository.update(foundUser);
            statusLabel.setText("[Done] Password reset successful! Redirecting to login...");
            statusLabel.setStyle("-fx-text-fill: #27714A;");

            // Navigate to login after a brief delay
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
            pause.setOnFinished(e -> {
                Object ctrl = SceneManager.getInstance().switchScene("LoginView.fxml");
                if (ctrl instanceof LoginController) {
                    ((LoginController) ctrl).setSuccessMessage("Password reset! Please sign in with your new password.");
                }
            });
            pause.play();
        } catch (SQLException e) {
            statusLabel.setText("Failed to reset password. Please try again.");
            statusLabel.setStyle("-fx-text-fill: #C0392B;");
        }
    }

    /**
     * Navigates back to login.
     * Use-case: View Navigation.
     */
    @FXML
    public void goToLogin(ActionEvent event) {
        SceneManager.getInstance().switchScene("LoginView.fxml");
    }
}
