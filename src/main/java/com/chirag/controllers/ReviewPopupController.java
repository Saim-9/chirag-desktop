package com.chirag.controllers;

import com.chirag.models.Course;
import com.chirag.models.Review;
import com.chirag.repositories.ReviewRepository;
import com.chirag.utils.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

/**
 * Controller for the review submission popup.
 * Use-cases: Social Proof.
 */
public class ReviewPopupController {

    @FXML
    private ComboBox<String> ratingComboBox;

    @FXML
    private TextArea reviewTextArea;

    private Course course;
    private ReviewRepository reviewRepository;

    public ReviewPopupController() {
        this.reviewRepository = new ReviewRepository();
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

            reviewRepository.create(review);
            closeStage();
        } catch (Exception e) {
            System.err.println("Failed to submit review: " + e.getMessage());
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
