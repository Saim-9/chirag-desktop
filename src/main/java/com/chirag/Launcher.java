package com.chirag;

/**
 * This class is a workaround to launch JavaFX applications from
 * standard IDEs without facing the module-path errors.
  * Use-cases: System Initialization, Application Startup.
 */
public class Launcher
{


    /**
     * Standard main method that forwards the args to the actual Main.
     * Use-case: Application Startup.
     */
    public static void main(String[] args) {
        // Call the real java fx main class
        Main.main(args);
    }
}
