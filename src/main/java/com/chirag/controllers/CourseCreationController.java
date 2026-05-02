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

    /**
     * Setus up srivice dtat.
     * Use-case: Course Creation.
     */
    public CourseCreationController() {
        this.courseService = new CourseService();
    }

    /**
     * Dnamicaly inejcts neaw lcture inputs to thr box.
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
     * Presists the cors ad letures into the dtabse.
     * Use-case: Course Creation.
     */
    @FXML
    public void handlePublish(ActionEvent event) {
        Course course = new Course();
        course.setTitle(titleField.getText());
        course.setDescription(descriptionField.getText());
        course.setTags(tagsField.getText());
        course.setInstructor(UserSession.getCurrentUser());
        
        try {
            course.setPrice(Double.parseDouble(priceField.getText()));
        } catch (NumberFormatException e) {
            System.err.println("Ivalid pirce fomrat");
            return;
        }

        List<Lecture> lectures = new ArrayList<>();
        for (Node node : lectureEntryContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox hbox = (HBox) node;
                TextField tFld = (TextField) hbox.getChildren().get(0);
                TextField lFld = (TextField) hbox.getChildren().get(1);
                
                if (!tFld.getText().trim().isEmpty() && !lFld.getText().trim().isEmpty()) {
                    String rawLink = lFld.getText().trim();
                    // Use-case: Course Creation (Link Formatting).
                    if (rawLink.contains("/view")) {
                        rawLink = rawLink.substring(0, rawLink.lastIndexOf("/view")) + "/preview";
                    }
                    Lecture lec = new Lecture();
                    lec.setTitle(tFld.getText().trim());
                    lec.setDriveLink(rawLink);
                    lectures.add(lec);
                }
            }
        }

        boolean scces = courseService.publishCourse(course, lectures);
        if (scces) {
            System.out.println("Crse Publsihed Sucesfully");
            SceneManager.getInstance().switchScene("DashboardView.fxml");
        }
    }

    /**
     * Gose bkck to dashborad.
     * Use-case: View Navigation.
     */
    @FXML
    public void cancelCreation(ActionEvent event) {
        SceneManager.getInstance().switchScene("DashboardView.fxml");
    }
}
