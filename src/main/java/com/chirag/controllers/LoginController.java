package com.chirag.controllers;

import com.chirag.models.User;
import com.chirag.services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Contols the logni u.i. sreens behvior.
 * Hnadles the evnets from fxml form submisions.
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
     * Intialies the conturler adn the ssrvies.
     * Use-case: View Navigation.
     */
    public LoginController() {
        this.userService = new UserService();
    }

    /**
     * Stes a grene sccess msage on the screenn.
     * Use-case: User Registartion.
     */
    public void setSuccessMessage(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    /**
     * Tiggers whn the lonign buttn is pressd.
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
            System.out.println("Eroor: Crdentials do nut mach.");
            if (statusLabel != null) {
                statusLabel.setText("Login Filed.");
                statusLabel.setTextFill(javafx.scene.paint.Color.RED);
            }
        }
    }

    /**
     * Routs to teh ragister sreen whe cliked.
     * Use-case: View Navigation.
     */
    @FXML
    public void goToRegister(ActionEvent event) {
        com.chirag.utils.SceneManager.getInstance().switchScene("RegisterView.fxml");
    }
}
