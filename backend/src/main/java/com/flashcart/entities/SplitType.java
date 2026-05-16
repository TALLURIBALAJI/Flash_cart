package com.flashcart.entities;

/**
 * Split types for expense distribution.
 * Mirrors PhonePe's equal/custom splits, plus advanced modes.
 */
public enum SplitType {
    EQUAL,       // Split equally among all participants
    EXACT,       // Each person owes a specific exact amount
    PERCENTAGE,  // Each person owes a percentage of the total
    RATIO        // Split by ratio (e.g., 2:1:1)
}