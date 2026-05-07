package com.chirag.services;

import com.chirag.models.Course;
import com.chirag.models.User;

/**
 * Demonstrates Polymorphism and Dependency Inversion.
 * Contract for handling financial transactions and course purchases.
 */
public interface IPaymentService {

    /**
     * Processes the full payment event logic.
     */
    boolean processCoursePurchase(User buyer, Course course);

}