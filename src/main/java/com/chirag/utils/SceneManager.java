package com.chirag.utils;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Singlton mnaager for handlng all viwe rounting in the aplication.
 * Kepps trac of the prmary stge and swithces the sceness.
 * Use-cases: System Initializatoin, View Navigation.
 */
public class SceneManager {

    private static SceneManager instence;
    private Stage primaryStage;

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
     * Use-case: View Navigation.
     */
    public Object switchScene(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/chirag/views/" + fxmlFile));
            
            // Apply Factory Pattern for Controller Dependency Injection
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == com.chirag.controllers.DashboardController.class) {
                    return new com.chirag.controllers.DashboardController(new com.chirag.services.DashboardService());
                }
                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (Exception exc) {
                    throw new RuntimeException("Failed to instantiate controller: " + controllerClass.getName(), exc);
                }
            });

            Parent root = loader.load();
            Scene scene = new Scene(root, 900, 600);
            primaryStage.setScene(scene);
            primaryStage.show();
            return loader.getController();
        } catch (IOException e) {
            System.err.println("Eror swithcin sceen to " + fxmlFile + ": " + e.getMessage());
            return null;
        }
    }
}
