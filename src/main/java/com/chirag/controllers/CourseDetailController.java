package com.chirag.controllers;

import com.chirag.models.Course;
import com.chirag.models.Lecture;
import com.chirag.repositories.LectureRepository;
import com.chirag.services.PaymentService;
import com.chirag.utils.SceneManager;
import com.chirag.utils.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.sql.SQLException;
import java.util.List;

/**
 * View details of a specific course and handles purchase.
 * Use-cases: Course View, Course Purchase.
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
    private PaymentService paymentService;
    private LectureRepository lectureRepository;
    private com.chirag.repositories.ReviewRepository reviewRepository;
    private com.chirag.repositories.ReportRepository reportRepository;

    /**
     * Sets up payment dependencies.
     * Use-case: Course Purchase.
     */
    public CourseDetailController() {
        this.paymentService = new PaymentService();
        this.lectureRepository = new LectureRepository();
        this.reviewRepository = new com.chirag.repositories.ReviewRepository();
        this.reportRepository = new com.chirag.repositories.ReportRepository();
    }

    /**
     * Injects the course data into the view after loading.
     * Use-case: Course View.
     */
    public void setCourse(Course course) {
        this.currentCourse = course;
        titleLabel.setText(course.getTitle());
        String ins = course.getInstructor() != null ? course.getInstructor().getName() : "Unknown";
        instructorLabel.setText("By " + ins);
        descriptionLabel.setText(course.getDescription());
        priceLabel.setText("$" + String.format("%.2f", course.getPrice()));
        buyButton.setText("Buy Course for $" + String.format("%.2f", course.getPrice()));

        if (course.getTags() != null && !course.getTags().isEmpty()) {
            for (String tag : course.getTags().split(",")) {
                Label tLbl = new Label(tag.trim());
                tLbl.getStyleClass().add("tag-chip");
                tagsFlowPane.getChildren().add(tLbl);
            }
        }

        loadLectures();
        loadReviews();
    }

    /**
     * Fetch lectures from repository and display them.
     * Use-case: Course View.
     */
    private void loadLectures() {
        lecturesList.getChildren().clear();


        java.util.List<com.chirag.models.Lecture> lecs = lectureRepository.findByCourse(currentCourse);

        for (com.chirag.models.Lecture l : lecs) {
            Label lbl = new Label("- " + l.getTitle());
            lbl.setStyle("-fx-font-size: 14px;");
            lecturesList.getChildren().add(lbl);
        }
    }

    /**
     * Executes purchase using PaymentService and shows alert.
     * Use-case: Course Purchase.
     */
    @FXML
    public void handleBuy(ActionEvent event) {
        if (currentCourse == null)
            return;

        boolean success = paymentService.processCoursePurchase(UserSession.getCurrentUser(), currentCourse);
        if (success) {
            statusMsgLabel.setText("Purchase Successful!");
            statusMsgLabel.setTextFill(javafx.scene.paint.Color.GREEN);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Enrollment Success");
            alert.setHeaderText(null);
            alert.setContentText("Enrollment Successful! Your course '" + currentCourse.getTitle()
                    + "' is now available in your Classroom.");
            java.net.URL cssUrl = getClass().getResource("/com/chirag/views/styles.css");
            if (cssUrl != null) {
                alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
            }
            alert.showAndWait();

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
        List<com.chirag.models.Review> reviews = reviewRepository.findByCourseId(currentCourse.getId());

        if (reviews.isEmpty()) {
            ratingLabel.setText("⭐ No reviews yet");
            Label noRev = new Label("Be the first to review after completing the course!");
            noRev.setStyle("-fx-text-fill: #8D99AE; -fx-font-style: italic;");
            reviewsList.getChildren().add(noRev);
        } else {
            double sum = 0;
            for (com.chirag.models.Review r : reviews) {
                sum += r.getRating();

                VBox reviewCard = new VBox(5.0);
                reviewCard.setStyle("-fx-padding: 10; -fx-background-color: #FAF8F5; -fx-border-color: #E0DCD3; -fx-border-radius: 5;");

                String userName = r.getUser() != null ? r.getUser().getName() : "Anonymous";
                Label nameLabel = new Label(userName + " (⭐ " + r.getRating() + "/5)");
                nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1B263B;");

                Label textLabel = new Label(r.getComment());
                textLabel.setWrapText(true);

                reviewCard.getChildren().addAll(nameLabel, textLabel);
                reviewsList.getChildren().add(reviewCard);
            }
            double avg = sum / reviews.size();
            ratingLabel.setText(String.format("⭐ %.1f/5 (%d reviews)", avg, reviews.size()));
        }
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
            try {
                com.chirag.models.Report report = new com.chirag.models.Report();
                report.setReporter(UserSession.getCurrentUser());
                report.setReportedCourse(currentCourse);
                report.setComplaintText(complaint);
                reportRepository.create(report);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Report Submitted");
                alert.setHeaderText(null);
                alert.setContentText("Thank you for your report. Our team will investigate this course shortly.");
                java.net.URL cssUrl = getClass().getResource("/com/chirag/views/styles.css");
                if (cssUrl != null) {
                    alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
                }
                alert.showAndWait();
            } catch (SQLException e) {
                System.err.println("Error saving report: " + e.getMessage());
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
