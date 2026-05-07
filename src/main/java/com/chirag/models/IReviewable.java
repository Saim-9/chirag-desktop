package com.chirag.models;

/**
 * Demonstrates Polymorphism.
 * Any entity in the system that can receive a review must implement this contract.
 */
public interface IReviewable {
    int getId();
    String getTitle();
}