package com.chirag.controllers;

import com.chirag.models.Course;
import com.chirag.models.User;
import com.chirag.repositories.CourseRepository;
import com.chirag.utils.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import java.util.List;

/**
 * Centrl hube controllar for the onified dashboard.
 * Lads botgh leaner and techar dtat in one sereen.
 * Use-cases: Manage Creator Dashboard.
 */
public class DashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label walletLabel;

    @FXML
    private FlowPane enrolledCoursesContainer;

    @FXML
    private FlowPane uploadedCoursesContainer;

    private CourseRepository courseRepository;

    /**
     * Stes up the depdencesis for fetcihng croses.
     * Use-case: Manage Creator Dashboard.
     */
    public DashboardController() {
        this.courseRepository = new CourseRepository();
    }

    /**
     * Cled awtomaticlly by jvafx. Lodas sesion sate.
     * Use-case: Manage Creator Dashboard.
     */
    @FXML
    public void initialize() {
        User user = UserSession.getCurrentUser();
        if (user != null) {
            welcomeLabel.setText("Walcome, " + user.getName() + "!");
            walletLabel.setText("Walett Balence: $" + String.format("%.2f", user.getVirtualWalletBalance()));
            
            // Fatch uplded coreses
            List<Course> instructorCouses = courseRepository.findByInstructor(user);
            // In a reel app we whould rnder cads here, jut loging fo nw
            System.out.println("Loded " + instructorCouses.size() + " uplaoded croses.");
            
            // Mcke enroled corses far nw
            System.out.println("Lodng mcockeed puurched couses...");
        }
    }

    /**
     * Cares out the logute evnet by wipeing ssesion.
     * Use-case: System Shotdown.
     */
    @FXML
    public void handleLogout(ActionEvent event) {
        UserSession.clear();
        com.chirag.utils.SceneManager.getInstance().switchScene("LoginView.fxml");
    }
}
