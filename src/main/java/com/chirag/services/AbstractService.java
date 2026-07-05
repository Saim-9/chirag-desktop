package com.chirag.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates the OOP Pillar of Inheritance.
 * Provides shared utility methods for all service implementations.
 * Now uses SLF4J for structured logging instead of System.out.println.
 */
public abstract class AbstractService {

    /**
     * SLF4J logger instance — each subclass gets its own logger identity
     * via getClass(), enabling per-service log filtering.
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * A protected logging method using SLF4J.
     * Only child classes that 'extend' this class can use it.
     */
    protected void logServiceAction(String entity, String action, boolean success) {
        if (success) {
            logger.info("[SERVICE] {} | Action: {} | Status: SUCCESS", entity, action);
        } else {
            logger.warn("[SERVICE] {} | Action: {} | Status: FAILED", entity, action);
        }
    }
}