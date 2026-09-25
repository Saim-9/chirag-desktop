package com.chirag.controllers;

import com.chirag.models.Course;
import com.chirag.models.Enrollment;
import com.chirag.models.Transaction;
import com.chirag.models.User;
import com.chirag.services.DashboardService;
import com.chirag.utils.UserSession;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Central hub controller for the unified dashboard.
 * Loads both learner and teacher data in one screen.
 * All DB fetches run on background threads to prevent UI lag.
 * Use-cases: Manage Creator Dashboard, Consume Content.
 */
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label walletLabel;

    @FXML
    private FlowPane enrolledCoursesContainer;

    @FXML
    private FlowPane uploadedCoursesContainer;

    @FXML
    private ListView<String> transactionsList;

    private DashboardService dashboardService;

    /**
     * Default constructor for fallback.
     */
    public DashboardController() {
    }

    /**
     * Injected constructor for fetching courses.
     * Use-case: Manage Creator Dashboard.
     */
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Called automatically by JavaFX. Loads session state.
     * DB-heavy operations are dispatched to background threads.
     * Use-case: Manage Creator Dashboard, Consume Content.
     */
    @FXML
     public void initialize() {
        User user = UserSession.getCurrentUser();
        if (user != null) {
            // These are instant — no DB call
            welcomeLabel.setText("Welcome, " + user.getName() + "!");
            walletLabel.setText("Wallet Balance: $" + String.format("%.2f", user.getVirtualWalletBalance()));

            // Load data asynchronously to prevent UI freeze
            loadUploadedCoursesAsync(user);
            loadEnrolledCoursesAsync(user);
            loadTransactionsAsync(user);
        }
    }

    /**
     * Loads instructor courses — serves from cache if available.
     */
    private void loadUploadedCoursesAsync(User user) {
        com.chirag.utils.DataCache dc = com.chirag.utils.DataCache.getInstance();
        List<Course> cached = dc.get(com.chirag.utils.DataCache.instructorCourses(user.getId()));
        if (cached != null) {
            renderUploadedCourses(cached);
            return;
        }

        Task<List<Course>> task = new Task<>() {
            @Override
            protected List<Course> call() {
                return dashboardService.getInstructorCourses(user);
            }
        };
        task.setOnSucceeded(e -> {
            dc.put(com.chirag.utils.DataCache.instructorCourses(user.getId()), task.getValue());
            renderUploadedCourses(task.getValue());
        });
        task.setOnFailed(e -> logger.error("Uploaded courses load failed: {}", task.getException().getMessage()));
        new Thread(task, "dashboard-uploads-loader").start();
    }

    /**
     * Loads enrolled courses AND lecture counts — serves from cache if available.
     */
    private volatile java.util.Map<Integer, Integer> lectureCounts = new java.util.HashMap<>();

    private void loadEnrolledCoursesAsync(User user) {
        com.chirag.utils.DataCache dc = com.chirag.utils.DataCache.getInstance();
        List<Enrollment> cached = dc.get(com.chirag.utils.DataCache.enrolledCourses(user.getId()));
        java.util.Map<Integer, Integer> cachedCounts = dc.get(com.chirag.utils.DataCache.lectureCounts(user.getId()));
        if (cached != null && cachedCounts != null) {
            renderEnrolledCourses(cached, user, cachedCounts);
            return;
        }

        Task<List<Enrollment>> task = new Task<>() {
            @Override
            protected List<Enrollment> call() {
                List<Enrollment> enrollments = dashboardService.getEnrolledCourses(user);
                // Pre-fetch lecture counts for all enrolled courses in 1 query
                java.util.List<Integer> courseIds = enrollments.stream()
                        .map(e -> e.getCourse().getId()).toList();
                lectureCounts = dashboardService.getLectureCountsMap(courseIds);
                return enrollments;
            }
        };
        task.setOnSucceeded(e -> {
            dc.put(com.chirag.utils.DataCache.enrolledCourses(user.getId()), task.getValue());
            dc.put(com.chirag.utils.DataCache.lectureCounts(user.getId()), lectureCounts);
            renderEnrolledCourses(task.getValue(), user, lectureCounts);
        });
        task.setOnFailed(e -> logger.error("Enrolled courses load failed: {}", task.getException().getMessage()));
        new Thread(task, "dashboard-enrollments-loader").start();
    }

    /**
     * Loads transactions — serves from cache if available.
     */
    private void loadTransactionsAsync(User user) {
        com.chirag.utils.DataCache dc = com.chirag.utils.DataCache.getInstance();
        List<Transaction> cached = dc.get(com.chirag.utils.DataCache.transactions(user.getId()));
        if (cached != null) {
            renderTransactions(cached, user);
            return;
        }

        Task<List<Transaction>> task = new Task<>() {
            @Override
            protected List<Transaction> call() {
                return dashboardService.getRecentTransactions(user, 5);
            }
        };
        task.setOnSucceeded(e -> {
            dc.put(com.chirag.utils.DataCache.transactions(user.getId()), task.getValue());
            renderTransactions(task.getValue(), user);
        });
        task.setOnFailed(e -> logger.error("Transactions load failed: {}", task.getException().getMessage()));
        new Thread(task, "dashboard-transactions-loader").start();
    }

    // ============================================================
    // RENDER METHODS — Run on FX thread after async fetch
    // ============================================================

    /**
     * Renders uploaded courses (Creator Studio).
     */
    private void renderUploadedCourses(List<Course> instructorCourses) {
        uploadedCoursesContainer.getChildren().clear();
        for (Course c : instructorCourses) {
            VBox card = new VBox(10.0); // Increased spacing
            card.getStyleClass().add("item-card");
            card.setPrefWidth(300.0); // Wider card for enterprise feel

            Label title = new Label(c.getTitle());
            title.setWrapText(true); // Let long titles wrap
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1B2A4A;");

            Label status = new Label();
            if (!c.isActive()) {
                status.setText("Status: Suspended by Admin");
                status.setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold;");
            } else {
                status.setText("Status: " + c.getStatus().toString());
                status.setStyle("-fx-text-fill: #27714A;");
            }

            Button editBtn = new Button("Edit Course");
            editBtn.getStyleClass().add("dynamic-btn");

            if (!c.isActive()) {
                editBtn.setDisable(true);
            }

            editBtn.setOnAction(e -> {
                Object ctrl = com.chirag.utils.SceneManager.getInstance().switchScene("EditCourseView.fxml");
                if (ctrl instanceof EditCourseController) {
                    ((EditCourseController) ctrl).setCourse(c);
                }
            });

            card.getChildren().addAll(title, status, editBtn);
            uploadedCoursesContainer.getChildren().add(card);
        }
    }

    /**
     * Renders enrolled courses (My Learning).
     * Uses pre-fetched lecture counts — no DB calls on the UI thread.
     */
    private void renderEnrolledCourses(List<Enrollment> enrollments, User user,
                                       java.util.Map<Integer, Integer> lectureCounts) {
        enrolledCoursesContainer.getChildren().clear();
        for (Enrollment enr : enrollments) {
            Course c = enr.getCourse();
            VBox card = new VBox(5.0);
            card.getStyleClass().add("item-card");
            card.setPrefWidth(250.0);

            Label title = new Label(c.getTitle());
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1B2A4A;");

            double progressValue = 0.0;
            try {
                // Use pre-fetched count (no DB call on UI thread)
                int totalLectures = lectureCounts.getOrDefault(c.getId(), 0);

                // Delegate the calculation logic to the Enrollment model (Information Expert)
                progressValue = enr.calculateProgressPercentage(totalLectures);
            } catch (Exception ex) {
                logger.error("Error calculating progress: {}", ex.getMessage());
            }

            Label percentLabel = new Label("Progress: " + (int)progressValue + "%");
            percentLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1B2A4A;");

            Label progStatus = new Label(enr.isCompleted() ? "Status: Completed" : "Status: In Progress");
            progStatus.setStyle("-fx-text-fill: " + (enr.isCompleted() ? "#27714A" : "#8A8A8A") + ";");

            // Add the title, percentage, and status to the card
            card.getChildren().addAll(title, percentLabel, progStatus);

            // Show certificate button for completed courses
            if (enr.isCompleted()) {
                Button certBtn = new Button("View Certificate");
                certBtn.getStyleClass().add("button-gold");
                certBtn.setStyle("-fx-padding: 5 12; -fx-font-size: 11px;");
                certBtn.setOnAction(ev -> {
                    new com.chirag.services.CertificateService().showCertificate(
                            user, c, enr);
                });
                card.getChildren().add(certBtn);
            }

            card.setOnMouseClicked(e -> {
                Object ctrl = com.chirag.utils.SceneManager.getInstance().switchScene("CoursePlayerView.fxml");
                if (ctrl instanceof CoursePlayerController) {
                    ((CoursePlayerController) ctrl).setCourse(c);
                }
            });
            card.setStyle(card.getStyle() + "-fx-cursor: hand;");
            enrolledCoursesContainer.getChildren().add(card);
        }
    }

    /**
     * Renders transactions list with color-coded income/expense.
     * Use-case: Dashboard Refresh, Purchase History.
     */
    private void renderTransactions(List<Transaction> transactions, User user) {
        transactionsList.getItems().clear();

        for (Transaction t : transactions) {
            boolean isIncome = (t.getInstructor() != null && t.getInstructor().getId() == user.getId()
                    && (t.getBuyer() == null || t.getBuyer().getId() != user.getId()));

            if (isIncome) {
                transactionsList.getItems()
                        .add("+$" + String.format("%.2f", t.getNetAmount()) + " (" + t.getDescription() + ")");
            } else {
                transactionsList.getItems()
                        .add("-$" + String.format("%.2f", t.getAmount()) + " (" + t.getDescription() + ")");
            }
        }

        transactionsList.setCellFactory(lv -> new javafx.scene.control.ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.startsWith("+$")) {
                        setStyle("-fx-text-fill: #27714A; -fx-font-weight: bold;"); // Green for income
                    } else if (item.startsWith("-$")) {
                        setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold;"); // Red for expense
                    } else {
                        setStyle("-fx-text-fill: #1B2A4A;");
                    }
                }
            }
        });
    }

    /**
     * Navigates to the marketplace to buy courses.
     * Use-case: Course Catalog.
     */
    @FXML
    public void goToMarketplace(ActionEvent event) {
        com.chirag.utils.SceneManager.getInstance().switchScene("MarketplaceView.fxml");
    }

    /**
     * Navigates to the creator studio to upload.
     * Use-case: Course Creation.
     */
    @FXML
    public void goToCreatorStudio(ActionEvent event) {
        com.chirag.utils.SceneManager.getInstance().switchScene("CourseCreationView.fxml");
    }

    /**
     * Navigates to the user profile screen.
     * Use-case: Profile View.
     */
    @FXML
    public void goToProfile(ActionEvent event) {
        com.chirag.utils.SceneManager.getInstance().switchScene("ProfileView.fxml");
    }
    /**
     * Opens the modal window to top up virtual wallet.
     * Use-case: Top Up Virtual Wallet.
     */
    @FXML
    public void openWalletPopup(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/chirag/views/WalletPopupView.fxml"));
            Parent root = loader.load();

            WalletPopupController popupController = loader.getController();
            popupController.setParentController(this);

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Top Up Wallet");
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();
        } catch (Exception e) {
            logger.error("Failed to open wallet popup: {}", e.getMessage());
        }
    }

    /**
     * Refreshes the wallet label and transactions after a top up.
     * Use-case: Top Up Virtual Wallet, Dashboard Refresh.
     */
    public void refreshWalletDisplay() {
        User user = UserSession.getCurrentUser();
        if (user != null) {
            // Invalidate transaction cache so fresh data is fetched
            com.chirag.utils.DataCache.getInstance().invalidate(
                    com.chirag.utils.DataCache.transactions(user.getId()));
            walletLabel.setText("Wallet Balance: $" + String.format("%.2f", user.getVirtualWalletBalance()));
            loadTransactionsAsync(user);
        }
    }

    /**
     * Carries out the logout event by wiping session.
     * Use-case: System Shutdown.
     */
    @FXML
    public void handleLogout(ActionEvent event) {
        // Clear all cached data so next user starts fresh
        com.chirag.utils.DataCache.getInstance().invalidateAll();
        UserSession.clearSession();
        logger.info("User logged out");
        com.chirag.utils.SceneManager.getInstance().switchScene("LoginView.fxml");
    }
}
