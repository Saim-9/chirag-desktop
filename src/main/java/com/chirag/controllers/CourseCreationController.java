package com.chirag.controllers;

import com.chirag.models.Course;
import com.chirag.models.Lecture;
import com.chirag.services.CourseService;
import com.chirag.utils.SceneManager;
import com.chirag.utils.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Alert;
import java.net.HttpURLConnection;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the course upload from.
 * Handles dynamically adding lectures and submitting.
 * Use-cases: Course Creation.
 */
public class CourseCreationController {

    private static final Logger logger = LoggerFactory.getLogger(CourseCreationController.class);

    @FXML
    private TextField titleField;
    @FXML
    private TextField priceField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TextField tagsField;
    @FXML
    private VBox lectureEntryContainer;

    private CourseService courseService;
    private final java.util.concurrent.atomic.AtomicBoolean isSubmitting = new java.util.concurrent.atomic.AtomicBoolean(false);

    /**
     * Sets up service data.
     * Use-case: Course Creation.
     */
    public CourseCreationController() {
        this.courseService = new CourseService();
    }

    /**
     * Dynamically injects new lecture inputs to the box.
     * Use-case: Course Creation.
     */
    @FXML
    public void addLectureField(ActionEvent event) {
        HBox hbox = new HBox(10.0);
        TextField titleFld = new TextField();
        titleFld.setPromptText("Lecture Title");
        HBox.setHgrow(titleFld, Priority.ALWAYS);
        
        TextField linkFld = new TextField();
        linkFld.setPromptText("Google Drive Link");
        HBox.setHgrow(linkFld, Priority.ALWAYS);
        
        hbox.getChildren().addAll(titleFld, linkFld);
        lectureEntryContainer.getChildren().add(hbox);
    }

    /**
     * Extracts a Google Drive file ID from various link formats.
     * Returns null if the link is not a valid Google Drive URL.
     */
    private String extractDriveFileId(String rawLink) {
        if (rawLink == null || rawLink.trim().isEmpty()) return null;

        String driveFileId = null;

        if (rawLink.contains("drive.google.com/file/d/")) {
            // Format: https://drive.google.com/file/d/{FILE_ID}/view?...
            int start = rawLink.indexOf("/file/d/") + 8;
            int end = rawLink.indexOf("/", start);
            if (end == -1) end = rawLink.length();
            driveFileId = rawLink.substring(start, end);
        } else if (rawLink.contains("drive.google.com/open?id=")) {
            // Format: https://drive.google.com/open?id={FILE_ID}
            int start = rawLink.indexOf("id=") + 3;
            int end = rawLink.indexOf("&", start);
            if (end == -1) end = rawLink.length();
            driveFileId = rawLink.substring(start, end);
        }

        return (driveFileId != null && !driveFileId.isEmpty()) ? driveFileId : null;
    }

    /**
     * Verifies that a Google Drive file is publicly accessible
     * by sending an HTTP HEAD request to the direct download URL.
     * Returns true if the file is reachable (HTTP 200 or 302 redirect).
     * Returns false if the file is private (403), not found (404), or unreachable.
     */
    private boolean verifyDriveLinkAccessibility(String fileId) {
        try {
            String checkUrl = "https://drive.google.com/uc?export=download&id=" + fileId;
            HttpURLConnection connection = (HttpURLConnection) new URL(checkUrl).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setInstanceFollowRedirects(true);
            int responseCode = connection.getResponseCode();
            connection.disconnect();

            // Google Drive returns 200 or 302 for accessible files,
            // 403 for private files, 404 for non-existent files
            return responseCode == 200 || responseCode == 302 || responseCode == 303;
        } catch (Exception e) {
            logger.error("Failed to verify Drive link: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Persists the cors ad lectures into the database.
     * Use-case: Course Creation.
     */
    @FXML
    public void handlePublish(ActionEvent event) {
        // AtomicBoolean.compareAndSet is thread-safe: returns true only if current value is false
        if (!isSubmitting.compareAndSet(false, true)) return;

        String priceText = priceField.getText().trim();

        // Validates Price (Regex forces positive numbers and correct decimals)
        if (!priceText.matches("\\d+(\\.\\d{1,2})?")) {
            showModernAlert(Alert.AlertType.ERROR, "Invalid Price", "Price must be a valid positive number (e.g., 19.99). Do not include the $ sign or letters.");
            isSubmitting.set(false); // Reset the button lockout
            return; // Stop the upload process
        }

        Course course = new Course();
        course.setTitle(titleField.getText());
        course.setDescription(descriptionField.getText());
        course.setTags(tagsField.getText());
        course.setInstructor(UserSession.getCurrentUser());
        course.setPrice(Double.parseDouble(priceText));

        List<Lecture> lectures = new ArrayList<>();
        for (Node node : lectureEntryContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox hbox = (HBox) node;
                TextField tFld = (TextField) hbox.getChildren().get(0);
                TextField lFld = (TextField) hbox.getChildren().get(1);

                if (!tFld.getText().trim().isEmpty() && !lFld.getText().trim().isEmpty()) {
                    String rawLink = lFld.getText().trim();

                    // Step 1: Extract the Google Drive file ID from the link
                    String driveFileId = extractDriveFileId(rawLink);

                    // Validate Link Format
                    if (driveFileId == null) {
                        showModernAlert(Alert.AlertType.ERROR, "Invalid Link",
                                "Only Google Drive video links are supported.\n\n" +
                                "Lecture: \"" + tFld.getText().trim() + "\"\n\n" +
                                "Accepted formats:\n" +
                                "• https://drive.google.com/file/d/{ID}/view\n" +
                                "• https://drive.google.com/open?id={ID}");
                        isSubmitting.set(false);
                        return;
                    }

                    // Step 2: Verify the link is publicly accessible via HTTP check
                    if (!verifyDriveLinkAccessibility(driveFileId)) {
                        showModernAlert(Alert.AlertType.ERROR, "Link Not Accessible",
                                "The Google Drive video for lecture \"" + tFld.getText().trim() + "\" is not publicly accessible.\n\n" +
                                "Please ensure the file is shared as 'Anyone with the link' in Google Drive:\n\n" +
                                "1. Open the file in Google Drive\n" +
                                "2. Right-click → Share\n" +
                                "3. Change 'Restricted' to 'Anyone with the link'\n" +
                                "4. Click 'Done' and try again");
                        isSubmitting.set(false);
                        return;
                    }

                    // Step 3: Convert to embed format for storage
                    String embedLink = "https://drive.google.com/file/d/" + driveFileId + "/preview";

                    Lecture lec = new Lecture();
                    lec.setTitle(tFld.getText().trim());
                    lec.setDriveLink(embedLink);
                    lectures.add(lec);
                }
            }
        }

        // Validates Empty Course
        if (lectures.isEmpty()) {
            showModernAlert(Alert.AlertType.WARNING, "Empty Course", "You must add at least one valid lecture before publishing this course.");
            isSubmitting.set(false); // Reset the button lockout
            return;
        }

        // Delegates to Service Layer
        boolean success = courseService.publishCourse(course, lectures);
        if (success) {
            // Invalidate caches so new course appears immediately
            com.chirag.utils.DataCache dc = com.chirag.utils.DataCache.getInstance();
            dc.invalidate(com.chirag.utils.DataCache.MARKETPLACE_COURSES);
            dc.invalidate(com.chirag.utils.DataCache.MARKETPLACE_RATINGS);
            dc.invalidate(com.chirag.utils.DataCache.instructorCourses(
                    UserSession.getCurrentUser().getId()));

            logger.info("Course published successfully: {}", course.getTitle());
            SceneManager.getInstance().switchScene("DashboardView.fxml");
        } else {
            showModernAlert(Alert.AlertType.ERROR, "Publish Failed", "An error occurred while saving the course to the database.");
            isSubmitting.set(false);
        }
    }


    /**
     * Spawns a modern, CSS-styled alert dialog.
     */
    private void showModernAlert(Alert.AlertType type, String title, String message) {
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
     * Goes back to dashboard.
     * Use-case: View Navigation.
     */
    @FXML
    public void cancelCreation(ActionEvent event) {
        SceneManager.getInstance().switchScene("DashboardView.fxml");
    }
}
