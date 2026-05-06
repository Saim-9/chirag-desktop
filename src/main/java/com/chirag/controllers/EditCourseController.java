package com.chirag.controllers;

import com.chirag.models.Course;
import com.chirag.services.CourseService;
import com.chirag.utils.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Controller for editing course metadata.
 * Creators can update Title, Description, Price, and Tags.
 * Use-case: Edit Course.
 */
public class EditCourseController {

    @FXML
    private TextField titleField;
    @FXML
    private TextField priceField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TextField tagsField;

    private CourseService courseService;
    private Course currentCourse;

    /**
     * Initializes the service for course updates.
     * Use-case: Edit Course.
     */
    public EditCourseController() {
        this.courseService = new CourseService();
    }

    /**
     * Pre-populates the fields with the current course data.
     * Use-case: Edit Course.
     */
    public void setCourse(Course course) {
        this.currentCourse = course;
        if (course != null) {
            titleField.setText(course.getTitle());
            priceField.setText(String.valueOf(course.getPrice()));
            descriptionField.setText(course.getDescription());
            tagsField.setText(course.getTags());
        }
    }

    /**
     * Validates and saves the changes to the database.
     * Use-case: Edit Course.
     */
    @FXML
    public void handleSave(ActionEvent event) {
        if (currentCourse == null) return;

        // Validation logic for Price field (Use Case 9, Step 2)
        String priceText = priceField.getText();
        double price = -1;
        try {
            price = Double.parseDouble(priceText);
            if (price < 0) {
                showError("Invalid Price", "Price cannot be negative.");
                highlightField(priceField);
                return;
            }
        } catch (NumberFormatException e) {
            showError("Invalid Price Format", "Please enter a valid numeric value for the price.");
            highlightField(priceField);
            return;
        }

        // Update course object metadata
        currentCourse.setTitle(titleField.getText());
        currentCourse.setDescription(descriptionField.getText());
        currentCourse.setPrice(price);
        currentCourse.setTags(tagsField.getText());

        // Overwrite in database (Use Case 9, Step 3)
        boolean success = courseService.updateCourse(currentCourse);
        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Course Updated Successfully");
            java.net.URL cssUrl = getClass().getResource("/com/chirag/views/styles.css");
            if (cssUrl != null) {
                alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
            }
            alert.showAndWait();
            
            // Navigate back to Dashboard (Use Case 9, Step 4)
            SceneManager.getInstance().switchScene("DashboardView.fxml");
        } else {
            showError("Update Failed", "There was an error updating the course in the database.");
        }
    }

    /**
     * Cancels the edit and returns to the dashboard.
     * Use-case: View Navigation.
     */
    @FXML
    public void handleCancel(ActionEvent event) {
        SceneManager.getInstance().switchScene("DashboardView.fxml");
    }

    /**
     * Helper to show error alerts.
     */
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        java.net.URL cssUrl = getClass().getResource("/com/chirag/views/styles.css");
        if (cssUrl != null) {
            alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
        }
        alert.showAndWait();
    }

    /**
     * Helper to highlight an invalid field.
     */
    private void highlightField(TextField field) {
        field.setStyle("-fx-border-color: #E63946; -fx-border-width: 2px;");
    }
}
