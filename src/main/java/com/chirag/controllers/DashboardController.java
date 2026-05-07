package com.chirag.controllers;

import com.chirag.models.Course;
import com.chirag.models.Enrollment;
import com.chirag.models.Transaction;
import com.chirag.models.User;
import com.chirag.repositories.CourseRepository;
import com.chirag.repositories.EnrollmentRepository;
import com.chirag.repositories.TransactionRepository;
import com.chirag.utils.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.chirag.models.Lecture;
import com.chirag.repositories.LectureRepository;


/**
 * Central hub controller for the unified dashboard.
 * Loads both learner and teacher data in one screen.
 * Use-cases: Manage Creator Dashboard, Consume Content.
 */
public class DashboardController {

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

    private CourseRepository courseRepository;
    private EnrollmentRepository enrollmentRepository;
    private TransactionRepository transactionRepository;
    private LectureRepository lectureRepository;

    /**
     * Sets up the dependencies for fetching courses.
     * Use-case: Manage Creator Dashboard.
     */
    public DashboardController() {
        this.courseRepository = new CourseRepository();
        this.enrollmentRepository = new EnrollmentRepository();
        this.transactionRepository = new TransactionRepository();
        this.lectureRepository = new LectureRepository();
    }

    /**
     * Called automatically by JavaFX. Loads session state.
     * Use-case: Manage Creator Dashboard, Consume Content.
     */
    @FXML
     public void initialize() {
        User user = UserSession.getCurrentUser();
        if (user != null) {
            welcomeLabel.setText("Welcome, " + user.getName() + "!");
            walletLabel.setText("Wallet Balance: $" + String.format("%.2f", user.getVirtualWalletBalance()));

            // --- 1. Fetch uploaded courses (Creator Studio) ---
            List<Course> instructorCourses = courseRepository.findByInstructor(user);
            uploadedCoursesContainer.getChildren().clear();
            for (Course c : instructorCourses) {
                VBox card = new VBox(10.0); // Increased spacing
                card.getStyleClass().add("item-card");
                card.setPrefWidth(300.0); // Wider card for enterprise feel

                Label title = new Label(c.getTitle());
                title.setWrapText(true); // Let long titles wrap
                title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1B263B;");

                Label status = new Label();
                if (!c.isActive()) {
                    status.setText("Status: Suspended by Admin");
                    status.setStyle("-fx-text-fill: #E63946; -fx-font-weight: bold;");
                } else {
                    status.setText("Status: " + c.getStatus().toString());
                    status.setStyle("-fx-text-fill: #2D6A4F;");
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

            // --- 2. Fetch enrolled courses (My Learning) ---
            // Fetch enrolled courses from db
            List<Enrollment> enrollments = enrollmentRepository.findByUser(user);
            enrolledCoursesContainer.getChildren().clear();
            for (Enrollment enr : enrollments) {
                Course c = enr.getCourse();
                VBox card = new VBox(5.0);
                card.getStyleClass().add("item-card");
                card.setPrefWidth(250.0);

                Label title = new Label(c.getTitle());
                title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1B263B;");

                double progressValue = 0.0;
                try {
                    // Get the total number of lectures for this specific course
                    int totalLectures = lectureRepository.getDao().queryBuilder()
                            .where().eq("course_id", c.getId()).query().size();

                    // Delegate the calculation logic to the Enrollment model (Information Expert)
                    progressValue = enr.calculateProgressPercentage(totalLectures);
                } catch (Exception ex) {
                    System.err.println("Error calculating progress math: " + ex.getMessage());
                }

                Label percentLabel = new Label("Progress: " + (int)progressValue + "%");
                percentLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1B263B;");

                Label progStatus = new Label(enr.isCompleted() ? "Status: Completed" : "Status: In Progress");
                progStatus.setStyle("-fx-text-fill: " + (enr.isCompleted() ? "#2D6A4F" : "#8D99AE") + ";");

                // Add the title, percentage, and status to the card
                card.getChildren().addAll(title, percentLabel, progStatus);

                card.setOnMouseClicked(e -> {
                    Object ctrl = com.chirag.utils.SceneManager.getInstance().switchScene("CoursePlayerView.fxml");
                    if (ctrl instanceof CoursePlayerController) {
                        ((CoursePlayerController) ctrl).setCourse(c);
                    }
                });
                card.setStyle(card.getStyle() + "-fx-cursor: hand;");
                enrolledCoursesContainer.getChildren().add(card);
            }

            // Fetch recent transactions
            loadTransactions(user);
        }
    }

    /**
     * Loads the recent transactions for the user.
     * Use-case: Dashboard Refresh, Purchase History.
     */
    private void loadTransactions(User user) {
        try {
            com.j256.ormlite.stmt.QueryBuilder<Transaction, Integer> qb = transactionRepository.getDao().queryBuilder();
            qb.orderBy("transactionDate", false);
            qb.where().eq("buyer_id", user.getId()).or().eq("instructor_id", user.getId());

            List<Transaction> transactions = qb.query();
            int limit = Math.min(5, transactions.size());
            transactionsList.getItems().clear();

            for (int i = 0; i < limit; i++) {
                Transaction t = transactions.get(i);
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
                            setStyle("-fx-text-fill: #2D6A4F; -fx-font-weight: bold;"); // Green for income
                        } else if (item.startsWith("-$")) {
                            setStyle("-fx-text-fill: #E63946; -fx-font-weight: bold;"); // Red for expense
                        } else {
                            setStyle("-fx-text-fill: #1B263B;");
                        }
                    }
                }
            });

        } catch (java.sql.SQLException e) {
            System.err.println("Failed to load transactions: " + e.getMessage());
        }
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
            System.err.println("Failed to open wallet popup: " + e.getMessage());
        }
    }

    /**
     * Refreshes the wallet label and transactions after a top up.
     * Use-case: Top Up Virtual Wallet, Dashboard Refresh.
     */
    public void refreshWalletDisplay() {
        User user = UserSession.getCurrentUser();
        if (user != null) {
            walletLabel.setText("Wallet Balance: $" + String.format("%.2f", user.getVirtualWalletBalance()));
            loadTransactions(user);
        }
    }

    /**
     * Carries out the logout event by wiping session.
     * Use-case: System Shutdown.
     */
    @FXML
    public void handleLogout(ActionEvent event) {
        UserSession.clear();
        System.out.println("User Logged Out");
        com.chirag.utils.SceneManager.getInstance().switchScene("LoginView.fxml");
    }
}
