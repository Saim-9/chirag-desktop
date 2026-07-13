package com.chirag.controllers;

import com.chirag.models.Course;
import com.chirag.services.ContentPlayerService;
import com.chirag.services.CourseService;
import com.chirag.utils.SceneManager;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cotrolls th markteplace sereen n fetures real-tme serch.
 * Use-cases: Course Cataloge, Search Course.
 */
public class MarketplaceController {

    private static final Logger logger = LoggerFactory.getLogger(MarketplaceController.class);

    @FXML
    private TextField searchField;
    @FXML
    private FlowPane coursesContainer;
    @FXML
    private TextField tagInput;
    @FXML
    private FlowPane activeTagsContainer;

    private CourseService courseService;
    private ContentPlayerService contentPlayerService;
    private List<Course> allCourses;
    private java.util.List<String> activeTagsList = new java.util.ArrayList<>();

    /**
     * Default constructor for fallback.
     */
    public MarketplaceController() {
    }

    /**
     * Injected constructor.
     * Use-case: Course Catalog.
     */
    public MarketplaceController(CourseService courseService) {
        this.courseService = courseService;
        this.contentPlayerService = new ContentPlayerService();
    }

    /**
     * Inittialzs the view withe published courese ad lisnter.
     * Loads courses asynchronously to prevent UI freeze.
     * Use-case: Course Catalogue, Search Course.
     */
    @FXML
    public void initialize() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });

        // Load courses on a background thread
        loadCoursesAsync();
    }

    /**
     * Fetches marketplace courses on a background thread.
     */
    private void loadCoursesAsync() {
        Task<List<Course>> task = new Task<>() {
            @Override
            protected List<Course> call() {
                return courseService.getMarketplaceCourses();
            }
        };
        task.setOnSucceeded(e -> {
            allCourses = task.getValue();
            applyFilters();
        });
        task.setOnFailed(e -> logger.error("Marketplace load failed: {}", task.getException().getMessage()));
        new Thread(task, "marketplace-loader").start();
    }

    /**
     * Adds a new tag to the search filters.
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
                
                // Allow clicking to remove tag
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
     * Applies both title ad task filters.
     * Use-case: Search Course.
     */
    private void applyFilters() {
        if (allCourses == null) return;
        String query = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        
        List<Course> filtered = allCourses.stream()
            .filter(c -> {
                // Search by title, description, AND instructor name
                boolean mtchT = query.isEmpty();
                if (!mtchT) {
                    boolean titleMatch = c.getTitle() != null && c.getTitle().toLowerCase().contains(query);
                    boolean descMatch = c.getDescription() != null && c.getDescription().toLowerCase().contains(query);
                    String insName = c.getInstructor() != null ? c.getInstructor().getName() : "";
                    boolean instrMatch = insName.toLowerCase().contains(query);
                    mtchT = titleMatch || descMatch || instrMatch;
                }
                
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
     * Renders a set of courses into the flow pane with tags.
     * Use-case: Course Catalogue.
     */
    private void renderCourses(List<Course> courses) {
        coursesContainer.getChildren().clear();
        for (Course c : courses) {
            VBox card = new VBox(5.0);
            card.getStyleClass().add("item-card");
            card.setPrefWidth(250.0);
            
            Label title = new Label(c.getTitle());
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1B2A4A;");
            
            String insName = c.getInstructor() != null ? c.getInstructor().getName() : "Unknown";
            Label inst = new Label("By " + insName);
            inst.setStyle("-fx-text-fill: #8A8A8A;");

            // Show average rating on marketplace card
            double avgRating = contentPlayerService.getAverageRating(c.getId());
            int reviewCount = contentPlayerService.getReviewCount(c.getId());
            Label ratingLbl;
            if (reviewCount > 0) {
                String stars = "*".repeat(Math.max(1, (int) Math.round(avgRating)));
                ratingLbl = new Label(stars + String.format(" %.1f (%d)", avgRating, reviewCount));
                ratingLbl.setStyle("-fx-text-fill: #D4A843; -fx-font-size: 12px;");
            } else {
                ratingLbl = new Label("No ratings yet");
                ratingLbl.setStyle("-fx-text-fill: #8A8A8A; -fx-font-size: 11px; -fx-font-style: italic;");
            }
            
            Label price = new Label("$" + String.format("%.2f", c.getPrice()));
            price.setStyle("-fx-font-weight: bold; -fx-text-fill: #27714A;");
            
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
            
            card.getChildren().addAll(title, inst, ratingLbl, price, tagsPane);
            
            // Make card clickable
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
     * Refreshes the marketplace by fetching fresh data from the cloud.
     * Keeps your current search filters active while updating the list!
     * Use-case: Live Updates.
     */
    @FXML
    public void handleRefresh(ActionEvent event) {
        logger.info("Fetching fresh courses from database...");
        // Re-fetch asynchronously
        loadCoursesAsync();
    }

    /**
     * Navigates back to dashboard.
     * Use-case: View Navigation.
     */
    @FXML
    public void goBack(ActionEvent event) {
        SceneManager.getInstance().switchScene("DashboardView.fxml");
    }
}
