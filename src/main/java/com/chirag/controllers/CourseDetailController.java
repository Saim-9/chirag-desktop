package com.chirag.controllers;

import com.chirag.models.Course;
import com.chirag.services.ContentPlayerService;
import com.chirag.services.CourseInteractionService;
import com.chirag.services.PaymentServiceImpl;
import com.chirag.utils.SceneManager;
import com.chirag.utils.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.util.List;

/**
 * View details of a specific course and handles purchase.
 * Uses ContentPlayerService for enrollment checks (no direct repo access).
 * Use-cases: Course View, Course Purchase, Enrollment Verification.
 */
public class CourseDetailController {

    @FXML
    private Label titleLabel;
    @FXML
    private Label instructorLabel;
    @FXML
    private FlowPane tagsFlowPane;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label priceLabel;
    @FXML
    private Button buyButton;
    @FXML
    private Label statusMsgLabel;
    @FXML
    private VBox lecturesList;
    @FXML
    private Label ratingLabel;
    @FXML
    private VBox reviewsList;

    private Course currentCourse;
    private PaymentServiceImpl paymentServiceImpl;
    private CourseInteractionService interactionService;
    private ContentPlayerService contentPlayerService;

    /**
     * Default constructor for fallback.
     */
    public CourseDetailController() {
        this.paymentServiceImpl = new PaymentServiceImpl();
        this.contentPlayerService = new ContentPlayerService();
    }

    /**
     * Injected constructor for course interactions.
     * Use-case: Course Purchase.
     */
    public CourseDetailController(CourseInteractionService interactionService) {
        this.paymentServiceImpl = new PaymentServiceImpl();
        this.interactionService = interactionService;
        this.contentPlayerService = new ContentPlayerService();
    }

    /**
     * Injects the course data into the view after loading.
     * Also checks enrollment status to show correct button text.
     * Use-case: Course View, Enrollment Verification.
     */
    public void setCourse(Course course) {
        this.currentCourse = course;
        titleLabel.setText(course.getTitle());
        String ins = course.getInstructor() != null ? course.getInstructor().getName() : "Unknown";
        instructorLabel.setText("By " + ins);
        descriptionLabel.setText(course.getDescription());
        priceLabel.setText("$" + String.format("%.2f", course.getPrice()));

        if (course.getTags() != null && !course.getTags().isEmpty()) {
            for (String tag : course.getTags().split(",")) {
                Label tLbl = new Label(tag.trim());
                tLbl.getStyleClass().add("tag-chip");
                tagsFlowPane.getChildren().add(tLbl);
            }
        }

        // Enrollment verification: show "Go to Course" if already enrolled
        configureActionButton();

        loadLectures();
        loadReviews();
    }

    /**
     * Configures the buy button based on enrollment and ownership status.
     * - Own course: disables button with "Your Course" text
     * - Already enrolled: shows "Go to Course" button
     * - Not enrolled: shows "Buy Course for $X.XX"
     * Use-case: Enrollment Verification.
     */
    private void configureActionButton() {
        // Block course creator from seeing Buy
        if (currentCourse.getInstructor() != null &&
            UserSession.getCurrentUser().getId() == currentCourse.getInstructor().getId()) {
            buyButton.setText("Your Course — Manage from Dashboard");
            buyButton.setDisable(true);
            buyButton.setStyle("-fx-opacity: 0.7;");
            return;
        }

        // Check enrollment via service layer (no direct repo access)
        boolean isEnrolled = contentPlayerService.isUserEnrolled(
                UserSession.getCurrentUser(), currentCourse);

        if (isEnrolled) {
            buyButton.setText("Go to Course");
            buyButton.setStyle("-fx-background-color: #27714A; -fx-text-fill: white;");
            statusMsgLabel.setText("[Done] You are enrolled in this course");
            statusMsgLabel.setTextFill(javafx.scene.paint.Color.web("#27714A"));
        } else {
            buyButton.setText("Buy Course for $" + String.format("%.2f", currentCourse.getPrice()));
        }
    }

    /**
     * Fetch lectures from service and display them.
     * Use-case: Course View.
     */
    private void loadLectures() {
        lecturesList.getChildren().clear();

        List<com.chirag.models.Lecture> lecs = interactionService.getLecturesForCourse(currentCourse);

        for (com.chirag.models.Lecture l : lecs) {
            Label lbl = new Label("- " + l.getTitle());
            lbl.setStyle("-fx-font-size: 14px;");
            lecturesList.getChildren().add(lbl);
        }
    }

    /**
     * Handles the buy/go-to-course button action.
     * If already enrolled → navigates to player.
     * If not enrolled → processes purchase.
     * Use-case: Course Purchase, Enrollment Verification.
     */
    @FXML
    public void handleBuy(ActionEvent event) {
        if (currentCourse == null)
            return;

        // Block course creator from enrolling in their own course
        if (currentCourse.getInstructor() != null &&
            UserSession.getCurrentUser().getId() == currentCourse.getInstructor().getId()) {
            showStyledAlert(Alert.AlertType.WARNING, "Self-Enrollment Blocked",
                    "You cannot enroll in a course you created. This course is yours — you can manage it from your Dashboard.");
            return;
        }

        // Check if already enrolled via service layer — navigate to player
        boolean isEnrolled = contentPlayerService.isUserEnrolled(
                UserSession.getCurrentUser(), currentCourse);
        if (isEnrolled) {
            Object ctrl = SceneManager.getInstance().switchScene("CoursePlayerView.fxml");
            if (ctrl instanceof CoursePlayerController) {
                ((CoursePlayerController) ctrl).setCourse(currentCourse);
            }
            return;
        }

        boolean success = paymentServiceImpl.processCoursePurchase(UserSession.getCurrentUser(), currentCourse);
        if (success) {
            // Invalidate cached dashboard data so it refreshes after purchase
            com.chirag.utils.DataCache dc = com.chirag.utils.DataCache.getInstance();
            int uid = UserSession.getCurrentUser().getId();
            dc.invalidate(com.chirag.utils.DataCache.enrolledCourses(uid));
            dc.invalidate(com.chirag.utils.DataCache.transactions(uid));
            dc.invalidate(com.chirag.utils.DataCache.lectureCounts(uid));

            statusMsgLabel.setText("Purchase Successful!");
            statusMsgLabel.setTextFill(javafx.scene.paint.Color.GREEN);

            showStyledAlert(Alert.AlertType.INFORMATION, "Enrollment Success",
                    "Enrollment Successful! Your course '" + currentCourse.getTitle()
                            + "' is now available in your Classroom.");

            Object ctrl = SceneManager.getInstance().switchScene("CoursePlayerView.fxml");
            if (ctrl instanceof CoursePlayerController) {
                ((CoursePlayerController) ctrl).setCourse(currentCourse);
            }
        } else {
            statusMsgLabel.setText("Error: Insufficient balance or failure!");
            statusMsgLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    /**
     * Loads the social proof and reviews.
     * Use-case: Social Proof.
     */
    private void loadReviews() {
        List<com.chirag.models.Review> reviews = interactionService.getReviewsForCourse(currentCourse.getId());

        if (reviews.isEmpty()) {
            ratingLabel.setText("* No reviews yet");
            Label noRev = new Label("Be the first to review after completing the course!");
            noRev.setStyle("-fx-text-fill: #8A8A8A; -fx-font-style: italic;");
            reviewsList.getChildren().add(noRev);
        } else {
            double sum = 0;
            for (com.chirag.models.Review r : reviews) {
                sum += r.getRating();

                VBox reviewCard = new VBox(5.0);
                reviewCard.setStyle("-fx-padding: 10; -fx-background-color: #F5F0EB; -fx-border-color: #E0D8CE; -fx-border-radius: 5;");

                String userName = r.getUser() != null ? r.getUser().getName() : "Anonymous";
                Label nameLabel = new Label(userName + " (* " + r.getRating() + "/5)");
                nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1B2A4A;");

                Label textLabel = new Label(r.getComment());
                textLabel.setWrapText(true);

                reviewCard.getChildren().addAll(nameLabel, textLabel);
                reviewsList.getChildren().add(reviewCard);
            }
            double avg = sum / reviews.size();
            ratingLabel.setText(String.format("* %.1f/5 (%d reviews)", avg, reviews.size()));
        }
    }

    /**
     * Spawns a CSS-styled alert dialog with consistent branding.
     */
    private void showStyledAlert(Alert.AlertType type, String title, String message) {
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
     * Opens a dialog for the user to report the course.
     * Use-case: Report Course.
     */
    @FXML
    public void handleReport(ActionEvent event) {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Report Course");
        dialog.setHeaderText("Reason for Reporting");
        dialog.setContentText("Please explain why you are reporting this course:");

        java.util.Optional<String> result = dialog.showAndWait();
        result.ifPresent(complaint -> {
            if (complaint.trim().isEmpty()) return;
            com.chirag.models.Report report = new com.chirag.models.Report();
            report.setReporter(UserSession.getCurrentUser());
            report.setReportedCourse(currentCourse);
            report.setComplaintText(complaint);
            
            boolean success = interactionService.submitReport(report);
            
            if (success) {
                showStyledAlert(Alert.AlertType.INFORMATION, "Report Submitted",
                        "Thank you for your report. Our team will investigate this course shortly.");
            }
        });
    }

    /**
     * Navigate back.
     * Use-case: View Navigation.
     */
    @FXML
    public void goBack(ActionEvent event) {
        SceneManager.getInstance().switchScene("MarketplaceView.fxml");
    }
}
