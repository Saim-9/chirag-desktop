package com.chirag.controllers;

import com.chirag.models.*;
import com.chirag.repositories.*;
import com.chirag.utils.SceneManager;
import com.chirag.utils.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.sql.SQLException;
import java.util.List;
import com.chirag.services.UserService;

/**
 * Controller for the Admin Command Center.
 * Use-cases: View Revenue, Manage Users, Resolve Reports.
 */
public class AdminDashboardController {

    @FXML private Label totalRevenueLabel;
    @FXML private BarChart<String, Number> revenueChart;
    @FXML private VBox reportsContainer;
    @FXML private VBox usersContainer;
    
    @FXML private TextField adminNameField;
    @FXML private TextField adminEmailField;
    @FXML private PasswordField adminPasswordField;

    private TransactionRepository transactionRepository;
    private ReportRepository reportRepository;
    private UserRepository userRepository;
    private CourseRepository courseRepository;
    private UserService userService;

    public AdminDashboardController() {
        this.transactionRepository = new TransactionRepository();
        this.reportRepository = new ReportRepository();
        this.userRepository = new UserRepository();
        this.courseRepository = new CourseRepository();
        this.userService = new UserService();
    }

    @FXML
    public void initialize() {
        loadFinancials();
        loadReports();
        loadUsers();
    }

    /**
     * Calculates 10% platform cut and displays transaction volume.
     * Use-case: View Revenue.
     */
    private void loadFinancials() {
        try {
            List<Transaction> txs = transactionRepository.getDao().queryForAll();
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
        } catch (SQLException e) {
            System.err.println("Financial error: " + e.getMessage());
        }
    }

    /**
     * Lists all reports with Dismiss and Take Down options.
     * Use-case: Moderation.
     */
    private void loadReports() {
        reportsContainer.getChildren().clear();
        try {
            List<Report> reports = reportRepository.getDao().queryBuilder().where().eq("status", "PENDING").query();
            for (Report r : reports) {
                HBox row = new HBox(15.0);
                row.setStyle("-fx-padding: 10; -fx-background-color: #FAF8F5; -fx-border-color: #E0DCD3;");
                row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                Label text = new Label("Report #" + r.getId() + ": " + r.getReportedCourse().getTitle() + " - " + r.getComplaintText());
                text.setWrapText(true);
                HBox.setHgrow(text, Priority.ALWAYS);

                Button dismissBtn = new Button("Dismiss");
                dismissBtn.setOnAction(e -> {
                    try {
                        reportRepository.getDao().delete(r);
                        loadReports();
                    } catch (SQLException ex) {}
                });

                Button takeDownBtn = new Button("Take Down");
                takeDownBtn.setStyle("-fx-background-color: #E63946; -fx-text-fill: white;");
                takeDownBtn.setOnAction(e -> {
                    try {
                        Course c = r.getReportedCourse();
                        c.setActive(false);
                        courseRepository.getDao().update(c);
                        r.setStatus("RESOLVED");
                        reportRepository.getDao().update(r);
                        loadReports();
                    } catch (SQLException ex) {}
                });

                row.getChildren().addAll(text, dismissBtn, takeDownBtn);
                reportsContainer.getChildren().add(row);
            }
        } catch (SQLException e) {
            System.err.println("Report loading error: " + e.getMessage());
        }
    }

    /**
     * Lists all users with Suspend functionality.
     * Use-case: User Management.
     */
    private void loadUsers() {
        usersContainer.getChildren().clear();
        try {
            List<User> users = userRepository.getDao().queryForAll();
            for (User u : users) {
                if ("ADMIN".equals(u.getRole())) continue;

                HBox row = new HBox(15.0);
                row.getStyleClass().add("admin-row");
                row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                Label text = new Label(u.getName() + " (" + u.getEmail() + ") - Status: " + u.getAccountStatus());
                HBox.setHgrow(text, Priority.ALWAYS);

                Button suspendBtn = new Button("Suspend");
                suspendBtn.getStyleClass().add("dynamic-btn");
                suspendBtn.setDisable("SUSPENDED".equals(u.getAccountStatus()));
                suspendBtn.setOnAction(e -> {
                    try {
                        u.setAccountStatus("SUSPENDED");
                        userRepository.getDao().update(u);
                        // Cascading Soft Delete
                        List<Course> courses = courseRepository.getDao().queryBuilder().where().eq("instructor_id", u.getId()).query();
                        for (Course c : courses) {
                            c.setActive(false);
                            courseRepository.getDao().update(c);
                        }
                        loadUsers();
                    } catch (SQLException ex) {}
                });

                row.getChildren().addAll(text, suspendBtn);
                usersContainer.getChildren().add(row);
            }
        } catch (SQLException e) {
            System.err.println("User loading error: " + e.getMessage());
        }
    }

    /**
     * Creates a new admin user directly.
     * Use-case: User Management.
     */
    @FXML
    public void handleCreateAdmin(ActionEvent event) {
        String name = adminNameField.getText();
        String email = adminEmailField.getText();
        String pass = adminPasswordField.getText();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) return;

        User newAdmin = new User();
        newAdmin.setName(name);
        newAdmin.setEmail(email);
        newAdmin.setPassword(pass); // Still plain text here, UserService will intercept it

        // Send it to the Service layer for hashing and saving
        boolean success = userService.registerNewAdmin(newAdmin);

        if (success) {
            adminNameField.clear();
            adminEmailField.clear();
            adminPasswordField.clear();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("New administrator created successfully.");
            java.net.URL cssUrl = getClass().getResource("/com/chirag/views/styles.css");
            if (cssUrl != null) {
                alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
            }
            alert.showAndWait();

            loadUsers();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not create admin. Email might be taken.");
            java.net.URL cssUrl = getClass().getResource("/com/chirag/views/styles.css");
            if (cssUrl != null) {
                alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
            }
            alert.showAndWait();
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        UserSession.logout();
        SceneManager.getInstance().switchScene("LoginView.fxml");
    }
}
