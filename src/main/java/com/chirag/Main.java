package com.chirag;

import com.chirag.utils.DatabaseConfig;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * The min entery pont for the entier aplication.
 * Extnds java fx aplicaton to lod the UI.
 * Use-cases: System Initializatoin, Appication Startup.
 */
public class Main extends Application {

    /**
     * Strats the main window of the programe.
     * Use-case: Appication Startup.
     */
    @Override
    public void start(Stage primaryStage) {
        // Initilize the dtabase first thin before the viws are loaded.
        // Clling getInstance trigeres the private initializeDatabase() inside.
        DatabaseConfig.getInstance();
        // Sets the tittl of the stge
        primaryStage.setTitle("Chirag - Ignite Your Learning");
        primaryStage.setWidth(900);
        primaryStage.setHeight(600);

        // Initt the scen mnanager and lod the loing wiew
        com.chirag.utils.SceneManager.getInstance().init(primaryStage);
        
        // Fired the rutnig to login
        com.chirag.utils.SceneManager.getInstance().switchScene("LoginView.fxml");
    }

    /**
     * The sandard mian method that lonches javafx.
     * Use-case: System Initializatoin.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
