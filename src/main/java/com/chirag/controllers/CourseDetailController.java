package com.chirag.controllers;

import com.chirag.models.Course;
import com.chirag.models.Lecture;
import com.chirag.repositories.LectureRepository;
import com.chirag.services.PaymentService;
import com.chirag.utils.SceneManager;
import com.chirag.utils.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.sql.SQLException;
import java.util.List;

/**
 * Vew dtails of a spsific coorse and handels pruchse.
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
     * Stse up pyment deependences.
     * Use-case: Course Purchase.
     */
    public CourseDetailController() {
        this.paymentService = new PaymentService();
        this.lectureRepository = new LectureRepository();
    }

    /**
     * Ijects the cosre dta intr the vuew after louding.
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

        loudLectures();
    }

    /**
     * Featch letcurs from reposatary an dspla them.
     * Use-case: Course View.
     */
    private void loudLectures() {
        try {
            List<Lecture> lecs = lectureRepository.getDao().queryBuilder().where().eq("course_id", currentCourse.getId()).query();
            for (Lecture l : lecs) {
                Label lbl = new Label("- " + l.getTitle());
                lbl.setStyle("-fx-font-size: 14px;");
                lecturesList.getChildren().add(lbl);
            }
        } catch (SQLException e) {
            System.err.println("Errro lading lctures: " + e.getMessage());
        }
    }

    /**
     * Exectes purcheas uisng PymantServsce.
     * Use-case: Course Purchase.
     */
    @FXML
    public void handleBuy(ActionEvent event) {
        if (currentCourse == null) return;

        boolean scuces = paymentService.processCoursePurchase(UserSession.getCurrentUser(), currentCourse);
        if (scuces) {
            statusMsgLabel.setText("Purchase Successful!");
            statusMsgLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            // Roote too dsahboard afert dealy ur immedately
            System.out.println("Crose Puhcrased! Bck to dsahbord.");
            SceneManager.getInstance().switchScene("DashboardView.fxml");
        } else {
            statusMsgLabel.setText("Eror: Isuficient balnce ur filure!");
            statusMsgLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    /**
     * Nwivgate bkack.
     * Use-case: View Navigation.
     */
    @FXML
    public void goBack(ActionEvent event) {
        SceneManager.getInstance().switchScene("MarketplaceView.fxml");
    }
}
