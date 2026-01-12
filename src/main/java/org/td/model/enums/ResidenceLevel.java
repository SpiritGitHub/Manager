package org.td.model.enums;

/**
 * Niveaux de résidences disponibles
 * Chaque niveau a des caractéristiques énergétiques et économiques différentes
 */
public enum ResidenceLevel {
    BASIC(
            1,                      // Niveau
            "Résidence Basique",    // Nom d'affichage
            50,                     // Demande énergétique minimale (kWh)
            100,                    // Demande énergétique maximale (kWh)
            80,                     // Revenu minimal par heure (€)
            150,                    // Revenu maximal par heure (€)
            2000,                   // Coût de construction (€)
            "🏠",                   // Icône
            "Petite maison familiale avec équipements de base"
    ),

    MEDIUM(
            2,                      // Niveau
            "Résidence Moderne",    // Nom d'affichage
            100,                    // Demande énergétique minimale (kWh)
            200,                    // Demande énergétique maximale (kWh)
            200,                    // Revenu minimal par heure (€)
            350,                    // Revenu maximal par heure (€)
            5000,                   // Coût de construction (€)
            "🏘️",                  // Icône
            "Immeuble moderne avec équipements confortables"
    ),

    ADVANCED(
            3,                      // Niveau
            "Résidence Luxueuse",   // Nom d'affichage
            200,                    // Demande énergétique minimale (kWh)
            400,                    // Demande énergétique maximale (kWh)
            500,                    // Revenu minimal par heure (€)
            800,                    // Revenu maximal par heure (€)
            12000,                  // Coût de construction (€)
            "🏢",                   // Icône
            "Tour résidentielle de luxe avec tous les équipements haut de gamme"
    );

    private final int level;
    private final String displayName;
    private final double minEnergyDemand;
    private final double maxEnergyDemand;
    private final double minRevenue;
    private final double maxRevenue;
    private final double constructionCost;
    private final String icon;
    private final String description;

    /**
     * Constructeur
     */
    ResidenceLevel(int level, String displayName,
                   double minEnergyDemand, double maxEnergyDemand,
                   double minRevenue, double maxRevenue,
                   double constructionCost, String icon, String description) {
        this.level = level;
        this.displayName = displayName;
        this.minEnergyDemand = minEnergyDemand;
        this.maxEnergyDemand = maxEnergyDemand;
        this.minRevenue = minRevenue;
        this.maxRevenue = maxRevenue;
        this.constructionCost = constructionCost;
        this.icon = icon;
        this.description = description;
    }

    /**
     * Retourne le niveau suivant (pour upgrade)
     */
    public ResidenceLevel getNext() {
        return switch(this) {
            case BASIC -> MEDIUM;
            case MEDIUM -> ADVANCED;
            case ADVANCED -> null; // Niveau max atteint
        };
    }

    /**
     * Vérifie s'il existe un niveau supérieur
     */
    public boolean hasNext() {
        return this != ADVANCED;
    }

    /**
     * Retourne le coût d'upgrade vers le niveau suivant
     */
    public double getUpgradeCost() {
        ResidenceLevel next = getNext();
        return next != null ? next.constructionCost * 0.7 : 0;
    }

    // Getters
    public int getLevel() {
        return level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getMinEnergyDemand() {
        return minEnergyDemand;
    }

    public double getMaxEnergyDemand() {
        return maxEnergyDemand;
    }

    public double getMinRevenue() {
        return minRevenue;
    }

    public double getMaxRevenue() {
        return maxRevenue;
    }

    public double getConstructionCost() {
        return constructionCost;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}