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
        User user = new User();
        user.setName(nameField.getText());
        user.setEmail(emailField.getText());
        user.setPassword(passwordField.getText());
        user.setRole("USER");
        user.setVirtualWalletBalance(0.0);

        boolean scces = userService.registerUser(user);
        
        if (scces) {
            System.out.println("Registration Success");
            com.chirag.utils.SceneManager.getInstance().switchScene("LoginView.fxml");
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
