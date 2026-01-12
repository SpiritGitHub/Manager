package org.td.model.enums;

/**
 * Santé financière de la ville
 */
public enum FinancialHealth {
    CRITICAL("Critique", "#ef4444"),
    POOR("Mauvaise", "#f97316"),
    MODERATE("Modérée", "#f59e0b"),
    GOOD("Bonne", "#10b981"),
    EXCELLENT("Excellente", "#22c55e");

    private final String displayName;
    private final String color;

    FinancialHealth(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }
}