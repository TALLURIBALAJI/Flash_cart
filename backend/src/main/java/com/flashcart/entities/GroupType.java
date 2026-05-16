package com.flashcart.entities;

/**
 * Group type categorization for organizing expense groups.
 */
public enum GroupType {
    TRIP("✈️", "Trip"),
    HOME("🏠", "Home"),
    COUPLE("💑", "Couple"),
    FRIENDS("👯", "Friends"),
    WORK("💼", "Work"),
    OTHER("📁", "Other");

    private final String icon;
    private final String label;

    GroupType(String icon, String label) {
        this.icon = icon;
        this.label = label;
    }

    public String getIcon() { return icon; }
    public String getLabel() { return label; }
}
