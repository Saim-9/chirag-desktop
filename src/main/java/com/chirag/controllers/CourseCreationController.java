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

/**
 * Cntroller foar the coruse uploadd from.
 * Handeles danamically adding lactures and submiiting.
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
        // Regex: Must be digits, optionally followed by a decimal and 1 or 2 digits
        if (!priceText.matches("\\d+(\\.\\d{1,2})?")) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Invalid Price");
            alert.setHeaderText(null);
            alert.setContentText("Price must be a valid number (e.g., 19.99). Do not include the $ sign or letters.");
            java.net.URL cssUrl = getClass().getResource("/com/chirag/views/styles.css");
            if (cssUrl != null) {
                alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
            }
            alert.showAndWait();
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
                    // Use-case: Course Creation (Link Formatting).
                    String embedLink = null;
                    if (rawLink.contains("youtube.com/watch?v=")) {
                        String id = rawLink.substring(rawLink.indexOf("v=") + 2);
                        if (id.contains("&")) id = id.substring(0, id.indexOf("&"));
                        embedLink = "https://www.youtube.com/embed/" + id;
                    } else if (rawLink.contains("youtu.be/")) {
                        String id = rawLink.substring(rawLink.indexOf("youtu.be/") + 9);
                        if (id.contains("?")) id = id.substring(0, id.indexOf("?"));
                        embedLink = "https://www.youtube.com/embed/" + id;
                    }
                    
                    if (embedLink == null) {
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                        alert.setTitle("Invalid Link");
                        alert.setHeaderText(null);
                        alert.setContentText("Only YouTube links are supported. Please provide a valid YouTube link.");
                        java.net.URL cssUrl = getClass().getResource("/com/chirag/views/styles.css");
                        if (cssUrl != null) {
                            alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
                        }
                        alert.showAndWait();
                        return; // Block submission
                    }

                    Lecture lec = new Lecture();
                    lec.setTitle(tFld.getText().trim());
                    lec.setDriveLink(embedLink);
                    lectures.add(lec);
                }
            }
        }

        boolean scces = courseService.publishCourse(course, lectures);
        if (scces) {
            System.out.println("Course Published Successfully");
            SceneManager.getInstance().switchScene("DashboardView.fxml");
        } else {
            isSubmitting = false;
        }
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
