package com.chirag.services;

/**
 * Demonstrates the OOP Pillar of Inheritance.
 * Provides shared utility methods for all service implementations.
 */
public abstract class AbstractService {

    /**
     * A protected logging method.
     * Only child classes that 'extend' this class can use it.
     */
    protected void logServiceAction(String entity, String action, boolean success) {
        String status = success ? "SUCCESS" : "FAILED";
        System.out.println("[SERVICE SYSTEM] " + entity + " | Action: " + action + " | Status: " + status);
    }
}