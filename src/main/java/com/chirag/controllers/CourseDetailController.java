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

    private Course currentCourse;
    private PaymentService paymentService;
    private LectureRepository lectureRepository;

    /**
     * Sets up payment dependencies.
     * Use-case: Course Purchase.
     */
    public CourseDetailController() {
        this.paymentService = new PaymentService();
        this.lectureRepository = new LectureRepository();
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
    }

    /**
     * Fetch lectures from repository and display them.
     * Use-case: Course View.
     */
    private void loadLectures() {
        try {
            List<Lecture> lecs = lectureRepository.getDao().queryBuilder().where().eq("course_id", currentCourse.getId()).query();
            for (Lecture l : lecs) {
                Label lbl = new Label("- " + l.getTitle());
                lbl.setStyle("-fx-font-size: 14px;");
                lecturesList.getChildren().add(lbl);
            }
        } catch (SQLException e) {
            System.err.println("Error loading lectures: " + e.getMessage());
        }
    }

    /**
     * Executes purchase using PaymentService and shows alert.
     * Use-case: Course Purchase.
     */
    @FXML
    public void handleBuy(ActionEvent event) {
        if (currentCourse == null) return;

        boolean success = paymentService.processCoursePurchase(UserSession.getCurrentUser(), currentCourse);
        if (success) {
            statusMsgLabel.setText("Purchase Successful!");
            statusMsgLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Enrollment Success");
            alert.setHeaderText(null);
            alert.setContentText("Enrollment Successful! Your course '" + currentCourse.getTitle() + "' is now available in your Classroom.");
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
     * Navigate back.
     * Use-case: View Navigation.
     */
    @FXML
    public void goBack(ActionEvent event) {
        SceneManager.getInstance().switchScene("MarketplaceView.fxml");
    }
}
