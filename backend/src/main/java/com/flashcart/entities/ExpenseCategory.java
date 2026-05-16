package com.flashcart.entities;

/**
 * Expense categories with associated emoji icons.
 * Surpasses PhonePe which lacks built-in categorization.
 */
public enum ExpenseCategory {
    FOOD("🍕", "Food & Drinks"),
    GROCERIES("🛒", "Groceries"),
    TRANSPORT("🚗", "Transport"),
    SHOPPING("🛍️", "Shopping"),
    ENTERTAINMENT("🎬", "Entertainment"),
    UTILITIES("💡", "Utilities"),
    RENT("🏠", "Rent"),
    MEDICAL("💊", "Medical"),
    TRAVEL("✈️", "Travel"),
    EDUCATION("📚", "Education"),
    SUBSCRIPTION("📱", "Subscriptions"),
    GIFTS("🎁", "Gifts"),
    SPORTS("⚽", "Sports"),
    PETS("🐾", "Pets"),
    OTHER("📦", "Other");

    private final String icon;
    private final String label;

    ExpenseCategory(String icon, String label) {
        this.icon = icon;
        this.label = label;
    }

    public String getIcon() { return icon; }
    public String getLabel() { return label; }
}
