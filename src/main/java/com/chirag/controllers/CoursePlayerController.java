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
    private Lecture currentLecture;
    private int totalLecturesCount;
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
     * Gets the current enrollment for the user and course.
     */
    private Enrollment getEnrollment() throws SQLException {
        List<Enrollment> enrs = enrollmentRepository.getDao().queryBuilder().where()
                .eq("user_id", UserSession.getCurrentUser().getId())
                .and()
                .eq("course_id", currentCourse.getId())
                .query();
        return enrs.isEmpty() ? null : enrs.get(0);
    }

    /**
     * Loads the list of lectures into the sidebar.
     * Use-case: Consume Content.
     */
    private void loadCurriculum() {
        try {
            lecturesList.getChildren().clear();
            List<Lecture> lecs = lectureRepository.getDao().queryBuilder().where()
                    .eq("course_id", currentCourse.getId()).query();
            totalLecturesCount = lecs.size();

            Enrollment enrollment = getEnrollment();
            String completedIds = enrollment != null ? enrollment.getCompletedLectureIds() : "";

            for (Lecture l : lecs) {
                String title = l.getTitle();
                if (completedIds.contains(String.valueOf(l.getId()))) {
                    title = "✅ " + title;
                }
                Button btn = new Button(title);
                btn.getStyleClass().add("nav-button");
                btn.setStyle("-fx-text-fill: #1B263B; -fx-padding: 5 0;");
                btn.setOnAction(e -> loadVideo(l));
                lecturesList.getChildren().add(btn);
            }
            if (!lecs.isEmpty()) {
                if (currentLecture == null) {
                    loadVideo(lecs.get(0));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch lectures: " + e.getMessage());
        }
    }

    /**
     * Injects the iframe URL into the webview.
     * Use-case: Consume Content.
     */
    private void loadVideo(Lecture lecture) {
        this.currentLecture = lecture;
        String driveLink = lecture.getDriveLink();
        String html = "<html><body style='margin:0;padding:0;background-color:#0B0F19;'><iframe width='100%' height='100%' src='"
                + driveLink + "?autoplay=1' frameborder='0' allowfullscreen></iframe></body></html>";
        videoEngine.getEngine().loadContent(html);
    }

    /**
     * Handles the manual toggle for tracking progress.
     * Use-case: Track Progress.
     */
    @FXML
    public void handleMarkAsComplete(ActionEvent event) {
        try {
            Enrollment e = getEnrollment();
            if (e != null && currentLecture != null) {
                String completedIds = e.getCompletedLectureIds();
                String currentIdStr = String.valueOf(currentLecture.getId());

                if (!completedIds.contains(currentIdStr)) {
                    if (completedIds.isEmpty()) {
                        completedIds = currentIdStr;
                    } else {
                        completedIds += "," + currentIdStr;
                    }
                    e.setCompletedLectureIds(completedIds);

                    int completedCount = completedIds.split(",").length;
                    if (completedCount >= totalLecturesCount) {
                        e.setCompleted(true);
                        enrollmentRepository.update(e);
                        progressLabel.setText("Course Completed!");
                        openReviewDialog();
                    } else {
                        enrollmentRepository.update(e);
                        progressLabel.setText("Progress Saved");
                    }
                    loadCurriculum();
                } else {
                    progressLabel.setText("Already Completed");
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error marking complete: " + ex.getMessage());
        }
    }

    private void openReviewDialog() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/chirag/views/ReviewPopupView.fxml"));
            javafx.scene.Parent root = loader.load();

            ReviewPopupController popupController = loader.getController();
            popupController.setCourse(currentCourse);

            javafx.stage.Stage popupStage = new javafx.stage.Stage();
            popupStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            popupStage.setTitle("Rate Course");
            popupStage.setScene(new javafx.scene.Scene(root));
            popupStage.showAndWait();
        } catch (Exception e) {
            System.err.println("Failed to open review popup: " + e.getMessage());
        }
    }

    /**
     * Launches the video in an external browser if WebView fails.
     * Use-case: Video Safety Valve.
     */
    @FXML
    public void launchExternalPlayer(ActionEvent event) {
        if (currentLecture != null) {
            try {
                String url = currentLecture.getDriveLink();
                // Converts embed link back to standard YouTube link for the external browser
                if (url != null && url.contains("embed/")) {
                    url = url.replace("embed/", "watch?v=");
                }
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                videoEngine.getEngine().loadContent("<h2 style='color:#E0DCD3; text-align:center; margin-top:20%; font-family:sans-serif;'>Video playing in your external browser...</h2>");
            } catch (Exception e) {
                System.err.println("Failed to launch browser: " + e.getMessage());
            }
        }
    }

    /**
     * Stops the video playback to prevent background audio bleed.
     * Use-case: Audio Kill Switch
     */
    public void stopVideo() {
        videoEngine.getEngine().load(null);
    }

    /**
     * Goes back to the dashboard.
     * Use-case: View Navigation.
     */
    @FXML
    public void goBack(ActionEvent event) {
        stopVideo();
        SceneManager.getInstance().switchScene("DashboardView.fxml");
    }
}
