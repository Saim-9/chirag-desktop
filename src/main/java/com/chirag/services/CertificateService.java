package com.chirag.services;

import com.chirag.models.Course;
import com.chirag.models.Enrollment;
import com.chirag.models.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Service for generating and displaying course completion certificates.
 * Renders a styled JavaFX popup window that acts as a certificate view.
 * Use-cases: Course Completion, Social Proof.
 */
public class CertificateService extends AbstractService {

    /**
     * Generates and displays a styled certificate popup for a completed course.
     * Should be called when enrollment.isCompleted() is true.
     * Use-case: Course Completion.
     *
     * @param user       the student who completed the course
     * @param course     the completed course
     * @param enrollment the enrollment record (must have isCompleted = true)
     */
    public void showCertificate(User user, Course course, Enrollment enrollment) {
        if (!enrollment.isCompleted()) {
            logServiceAction("Certificate", "Show Certificate — course not completed", false);
            return;
        }

        Stage certStage = new Stage();
        certStage.initModality(Modality.APPLICATION_MODAL);
        certStage.setTitle("Certificate of Completion");
        certStage.setResizable(false);

        // --- Certificate Layout ---
        VBox certificate = new VBox(12);
        certificate.setAlignment(Pos.CENTER);
        certificate.setPadding(new Insets(40, 50, 40, 50));
        certificate.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #F5F0EB, #FFFFFF);" +
                "-fx-border-color: #1B2A4A;" +
                "-fx-border-width: 4;" +
                "-fx-border-insets: 10;" +
                "-fx-background-insets: 10;"
        );

        // Decorative top border
        Region topBorder = new Region();
        topBorder.setMinHeight(4);
        topBorder.setStyle("-fx-background-color: linear-gradient(to right, #D4A843, #1B2A4A);");

        // Title
        Label titleLbl = new Label("CERTIFICATE OF COMPLETION");
        titleLbl.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
        titleLbl.setTextFill(Color.web("#1B2A4A"));
        titleLbl.setTextAlignment(TextAlignment.CENTER);

        // Decorative divider
        Region divider1 = new Region();
        divider1.setMinHeight(2);
        divider1.setMaxWidth(200);
        divider1.setStyle("-fx-background-color: #D4A843;");

        // "This is to certify that"
        Label certifyLbl = new Label("This is to certify that");
        certifyLbl.setFont(Font.font("Segoe UI", 14));
        certifyLbl.setTextFill(Color.web("#8A8A8A"));

        // Student name
        String studentName = user.getName() != null ? user.getName() : "Student";
        Label nameLbl = new Label(studentName);
        nameLbl.setFont(Font.font("Georgia", FontWeight.BOLD, 28));
        nameLbl.setTextFill(Color.web("#1B2A4A"));
        nameLbl.setTextAlignment(TextAlignment.CENTER);

        // "has successfully completed"
        Label completedLbl = new Label("has successfully completed the course");
        completedLbl.setFont(Font.font("Segoe UI", 14));
        completedLbl.setTextFill(Color.web("#8A8A8A"));

        // Course title
        String courseTitle = course.getTitle() != null ? course.getTitle() : "Untitled Course";
        Label courseLbl = new Label("\"" + courseTitle + "\"");
        courseLbl.setFont(Font.font("Georgia", FontWeight.BOLD, 20));
        courseLbl.setTextFill(Color.web("#27714A"));
        courseLbl.setTextAlignment(TextAlignment.CENTER);
        courseLbl.setWrapText(true);

        // Instructor
        String instructorName = course.getInstructor() != null ? course.getInstructor().getName() : "Unknown";
        Label instructorLbl = new Label("Taught by: " + instructorName);
        instructorLbl.setFont(Font.font("Segoe UI", 13));
        instructorLbl.setTextFill(Color.web("#8A8A8A"));

        // Decorative divider
        Region divider2 = new Region();
        divider2.setMinHeight(2);
        divider2.setMaxWidth(200);
        divider2.setStyle("-fx-background-color: #D4A843;");

        // Date
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
        Label dateLbl = new Label("Date of Completion: " + dateStr);
        dateLbl.setFont(Font.font("Segoe UI", 12));
        dateLbl.setTextFill(Color.web("#8A8A8A"));

        // Platform branding
        Label brandLbl = new Label("— Chirag Learning Platform —");
        brandLbl.setFont(Font.font("Georgia", FontWeight.NORMAL, 11));
        brandLbl.setTextFill(Color.web("#B4B2A9"));

        // Decorative bottom border
        Region bottomBorder = new Region();
        bottomBorder.setMinHeight(4);
        bottomBorder.setStyle("-fx-background-color: linear-gradient(to right, #1B2A4A, #D4A843);");

        // Close button
        Button closeBtn = new Button("Close");
        closeBtn.setStyle(
                "-fx-background-color: #1B2A4A; -fx-text-fill: #F5F0EB; " +
                "-fx-padding: 8 30; -fx-font-weight: bold; -fx-cursor: hand; " +
                "-fx-background-radius: 4;"
        );
        closeBtn.setOnAction(e -> certStage.close());

        VBox.setMargin(closeBtn, new Insets(10, 0, 0, 0));

        certificate.getChildren().addAll(
                topBorder,
                titleLbl,
                divider1,
                certifyLbl,
                nameLbl,
                completedLbl,
                courseLbl,
                instructorLbl,
                divider2,
                dateLbl,
                brandLbl,
                bottomBorder,
                closeBtn
        );

        Scene scene = new Scene(certificate, 520, 500);

        // Apply global CSS
        java.net.URL cssUrl = getClass().getResource("/com/chirag/views/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        certStage.setScene(scene);
        certStage.showAndWait();

        logServiceAction("Certificate", "Show Certificate", true);
    }
}
