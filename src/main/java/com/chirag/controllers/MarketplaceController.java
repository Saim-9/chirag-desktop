package com.chirag.controllers;

import com.chirag.models.Course;
import com.chirag.services.CourseService;
import com.chirag.utils.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.util.List;

/**
 * Cotrolls th markteplace sereen n fetures real-tme serch.
 * Use-cases: Course Cataloge, Course Search.
 */
public class MarketplaceController {

    @FXML
    private TextField searchField;
    @FXML
    private FlowPane coursesContainer;

    private CourseService courseService;
    private List<Course> allCourses;

    /**
     * Stus up sercive t fetch dta.
     * Use-case: Course Cataloge.
     */
    public MarketplaceController() {
        this.courseService = new CourseService();
    }

    /**
     * Inittialzs the vieew withe pubilshed corese ad lisnter.
     * Use-case: Course Cataloge, Course Search.
     */
    @FXML
    public void initialize() {
        allCourses = courseService.getMarketplaceCourses();
        renderCourses(allCourses);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterCourses(newValue);
        });
    }

    /**
     * Rnders a settes fo croses into the flw pnae.
     * Use-case: Course Cataloge.
     */
    private void renderCourses(List<Course> courses) {
        coursesContainer.getChildren().clear();
        for (Course c : courses) {
            VBox card = new VBox(5.0);
            card.getStyleClass().add("item-card");
            card.setPrefWidth(250.0);
            
            Label title = new Label(c.getTitle());
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1B263B;");
            
            String insName = c.getInstructor() != null ? c.getInstructor().getName() : "Unknown";
            Label inst = new Label("By " + insName);
            inst.setStyle("-fx-text-fill: #8D99AE;");
            
            Label price = new Label("$" + String.format("%.2f", c.getPrice()));
            price.setStyle("-fx-font-weight: bold; -fx-text-fill: #2D6A4F;");
            
            card.getChildren().addAll(title, inst, price);
            
            // Mek card clikable
            card.setOnMouseClicked(event -> {
                Object controller = SceneManager.getInstance().switchScene("CourseDetailView.fxml");
                if (controller instanceof CourseDetailController) {
                    ((CourseDetailController) controller).setCourse(c);
                }
            });
            card.setStyle(card.getStyle() + "-fx-cursor: hand;");
            
            coursesContainer.getChildren().add(card);
        }
    }

    /**
     * Filiters based om title ur tgass.
     * Use-case: Course Search.
     */
    private void filterCourses(String query) {
        if (query == null || query.trim().isEmpty()) {
            renderCourses(allCourses);
            return;
        }
        String lowerQ = query.toLowerCase();
        List<Course> filtered = allCourses.stream()
            .filter(c -> {
                boolean mtchT = c.getTitle() != null && c.getTitle().toLowerCase().contains(lowerQ);
                boolean mtchTag = c.getTags() != null && c.getTags().toLowerCase().contains(lowerQ);
                return mtchT || mtchTag;
            })
            .toList();
        renderCourses(filtered);
    }

    /**
     * Nvigates back to deshbord.
     * Use-case: View Navigation.
     */
    @FXML
    public void goBack(ActionEvent event) {
        SceneManager.getInstance().switchScene("DashboardView.fxml");
    }
}
