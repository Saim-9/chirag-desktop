package com.chirag.controllers;

import com.chirag.models.User;
import com.chirag.services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the registeration form.
 * Submits the data to the service and handles success routing.
 * Use-cases: User Registeration.
 */
public class RegisterController {

    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private UserService userService;

    /**
     * Initializes the user service dependency.
     * Use-case: View Navigation.
     */
    public RegisterController() {
        this.userService = new UserService();
    }

    /**
     * Called when register buton clicked.
     * Use-case: User Registartion.
     */
    @FXML
    public void handleRegister(javafx.event.ActionEvent event) {
        String email = emailField.getText().trim();
        String plainPassword = passwordField.getText();

        // Regex: Standard Email Format Validation
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-z]+$")) {
            showModernAlert(Alert.AlertType.ERROR, "Invalid Email", "Please enter a valid email address (e.g., user@domain.com).");
            return; // Stop registration process
        }

        // Password Length Validation (Strike 7)
        if (plainPassword == null || plainPassword.trim().length() < 4) {
            showModernAlert(Alert.AlertType.WARNING, "Weak Password", "Security requirement: Your password must be at least 4 characters long.");
            return; // Stop registration process
        }

        //  Populate Domain Model
        User user = new User();
        user.setName(nameField.getText());
        user.setEmail(email);
        user.setPassword(plainPassword);

        // Delegate to Service Layer
        boolean success = userService.registerUser(user);

        if (success) {
            logger.info("Registration successful for: {}", email);
            Object c = com.chirag.utils.SceneManager.getInstance().switchScene("LoginView.fxml");
            if (c instanceof LoginController) {
                ((LoginController) c).setSuccessMessage("Account created! Please sign in.");
            }
        } else {
            showModernAlert(Alert.AlertType.ERROR, "Registration Failed", "This email is already registered. Please use a different one or log in.");
        }
    }

    /**
     * Goes beck to login screen.
     * Use-case: View Navigation.
     */
    @FXML
    public void goToLogin(ActionEvent event) {
        com.chirag.utils.SceneManager.getInstance().switchScene("LoginView.fxml");
    }

    /**
     * Spawns a modern, CSS-styled alert dialog.
     */
    private void showModernAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);


        java.net.URL cssUrl = getClass().getResource("/com/chirag/views/styles.css");
        if (cssUrl != null) {
            alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
        }
        alert.showAndWait();
    }
}
