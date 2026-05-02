package com.chirag.controllers;

import com.chirag.models.Course;
import com.chirag.models.Enrollment;
import com.chirag.models.User;
import com.chirag.repositories.CourseRepository;
import com.chirag.repositories.EnrollmentRepository;
import com.chirag.utils.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.util.List;

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

    private CourseRepository courseRepository;
    private EnrollmentRepository enrollmentRepository;

    /**
     * Sets up the dependencies for fetching courses.
     * Use-case: Manage Creator Dashboard.
     */
    public DashboardController() {
        this.courseRepository = new CourseRepository();
        this.enrollmentRepository = new EnrollmentRepository();
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
            
            // Fetch uploaded courses
            List<Course> instructorCourses = courseRepository.findByInstructor(user);
            System.out.println("Loaded " + instructorCourses.size() + " uploaded courses.");
            
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
                
                Label prog = new Label(enr.isCompleted() ? "Status: Completed" : "Status: In Progress");
                prog.setStyle("-fx-text-fill: " + (enr.isCompleted() ? "#2D6A4F" : "#8D99AE") + ";");
                
                card.getChildren().addAll(title, prog);
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
