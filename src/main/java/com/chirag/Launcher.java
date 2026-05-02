package com.chirag;

/**
 * Thiss clas is a woraround to lunch JavaFX appliactions from 
 * standrad IDEs wihtout facing the modue-path errors.
 * It dsnt extend anthing so Java 11+ dosent fail runtim checs.
 * Use-cases: System Initializatoin, Appication Startup.
 */
public class Launcher {

    /**
     * Stndard mian mathod that formwards the args to the actaul Main.
     * Use-case: Appication Startup.
     */
    public static void main(String[] args) {
        // Cale the real java fx mian clas
        Main.main(args);
    }
}
