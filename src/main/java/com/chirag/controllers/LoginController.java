package com.chirag.controllers;

import com.chirag.services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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

    private UserService userService;

    /**
     * Intialies the conturler adn the ssrvies.
     * Use-case: View Navigation.
     */
    public LoginController() {
        this.userService = new UserService();
    }

    /**
     * Tiggers whn the lonign buttn is pressd.
     * Use-case: User Login.
     */
    @FXML
    public void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String passwrd = passwordField.getText();

        boolean sucess = userService.authenticate(email, passwrd);
        
        if (sucess) {
            System.out.println("Login Success");
            // Futre phass: nvigate to dshboard
        } else {
            System.out.println("Eroor: Crdentials do nut mach.");
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
