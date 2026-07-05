package com.chirag.controllers;

import com.chirag.models.Course;
import com.chirag.models.Enrollment;
import com.chirag.models.Lecture;
import com.chirag.services.ContentPlayerService;
import com.chirag.utils.SceneManager;
import com.chirag.utils.UserSession;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages the view for the course player classroom.
 * Uses native JavaFX MediaPlayer for Google Drive video playback.
 * Delegates all DB operations to ContentPlayerService (proper 3-tier).
 * Use-cases: Consume Content, Track Progress.
 */
public class CoursePlayerController {

    @FXML
    private Label courseTitleLabel;
    @FXML
    private MediaView mediaView;
    @FXML
    private StackPane videoContainer;
    @FXML
    private Label videoStatusLabel;
    @FXML
    private VBox lecturesList;
    @FXML
    private Label progressLabel;
    @FXML
    private Button playPauseBtn;
    @FXML
    private Label timeLabel;
    @FXML
    private Slider seekSlider;
    @FXML
    private Slider volumeSlider;

    private Course currentCourse;
    private Lecture currentLecture;
    private int totalLecturesCount;
    private ContentPlayerService contentPlayerService;
    private MediaPlayer currentMediaPlayer;
    private boolean isSeeking = false;

    /**
     * Sets up service dependency for content playback.
     * Use-case: Consume Content.
     */
    public CoursePlayerController() {
        this.contentPlayerService = new ContentPlayerService();
    }

    /**
     * Injects course data to start playing.
     * Use-case: Consume Content.
     */
    public void setCourse(Course course) {
        this.currentCourse = course;
        courseTitleLabel.setText(course.getTitle());
        initializeControls();
        loadCurriculumAsync();
    }

    /**
     * Wires up the volume slider and seek bar interactions.
     * Use-case: Consume Content.
     */
    private void initializeControls() {
        // Bind mediaView size to container
        mediaView.fitWidthProperty().bind(videoContainer.widthProperty());
        mediaView.fitHeightProperty().bind(videoContainer.heightProperty().subtract(10));

        // Volume slider listener
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (currentMediaPlayer != null) {
                currentMediaPlayer.setVolume(newVal.doubleValue() / 100.0);
            }
        });

        // Seek slider: user is dragging
        seekSlider.setOnMousePressed(e -> isSeeking = true);
        seekSlider.setOnMouseReleased(e -> {
            isSeeking = false;
            if (currentMediaPlayer != null) {
                currentMediaPlayer.seek(Duration.seconds(seekSlider.getValue()));
            }
        });
        seekSlider.valueChangingProperty().addListener((obs, wasChanging, isNowChanging) -> {
            isSeeking = isNowChanging;
        });
    }

    /**
     * Loads the curriculum on a background thread to prevent UI lag.
     * Use-case: Consume Content.
     */
    private void loadCurriculumAsync() {
        Task<List<Lecture>> fetchTask = new Task<>() {
            @Override
            protected List<Lecture> call() {
                return contentPlayerService.getLecturesForCourse(currentCourse);
            }
        };
        fetchTask.setOnSucceeded(e -> renderCurriculum(fetchTask.getValue()));
        fetchTask.setOnFailed(e -> System.err.println("Curriculum load failed: " + fetchTask.getException()));
        new Thread(fetchTask, "curriculum-loader").start();
    }

    /**
     * Renders the lecture list in the sidebar after async fetch.
     */
    private void renderCurriculum(List<Lecture> lecs) {
        lecturesList.getChildren().clear();
        totalLecturesCount = lecs.size();

        Enrollment enrollment = contentPlayerService.getEnrollment(
                UserSession.getCurrentUser(), currentCourse);
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
    }

    /**
     * Extracts the Google Drive file ID from a stored driveLink.
     * Handles both /preview format (stored) and standard share links.
     */
    private String extractFileId(String driveLink) {
        if (driveLink == null) return null;

        if (driveLink.contains("/file/d/")) {
            String part = driveLink.substring(driveLink.indexOf("/file/d/") + 8);
            if (part.contains("/")) part = part.substring(0, part.indexOf("/"));
            if (part.contains("?")) part = part.substring(0, part.indexOf("?"));
            return part;
        } else if (driveLink.contains("id=")) {
            String part = driveLink.substring(driveLink.indexOf("id=") + 3);
            if (part.contains("&")) part = part.substring(0, part.indexOf("&"));
            return part;
        }
        return null;
    }

    /**
     * Loads a Google Drive video using the native JavaFX MediaPlayer.
     * Converts the stored drive link to a direct download URL for streaming.
     * Use-case: Consume Content.
     */
    private void loadVideo(Lecture lecture) {
        this.currentLecture = lecture;

        // Stop any currently playing video
        disposeCurrentPlayer();

        String driveLink = lecture.getDriveLink();
        String fileId = extractFileId(driveLink);

        if (fileId == null || fileId.isEmpty()) {
            videoStatusLabel.setText("⚠ Invalid video link format");
            videoStatusLabel.setVisible(true);
            return;
        }

        // Use Google Drive direct download URL for streaming
        // confirm=t bypasses virus scan confirmation for large files
        String directUrl = "https://drive.google.com/uc?export=download&confirm=t&id=" + fileId;

        videoStatusLabel.setText("⏳ Loading video...");
        videoStatusLabel.setVisible(true);

        try {
            Media media = new Media(directUrl);
            currentMediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(currentMediaPlayer);

            // Set initial volume from slider
            currentMediaPlayer.setVolume(volumeSlider.getValue() / 100.0);

            // When media is ready, hide the loading overlay and configure seek bar
            currentMediaPlayer.setOnReady(() -> {
                videoStatusLabel.setVisible(false);
                Duration totalDuration = media.getDuration();
                seekSlider.setMax(totalDuration.toSeconds());
                updateTimeLabel(Duration.ZERO, totalDuration);
                playPauseBtn.setText("▶ Play");
            });

            // Update seek bar and time label as video plays
            currentMediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (!isSeeking) {
                    Platform.runLater(() -> {
                        seekSlider.setValue(newTime.toSeconds());
                        updateTimeLabel(newTime, media.getDuration());
                    });
                }
            });

            // Handle playback errors gracefully
            currentMediaPlayer.setOnError(() -> {
                String errMsg = "Unable to play video";
                if (currentMediaPlayer.getError() != null) {
                    errMsg = currentMediaPlayer.getError().getMessage();
                    System.err.println("MediaPlayer error: " + errMsg);
                }
                videoStatusLabel.setText("⚠ " + errMsg + "\n\nEnsure the Google Drive file is shared as 'Anyone with the link'.");
                videoStatusLabel.setVisible(true);
            });

            // Reset UI when video ends
            currentMediaPlayer.setOnEndOfMedia(() -> {
                Platform.runLater(() -> playPauseBtn.setText("↻ Replay"));
            });

        } catch (Exception e) {
            System.err.println("Failed to create media player: " + e.getMessage());
            videoStatusLabel.setText("⚠ Failed to load video player.\n" + e.getMessage());
            videoStatusLabel.setVisible(true);
        }
    }

    /**
     * Formats a duration pair into a readable "MM:SS / MM:SS" time string.
     */
    private void updateTimeLabel(Duration current, Duration total) {
        timeLabel.setText(formatDuration(current) + " / " + formatDuration(total));
    }

    /**
     * Converts a Duration to "MM:SS" format.
     */
    private String formatDuration(Duration duration) {
        if (duration == null || duration.isUnknown()) return "00:00";
        int totalSeconds = (int) Math.floor(duration.toSeconds());
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Toggles between play and pause states for the video.
     * Use-case: Consume Content.
     */
    @FXML
    public void handlePlayPause(ActionEvent event) {
        if (currentMediaPlayer == null) return;

        MediaPlayer.Status status = currentMediaPlayer.getStatus();
        if (status == MediaPlayer.Status.PLAYING) {
            currentMediaPlayer.pause();
            playPauseBtn.setText("▶ Play");
        } else if (status == MediaPlayer.Status.STOPPED || status == MediaPlayer.Status.UNKNOWN) {
            // Replay from start
            currentMediaPlayer.seek(Duration.ZERO);
            currentMediaPlayer.play();
            playPauseBtn.setText("⏸ Pause");
        } else {
            currentMediaPlayer.play();
            playPauseBtn.setText("⏸ Pause");
        }
    }

    /**
     * Handles the manual toggle for tracking progress.
     * Delegates persistence to ContentPlayerService.
     * Use-case: Track Progress.
     */
    @FXML
    public void handleMarkAsComplete(ActionEvent event) {
        Enrollment e = contentPlayerService.getEnrollment(
                UserSession.getCurrentUser(), currentCourse);
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
                    contentPlayerService.updateEnrollment(e);
                    progressLabel.setText("Course Completed!");
                    openReviewDialog();
                    // Show completion certificate
                    new com.chirag.services.CertificateService().showCertificate(
                            UserSession.getCurrentUser(), currentCourse, e);
                } else {
                    contentPlayerService.updateEnrollment(e);
                    progressLabel.setText("Progress Saved");
                }
                loadCurriculumAsync();
            } else {
                progressLabel.setText("Already Completed");
            }
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
     * Safely disposes the current MediaPlayer to free resources.
     * Prevents memory leaks and background audio bleed.
     */
    private void disposeCurrentPlayer() {
        if (currentMediaPlayer != null) {
            currentMediaPlayer.stop();
            currentMediaPlayer.dispose();
            currentMediaPlayer = null;
        }
    }

    /**
     * Stops the video playback to prevent background audio bleed.
     * Use-case: Audio Kill Switch
     */
    public void stopVideo() {
        disposeCurrentPlayer();
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
