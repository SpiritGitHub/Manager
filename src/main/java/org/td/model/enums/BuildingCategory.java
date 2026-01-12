package org.td.model.enums;

public enum BuildingCategory {
    COMMERCIAL("Commercial"),
    ENTERTAINMENT("Divertissement"),
    PARK("Espace Vert"),
    PUBLIC_SERVICE("Service Public"),
    SECURITY("Sécurité");

    private final String displayName;

    BuildingCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

