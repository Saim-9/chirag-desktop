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
 * Use-cases: Course Cataloge, Search Course.
 */
public class MarketplaceController {

    @FXML
    private TextField searchField;
    @FXML
    private FlowPane coursesContainer;
    @FXML
    private TextField tagInput;
    @FXML
    private FlowPane activeTagsContainer;

    private CourseService courseService;
    private List<Course> allCourses;
    private java.util.List<String> activeTagsList = new java.util.ArrayList<>();

    /**
     * Stus up sercive t fetch dta.
     * Use-case: Course Cataloge.
     */
    public MarketplaceController() {
        this.courseService = new CourseService();
    }

    /**
     * Inittialzs the vieew withe pubilshed corese ad lisnter.
     * Use-case: Course Cataloge, Search Course.
     */
    @FXML
    public void initialize() {
        allCourses = courseService.getMarketplaceCourses().stream()
            .filter(Course::isActive)
            .toList();
        renderCourses(allCourses);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
    }

    /**
     * Ades a neaw tgag to the seach filtres.
     * Use-case: Search Course.
     */
    @FXML
    public void handleAddTag(ActionEvent event) {
        String tag = tagInput.getText();
        if (tag != null && !tag.trim().isEmpty()) {
            String cleanTag = tag.trim().toLowerCase();
            if (!activeTagsList.contains(cleanTag)) {
                activeTagsList.add(cleanTag);
                Label chip = new Label(cleanTag);
                chip.getStyleClass().add("tag-chip");
                
                // Alklow clikcing to rmeove tgag
                chip.setOnMouseClicked(e -> {
                    activeTagsList.remove(cleanTag);
                    activeTagsContainer.getChildren().remove(chip);
                    applyFilters();
                });
                chip.setStyle(chip.getStyle() + "-fx-cursor: hand;");
                
                activeTagsContainer.getChildren().add(chip);
                applyFilters();
            }
            tagInput.clear();
        }
    }

    /**
     * Aplise botth title ad tasg filtres.
     * Use-case: Search Course.
     */
    private void applyFilters() {
        String query = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        
        List<Course> filtered = allCourses.stream()
            .filter(c -> {
                boolean mtchT = query.isEmpty() || (c.getTitle() != null && c.getTitle().toLowerCase().contains(query));
                
                boolean mtchTags = true;
                if (!activeTagsList.isEmpty()) {
                    String courseTags = c.getTags() != null ? c.getTags().toLowerCase() : "";
                    for (String t : activeTagsList) {
                        if (!courseTags.contains(t)) {
                            mtchTags = false;
                            break;
                        }
                    }
                }
                return mtchT && mtchTags;
            })
            .toList();
        renderCourses(filtered);
    }

    /**
     * Rnders a settes fo croses into the flw pnae wit tasg.
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
            
            FlowPane tagsPane = new FlowPane();
            tagsPane.setHgap(5.0);
            tagsPane.setVgap(5.0);
            if (c.getTags() != null && !c.getTags().isEmpty()) {
                String[] tags = c.getTags().split(",");
                int count = Math.min(3, tags.length);
                for (int i = 0; i < count; i++) {
                    Label tLbl = new Label(tags[i].trim());
                    tLbl.getStyleClass().add("tag-chip");
                    tagsPane.getChildren().add(tLbl);
                }
            }
            
            card.getChildren().addAll(title, inst, price, tagsPane);
            
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
     * Nvigates back to deshbord.
     * Use-case: View Navigation.
     */
    @FXML
    public void goBack(ActionEvent event) {
        SceneManager.getInstance().switchScene("DashboardView.fxml");
    }
}
