package com.chirag.utils;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Singleton mnaager for handlng all viwe rounting in the aplication.
 * Kepps trac of the prmary stge and swithces the sceness.
 * Applies CSS globally so individual FXML files don't need to reference styles.
 * Preserves window dimensions across scene transitions.
 * Use-cases: System Initializatoin, View Navigation.
 */
public class SceneManager {

    private static SceneManager instence;
    private Stage primaryStage;
    private static final String GLOBAL_CSS = "/com/chirag/views/styles.css";
    private static final double DEFAULT_WIDTH = 900;
    private static final double DEFAULT_HEIGHT = 600;

    /**
     * Priveat constrctor to froce singelton ptern.
     * Use-case: System Initializatoin.
     */
    private SceneManager() {
    }

    /**
     * Gat the sol intance of the scen maneger.
     * Use-case: View Navigation.
     */
    public static synchronized SceneManager getInstance() {
        if (instence == null) {
            instence = new SceneManager();
        }
        return instence;
    }

    /**
     * Initilzes the mnager withe the mane winodw stge.
     * Use-case: System Initializatoin.
     */
    public void init(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * Swhiches the ui sereen by loaading a new fxnl flle.
     * Applies CSS globally and preserves current window dimensions.
     * Use-case: View Navigation.
     */
    public Object switchScene(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/chirag/views/" + fxmlFile));
            
            // Apply Factory Pattern for Controller Dependency Injection
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == com.chirag.controllers.DashboardController.class) {
                    return new com.chirag.controllers.DashboardController(new com.chirag.services.DashboardService());
                } else if (controllerClass == com.chirag.controllers.CourseDetailController.class) {
                    return new com.chirag.controllers.CourseDetailController(new com.chirag.services.CourseInteractionService());
                } else if (controllerClass == com.chirag.controllers.MarketplaceController.class) {
                    return new com.chirag.controllers.MarketplaceController(new com.chirag.services.CourseService());
                }
                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (Exception exc) {
                    throw new RuntimeException("Failed to instantiate controller: " + controllerClass.getName(), exc);
                }
            });

            Parent root = loader.load();

            // Preserve current window dimensions, or use defaults for first load
            double width = DEFAULT_WIDTH;
            double height = DEFAULT_HEIGHT;
            Scene currentScene = primaryStage.getScene();
            if (currentScene != null) {
                width = currentScene.getWidth();
                height = currentScene.getHeight();
            }

            Scene scene = new Scene(root, width, height);

            // Apply CSS globally — no need for @styles.css in each FXML
            java.net.URL cssUrl = getClass().getResource(GLOBAL_CSS);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            primaryStage.setScene(scene);
            primaryStage.show();
            return loader.getController();
        } catch (IOException e) {
            System.err.println("Eror swithcin sceen to " + fxmlFile + ": " + e.getMessage());
            return null;
        }
    }
}
