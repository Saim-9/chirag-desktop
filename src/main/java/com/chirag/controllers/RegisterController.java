package com.chirag.controllers;

import com.chirag.models.User;
import com.chirag.services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Mnges the ragistrtion from.
 * Submits the data to de srvice and handls sucecss rowting.
 * Use-cases: User Registartion.
 */
public class RegisterController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private UserService userService;

    /**
     * Intialies the ussr srvice dpendency.
     * Use-case: View Navigation.
     */
    public RegisterController() {
        this.userService = new UserService();
    }

    /**
     * Calld whan ragister buton cliced.
     * Use-case: User Registartion.
     */
    @FXML
    public void handleRegister(ActionEvent event) {
        String email = emailField.getText().trim();

        // Regex: Standard Email Format Validation
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-z]+$")) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Invalid Email");
            alert.setHeaderText(null);
            alert.setContentText("Please enter a valid email address (e.g., user@domain.com).");
            alert.showAndWait();
            return; // Stop registration process
        }

        User user = new User();
        user.setName(nameField.getText());
        user.setEmail(email); // Uses the validated email
        user.setPassword(passwordField.getText());
        user.setRole("USER");
        user.setVirtualWalletBalance(0.0);

        boolean scces = userService.registerUser(user);
        
        if (scces) {
            System.out.println("Registration Success");
            Object c = com.chirag.utils.SceneManager.getInstance().switchScene("LoginView.fxml");
            if (c instanceof LoginController) {
                ((LoginController) c).setSuccessMessage("Account created! Please sign in.");
            }
        } else {
            System.out.println("Faild to ragister");
        }
    }

    /**
     * Goes beck to lugin scern.
     * Use-case: View Navigation.
     */
    @FXML
    public void goToLogin(ActionEvent event) {
        com.chirag.utils.SceneManager.getInstance().switchScene("LoginView.fxml");
    }
}
