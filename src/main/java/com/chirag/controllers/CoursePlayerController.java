package com.chirag.controllers;

import com.chirag.models.Course;
import com.chirag.models.Enrollment;
import com.chirag.models.Lecture;
import com.chirag.repositories.EnrollmentRepository;
import com.chirag.repositories.LectureRepository;
import com.chirag.utils.SceneManager;
import com.chirag.utils.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import java.sql.SQLException;
import java.util.List;

/**
 * Manages the view for the course player classroom.
 * Also handles progress tracking manually.
 * Use-cases: Consume Content, Track Progress.
 */
public class CoursePlayerController {

    @FXML
    private Label courseTitleLabel;
    @FXML
    private WebView videoEngine;
    @FXML
    private VBox lecturesList;
    @FXML
    private Label progressLabel;

    private Course currentCourse;
    private LectureRepository lectureRepository;
    private EnrollmentRepository enrollmentRepository;

    /**
     * Sets up dependencies for lecture and enrollment data.
     * Use-case: Consume Content.
     */
    public CoursePlayerController() {
        this.lectureRepository = new LectureRepository();
        this.enrollmentRepository = new EnrollmentRepository();
    }

    /**
     * Injects course data to start playing.
     * Use-case: Consume Content.
     */
    public void setCourse(Course course) {
        this.currentCourse = course;
        courseTitleLabel.setText(course.getTitle());
        loadCurriculum();
    }

    /**
     * Loads the list of lectures into the sidebar.
     * Use-case: Consume Content.
     */
    private void loadCurriculum() {
        try {
            List<Lecture> lecs = lectureRepository.getDao().queryBuilder().where().eq("course_id", currentCourse.getId()).query();
            for (Lecture l : lecs) {
                Button btn = new Button(l.getTitle());
                btn.getStyleClass().add("nav-button");
                btn.setStyle("-fx-text-fill: #1B263B; -fx-padding: 5 0;");
                btn.setOnAction(e -> loadVideo(l.getDriveLink()));
                lecturesList.getChildren().add(btn);
            }
            if (!lecs.isEmpty()) {
                loadVideo(lecs.get(0).getDriveLink()); // Load first video automatically
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch lectures: " + e.getMessage());
        }
    }

    /**
     * Injects the iframe URL into the webview.
     * Use-case: Consume Content.
     */
    private void loadVideo(String driveLink) {
        videoEngine.getEngine().load(driveLink);
    }

    /**
     * Handles the manual toggle for tracking progress.
     * Use-case: Track Progress.
     */
    @FXML
    public void handleMarkAsComplete(ActionEvent event) {
        try {
            List<Enrollment> enrs = enrollmentRepository.getDao().queryBuilder().where()
                .eq("user_id", UserSession.getCurrentUser().getId())
                .and()
                .eq("course_id", currentCourse.getId())
                .query();
            
            if (!enrs.isEmpty()) {
                Enrollment e = enrs.get(0);
                e.setCompleted(true);
                enrollmentRepository.update(e);
                progressLabel.setText("Progress Saved");
            }
        } catch (SQLException e) {
            System.err.println("Error marking complete: " + e.getMessage());
        }
    }

    /**
     * Goes back to the dashboard.
     * Use-case: View Navigation.
     */
    @FXML
    public void goBack(ActionEvent event) {
        SceneManager.getInstance().switchScene("DashboardView.fxml");
    }
}
