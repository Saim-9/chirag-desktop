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

/**
 * Controller for the course upload from.
 * Handles dynamically adding lectures and submitting.
 * Use-cases: Course Creation.
 */
public class CourseCreationController {

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
    private boolean isSubmitting = false;

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
        linkFld.setPromptText("Video Link (Drive)");
        HBox.setHgrow(linkFld, Priority.ALWAYS);
        
        hbox.getChildren().addAll(titleFld, linkFld);
        lectureEntryContainer.getChildren().add(hbox);
    }

    /**
     * Persists the cors ad lectures into the database.
     * Use-case: Course Creation.
     */
    @FXML
    public void handlePublish(ActionEvent event) {
        if (isSubmitting) return;
        isSubmitting = true;

        String priceText = priceField.getText().trim();

        // Validates Price (Regex forces positive numbers and correct decimals)
        if (!priceText.matches("\\d+(\\.\\d{1,2})?")) {
            showModernAlert(Alert.AlertType.ERROR, "Invalid Price", "Price must be a valid positive number (e.g., 19.99). Do not include the $ sign or letters.");
            isSubmitting = false; // Reset the button lockout
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
                    String embedLink = null;

                    // Extract Google Drive file ID from various link formats
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

                    if (driveFileId != null && !driveFileId.isEmpty()) {
                        embedLink = "https://drive.google.com/file/d/" + driveFileId + "/preview";
                    }

                    // Validate Link Format
                    if (embedLink == null) {
                        showModernAlert(Alert.AlertType.ERROR, "Invalid Link", "Only Google Drive video links are supported. Please provide a valid Google Drive share link.\n\nAccepted formats:\n• https://drive.google.com/file/d/{ID}/view\n• https://drive.google.com/open?id={ID}");
                        isSubmitting = false; // CRITICAL FIX: Unlock the button so they can try again!
                        return; // Block submission
                    }

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
            isSubmitting = false; // Reset the button lockout
            return;
        }

        // Delegates to Service Layer
        boolean success = courseService.publishCourse(course, lectures);
        if (success) {
            System.out.println("Course Published Successfully");
            SceneManager.getInstance().switchScene("DashboardView.fxml");
        } else {
            showModernAlert(Alert.AlertType.ERROR, "Publish Failed", "An error occurred while saving the course to the database.");
            isSubmitting = false;
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
