package com.chirag.controllers;

import com.chirag.services.UserService;
import com.chirag.utils.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the wallet top-up modal window.
 * Simulates an external payment gateway.
 * Use-cases: Top Up Virtual Wallet.
 */
public class WalletPopupController {

    @FXML
    private TextField amountField;

    private UserService userService;
    private DashboardController parentController;

    /**
     * Initializes the user service dependency.
     * Use-case: Top Up Virtual Wallet.
     */
    public WalletPopupController() {
        this.userService = new UserService();
    }
    
    /**
     * Sets the parent controller to refresh the UI after top up.
     * Use-case: Top Up Virtual Wallet.
     */
    public void setParentController(DashboardController controller) {
        this.parentController = controller;
    }

    /**
     * Handles the simulated payment confirmation.
     * Updates the session and the database.
     * Use-case: Top Up Virtual Wallet.
     */
    @FXML
    public void handleConfirm(ActionEvent event) {
        try {
            double amount = Double.parseDouble(amountField.getText());
            if (amount <= 0) {
                showAlert(Alert.AlertType.ERROR, "Invalid Amount", "Please enter a positive value.");
                return;
            }

            boolean success = userService.updateBalance(UserSession.getCurrentUser(), amount);
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Transaction Successful", "Your wallet has been topped up with $" + String.format("%.2f", amount));
                if (parentController != null) {
                    parentController.refreshWalletDisplay();
                }
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Transaction Failed", "Could not process the payment at this time.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid numeric amount.");
        }
    }

    /**
     * Cancels and closes the modal.
     * Use-case: View Navigation.
     */
    @FXML
    public void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) amountField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        java.net.URL cssUrl = getClass().getResource("/com/chirag/views/styles.css");
        if (cssUrl != null) {
            alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
        }
        alert.showAndWait();
    }
}
