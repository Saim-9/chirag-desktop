package com.chirag.controllers;

import com.chirag.models.User;
import com.chirag.services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controls the login u.i. screens behavior.
 * Handles the events from fxml form submissions.
 * Use-cases: User Login.
 */
public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    private UserService userService;

    /**
     * Initializes the controller adn the service.
     * Use-case: View Navigation.
     */
    public LoginController() {
        this.userService = new UserService();
    }

    /**
     * Sets a green success message on the screen.
     * Use-case: User Registration.
     */
    public void setSuccessMessage(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    /**
     * Tiggers whn the login button is pressed.
     * Use-case: User Login.
     */
    @FXML
    public void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String passwrd = passwordField.getText();

        User user = userService.authenticate(email, passwrd);
        
        if (user != null) {
            if ("SUSPENDED".equals(user.getAccountStatus())) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Account Suspended");
                alert.setHeaderText(null);
                alert.setContentText("Your account has been suspended by an administrator.");
                java.net.URL cssUrl = getClass().getResource("/com/chirag/views/styles.css");
                if (cssUrl != null) {
                    alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
                }
                alert.showAndWait();
                return;
            }

            System.out.println("Login Success");
            com.chirag.utils.UserSession.setCurrentUser(user);
            
            if ("ADMIN".equals(user.getRole())) {
                com.chirag.utils.SceneManager.getInstance().switchScene("AdminDashboardView.fxml");
            } else {
                com.chirag.utils.SceneManager.getInstance().switchScene("DashboardView.fxml");
            }
        } else {
            System.out.println("Error: Credentials do not mach.");
            if (statusLabel != null) {
                statusLabel.setText("Login Filed.");
                statusLabel.setTextFill(javafx.scene.paint.Color.RED);
            }
        }
    }

    /**
     * Routs to teh register screen when clicked.
     * Use-case: View Navigation.
     */
    @FXML
    public void goToRegister(ActionEvent event) {
        com.chirag.utils.SceneManager.getInstance().switchScene("RegisterView.fxml");
    }
}
