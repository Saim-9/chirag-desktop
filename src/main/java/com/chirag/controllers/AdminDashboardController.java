package com.chirag.controllers;

import com.chirag.models.*;
import com.chirag.services.AdminService;
import com.chirag.services.UserService;
import com.chirag.utils.SceneManager;
import com.chirag.utils.UserSession;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.List;

/**
 * Controller for the Admin Command Center.
 * Delegates all business logic to AdminService (proper 3-tier architecture).
 * Super Admin gets an extra tab to manage/delete other admins.
 * All DB fetches run on background threads to prevent UI lag.
 * Use-cases: View Revenue, Manage Users, Resolve Reports, Super Admin Management.
 */
public class AdminDashboardController {

    @FXML private Label totalRevenueLabel;
    @FXML private BarChart<String, Number> revenueChart;
    @FXML private VBox reportsContainer;
    @FXML private VBox usersContainer;
    @FXML private VBox adminsContainer;
    @FXML private TabPane mainTabPane;
    @FXML private Tab manageAdminsTab;
    
    @FXML private TextField adminNameField;
    @FXML private TextField adminEmailField;
    @FXML private PasswordField adminPasswordField;

    private AdminService adminService;
    private UserService userService;

    public AdminDashboardController() {
        this.adminService = new AdminService();
        this.userService = new UserService();
    }

    @FXML
    public void initialize() {
        // Hide the "Manage Admins" tab if the current user is NOT the super admin
        User currentUser = UserSession.getCurrentUser();
        if (!adminService.isSuperAdmin(currentUser)) {
            mainTabPane.getTabs().remove(manageAdminsTab);
        }

        // Load all data on background threads to avoid UI freeze
        loadFinancialsAsync();
        loadReportsAsync();
        loadUsersAsync();

        if (adminService.isSuperAdmin(currentUser)) {
            loadAdminsAsync();
        }
    }

    // ============================================================
    // ASYNC DATA LOADING — Prevents UI freeze on DB fetches
    // ============================================================

    /**
     * Loads financial data on a background thread.
     */
    private void loadFinancialsAsync() {
        Task<List<Transaction>> task = new Task<>() {
            @Override
            protected List<Transaction> call() {
                return adminService.getAllTransactions();
            }
        };
        task.setOnSucceeded(e -> renderFinancials(task.getValue()));
        task.setOnFailed(e -> System.err.println("Financial load failed: " + task.getException()));
        new Thread(task, "admin-financials-loader").start();
    }

    /**
     * Loads reports on a background thread.
     */
    private void loadReportsAsync() {
        Task<List<Report>> task = new Task<>() {
            @Override
            protected List<Report> call() {
                return adminService.getPendingReports();
            }
        };
        task.setOnSucceeded(e -> renderReports(task.getValue()));
        task.setOnFailed(e -> System.err.println("Reports load failed: " + task.getException()));
        new Thread(task, "admin-reports-loader").start();
    }

    /**
     * Loads users on a background thread.
     */
    private void loadUsersAsync() {
        Task<List<User>> task = new Task<>() {
            @Override
            protected List<User> call() {
                return adminService.getAllRegularUsers();
            }
        };
        task.setOnSucceeded(e -> renderUsers(task.getValue()));
        task.setOnFailed(e -> System.err.println("Users load failed: " + task.getException()));
        new Thread(task, "admin-users-loader").start();
    }

    /**
     * Loads other admins on a background thread (super admin only).
     */
    private void loadAdminsAsync() {
        User current = UserSession.getCurrentUser();
        Task<List<User>> task = new Task<>() {
            @Override
            protected List<User> call() {
                return adminService.getOtherAdmins(current);
            }
        };
        task.setOnSucceeded(e -> renderAdmins(task.getValue()));
        task.setOnFailed(e -> System.err.println("Admins load failed: " + task.getException()));
        new Thread(task, "admin-admins-loader").start();
    }

    // ============================================================
    // RENDER METHODS — Run on FX thread after async fetch
    // ============================================================

    /**
     * Calculates 10% platform cut and displays transaction volume.
     * Use-case: View Revenue.
     */
    private void renderFinancials(List<Transaction> txs) {
        double totalRevenue = 0;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Transaction Cut");

        for (Transaction t : txs) {
            double cut = t.getPlatformFee();
            totalRevenue += cut;
            series.getData().add(new XYChart.Data<>(t.getTransactionDate().toString(), cut));
        }
        
        totalRevenueLabel.setText("Total Platform Revenue: $" + String.format("%.2f", totalRevenue));
        revenueChart.getData().clear();
        revenueChart.getData().add(series);
    }

    /**
     * Lists all reports with Dismiss and Take Down options.
     * Use-case: Moderation.
     */
    private void renderReports(List<Report> reports) {
        reportsContainer.getChildren().clear();

        for (Report r : reports) {
            HBox row = new HBox(15.0);
            row.setStyle("-fx-padding: 10; -fx-background-color: #FAF8F5; -fx-border-color: #E0DCD3;");
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label text = new Label("Report #" + r.getId() + ": " + r.getReportedCourse().getTitle() + " - " + r.getComplaintText());
            text.setWrapText(true);
            HBox.setHgrow(text, Priority.ALWAYS);

            Button dismissBtn = new Button("Dismiss");
            dismissBtn.getStyleClass().add("dynamic-btn");
            dismissBtn.setOnAction(e -> {
                adminService.dismissReport(r);
                loadReportsAsync();
            });

            Button takeDownBtn = new Button("Take Down");
            takeDownBtn.setStyle("-fx-background-color: #E63946; -fx-text-fill: white;");
            takeDownBtn.getStyleClass().add("dynamic-btn");
            takeDownBtn.setOnAction(e -> {
                adminService.takeDownCourse(r);
                loadReportsAsync();
            });

            row.getChildren().addAll(text, dismissBtn, takeDownBtn);
            reportsContainer.getChildren().add(row);
        }
    }

    /**
     * Lists all regular users with Suspend functionality.
     * Use-case: User Management.
     */
    private void renderUsers(List<User> users) {
        usersContainer.getChildren().clear();

        for (User u : users) {
            HBox row = new HBox(15.0);
            row.getStyleClass().add("admin-row");
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label text = new Label(u.getName() + " (" + u.getEmail() + ") - Status: " + u.getAccountStatus());
            HBox.setHgrow(text, Priority.ALWAYS);

            Button suspendBtn = new Button("Suspend");
            suspendBtn.getStyleClass().add("dynamic-btn");
            suspendBtn.setDisable("SUSPENDED".equals(u.getAccountStatus()));
            suspendBtn.setOnAction(e -> {
                adminService.suspendUser(u);
                loadUsersAsync();
            });

            row.getChildren().addAll(text, suspendBtn);
            usersContainer.getChildren().add(row);
        }
    }

    /**
     * Lists all other admin users with Delete option (super admin only).
     * Use-case: Super Admin Management.
     */
    private void renderAdmins(List<User> admins) {
        adminsContainer.getChildren().clear();

        if (admins.isEmpty()) {
            Label emptyLabel = new Label("No other administrators found.");
            emptyLabel.setStyle("-fx-text-fill: #8D99AE; -fx-font-style: italic;");
            adminsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (User admin : admins) {
            HBox row = new HBox(15.0);
            row.getStyleClass().add("admin-row");
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label text = new Label(admin.getName() + " (" + admin.getEmail() + ")");
            text.setStyle("-fx-font-weight: bold;");
            HBox.setHgrow(text, Priority.ALWAYS);

            Button deleteBtn = new Button("Remove Admin");
            deleteBtn.setStyle("-fx-background-color: #E63946; -fx-text-fill: white;");
            deleteBtn.getStyleClass().add("dynamic-btn");
            deleteBtn.setOnAction(e -> {
                // Confirmation dialog before deletion
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirm Deletion");
                confirm.setHeaderText(null);
                confirm.setContentText("Are you sure you want to permanently remove admin '" + admin.getName() + "' (" + admin.getEmail() + ")?\n\nThis action cannot be undone.");
                java.net.URL cssUrl = getClass().getResource("/com/chirag/views/styles.css");
                if (cssUrl != null) {
                    confirm.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
                }

                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        boolean success = adminService.deleteAdmin(admin);
                        if (success) {
                            showAlert(Alert.AlertType.INFORMATION, "Admin Removed",
                                    "Administrator '" + admin.getName() + "' has been removed.");
                            loadAdminsAsync();
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Deletion Failed",
                                    "Could not remove the administrator. Please try again.");
                        }
                    }
                });
            });

            row.getChildren().addAll(text, deleteBtn);
            adminsContainer.getChildren().add(row);
        }
    }

    /**
     * Creates a new admin user with proper validation.
     * Validates email format and password strength before delegating to UserService.
     * Use-case: User Management.
     */
    @FXML
    public void handleCreateAdmin(ActionEvent event) {
        String name = adminNameField.getText().trim();
        String email = adminEmailField.getText().trim();
        String pass = adminPasswordField.getText();

        // Check for empty fields
        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields",
                    "Please fill in all fields (Name, Email, Password).");
            return;
        }

        // Email format validation (matches RegisterController pattern)
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert(Alert.AlertType.ERROR, "Invalid Email",
                    "Please enter a valid email address (e.g., admin@example.com).");
            highlightField(adminEmailField);
            return;
        }

        // Password strength validation (minimum 6 characters)
        if (pass.length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Weak Password",
                    "Password must be at least 6 characters long.");
            highlightField(adminPasswordField);
            return;
        }

        User newAdmin = new User();
        newAdmin.setName(name);
        newAdmin.setEmail(email);
        newAdmin.setPassword(pass); // UserService will hash it

        // Send it to the Service layer for hashing and saving
        boolean success = userService.registerNewAdmin(newAdmin);

        if (success) {
            adminNameField.clear();
            adminEmailField.clear();
            adminPasswordField.clear();
            // Reset any error highlights
            adminEmailField.setStyle("");
            adminPasswordField.setStyle("");

            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "New administrator '" + name + "' created successfully.");
            loadUsersAsync();
            // Refresh admin list if super admin
            if (adminService.isSuperAdmin(UserSession.getCurrentUser())) {
                loadAdminsAsync();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Could not create admin. Email might already be taken.");
        }
    }

    /**
     * Spawns a modern, CSS-styled alert dialog.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
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

    /**
     * Highlights an invalid input field with a red border.
     */
    private void highlightField(TextField field) {
        field.setStyle("-fx-border-color: #E63946; -fx-border-width: 2px;");
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        UserSession.clearSession();
        SceneManager.getInstance().switchScene("LoginView.fxml");
    }
}
