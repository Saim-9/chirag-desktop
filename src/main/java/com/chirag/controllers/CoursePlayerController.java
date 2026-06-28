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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            // Parse CSV into a Set for exact ID matching (fixes substring bug)
            Set<String> completedSet = new HashSet<>();
            if (completedIds != null && !completedIds.trim().isEmpty()) {
                completedSet.addAll(Arrays.asList(completedIds.split(",")));
            }

            for (Lecture l : lecs) {
                String title = l.getTitle();
                if (completedSet.contains(String.valueOf(l.getId()))) {
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
     * Injects the Google Drive preview iframe into the webview.
     * Shows only the video player interface, no browser chrome.
     * Use-case: Consume Content.
     */
    private void loadVideo(Lecture lecture) {
        this.currentLecture = lecture;
        String driveLink = lecture.getDriveLink();

        // Ensure the link is in embed/preview format
        if (driveLink != null && driveLink.contains("drive.google.com/file/d/") && !driveLink.contains("/preview")) {
            // Convert view link to preview embed
            String fileId = driveLink.substring(driveLink.indexOf("/file/d/") + 8);
            if (fileId.contains("/")) fileId = fileId.substring(0, fileId.indexOf("/"));
            driveLink = "https://drive.google.com/file/d/" + fileId + "/preview";
        }

        String html = "<html><head><style>"
                + "* { margin: 0; padding: 0; overflow: hidden; }"
                + "body { background-color: #0B0F19; width: 100%; height: 100%; }"
                + "iframe { width: 100%; height: 100%; border: none; }"
                + "</style></head><body>"
                + "<iframe src='" + driveLink + "' allow='autoplay; encrypted-media' allowfullscreen></iframe>"
                + "</body></html>";
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

                // Parse CSV into Set for exact matching (fixes substring bug)
                Set<String> completedSet = new HashSet<>();
                if (completedIds != null && !completedIds.trim().isEmpty()) {
                    completedSet.addAll(Arrays.asList(completedIds.split(",")));
                }

                if (!completedSet.contains(currentIdStr)) {
                    completedSet.add(currentIdStr);
                    String updatedIds = String.join(",", completedSet);
                    e.setCompletedLectureIds(updatedIds);

                    if (completedSet.size() >= totalLecturesCount) {
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
     * Opens the Google Drive video in an external browser as a fallback.
     * Use-case: Video Safety Valve.
     */
    @FXML
    public void launchExternalPlayer(ActionEvent event) {
        if (currentLecture != null) {
            try {
                String url = currentLecture.getDriveLink();
                // Convert preview link to view link for external browser
                if (url != null && url.contains("/preview")) {
                    url = url.replace("/preview", "/view");
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
