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



        // Crates a blanck pan just to shwo the wondow for noow
        Pane root = new Pane();
        Scene scene = new Scene(root, 1024, 768);

        // Sets the tittl of the stge
        primaryStage.setTitle("Chirag - Ignite Your Learning");
        primaryStage.setScene(scene);
        
        // Finaley displae the screne
        primaryStage.show();
    }

    /**
     * The sandard mian method that lonches javafx.
     * Use-case: System Initializatoin.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
