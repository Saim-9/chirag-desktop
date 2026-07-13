package com.chirag.controllers;

import com.chirag.models.Course;
import com.chirag.models.Review;
import com.chirag.services.CourseInteractionService;
import com.chirag.utils.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the review submission popup.
 * Delegates to CourseInteractionService instead of direct repo access.
 * Use-cases: Social Proof.
 */
public class ReviewPopupController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewPopupController.class);

    @FXML
    private ComboBox<String> ratingComboBox;

    @FXML
    private TextArea reviewTextArea;

    private Course course;
    private CourseInteractionService interactionService;

    public ReviewPopupController() {
        this.interactionService = new CourseInteractionService();
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    @FXML
    public void handleSubmit(ActionEvent event) {
        try {
            int rating = Integer.parseInt(ratingComboBox.getValue());
            String comment = reviewTextArea.getText();

            Review review = new Review();
            review.setUser(UserSession.getCurrentUser());
            review.setCourse(course);
            review.setRating(rating);
            review.setComment(comment != null ? comment.trim() : "");

            // Delegate to service layer instead of direct repo access
            interactionService.submitReview(review);
            closeStage();
        } catch (Exception e) {
            logger.error("Failed to submit review: {}", e.getMessage());
        }
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) ratingComboBox.getScene().getWindow();
        stage.close();
    }
}
