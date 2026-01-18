package org.td.model.enums;

/**
 * Types de centrales électriques disponibles
 * REBALANCED: Reduced costs by ~80% for better game economy
 */
public enum PowerPlantType {
    COAL(
            "Centrale à Charbon",
            "🏭",
            600, // Production (kWh) - Increased
            3000, // Coût (coins)
            15, // Maintenance
            8.0, // Pollution
            "Production stable et peu coûteuse, mais très polluante",
            1),

    SOLAR(
            "Centrale Solaire",
            "☀️",
            350, // Production (kWh) - Increased
            6000, // Coût (coins) - Optimized
            5, // Maintenance
            0.5, // Pollution
            "Énergie propre mais production variable selon l'ensoleillement",
            1),

    WIND(
            "Éolienne",
            "💨",
            250, // Production (kWh) - Increased
            3500, // Coût (coins)
            8, // Maintenance
            0.2, // Pollution
            "Énergie propre, production dépend du vent",
            2),

    NUCLEAR(
            "Centrale Nucléaire",
            "☢️",
            2500, // Production (kWh) - Massive increase for distinct tier
            25000, // Coût (coins)
            50, // Maintenance - Increased
            2.0, // Pollution
            "Production massive et stable, nécessite maintenance stricte",
            5),

    HYDRO(
            "Centrale Hydraulique",
            "🌊",
            900, // Production (kWh)
            12000, // Coût (coins)
            12, // Maintenance
            1.0, // Pollution
            "Production stable et propre, nécessite un cours d'eau",
            3),

    GEOTHERMAL(
            "Centrale Géothermique",
            "🌋",
            750, // Production (kWh)
            10000, // Coût (coins)
            10, // Maintenance
            0.8, // Pollution
            "Énergie constante et propre, nécessite zone géothermique",
            4);

    private final String displayName;
    private final String icon;
    private final double baseProduction;
    private final double baseCost;
    private final double maintenanceCost;
    private final double pollutionLevel;
    private final String description;
    private final int minimumCityLevel;

    /**
     * Constructeur
     */
    PowerPlantType(String displayName, String icon,
            double baseProduction, double baseCost,
            double maintenanceCost, double pollutionLevel,
            String description, int minimumCityLevel) {
        this.displayName = displayName;
        this.icon = icon;
        this.baseProduction = baseProduction;
        this.baseCost = baseCost;
        this.maintenanceCost = maintenanceCost;
        this.pollutionLevel = pollutionLevel;
        this.description = description;
        this.minimumCityLevel = minimumCityLevel;
    }

    /**
     * Vérifie si ce type est débloqué pour un niveau de ville donné
     */
    public boolean isUnlockedAt(int cityLevel) {
        return cityLevel >= minimumCityLevel;
    }

    /**
     * Retourne le coût de construction pour un niveau donné
     */
    public double getConstructionCost(int level) {
        return baseCost * level;
    }

    /**
     * Retourne la production pour un niveau donné
     */
    public double getProduction(int level) {
        return baseProduction * level;
    }

    /**
     * Calcule le coût d'upgrade au niveau suivant
     */
    public double getUpgradeCost(int currentLevel) {
        // L'upgrade coûte 75% du coût de construction du niveau suivant
        return baseCost * (currentLevel + 1) * 0.75;
    }

    /**
     * Retourne une catégorie environnementale
     */
    public String getEnvironmentalCategory() {
        if (pollutionLevel < 1.0)
            return "Très propre";
        if (pollutionLevel < 3.0)
            return "Propre";
        if (pollutionLevel < 5.0)
            return "Modéré";
        if (pollutionLevel < 7.0)
            return "Polluant";
        return "Très polluant";
    }

    /**
     * Retourne une couleur représentative (pour UI)
     */
    public String getColor() {
        return switch (this) {
            case COAL -> "#78716c"; // Gris foncé
            case SOLAR -> "#fbbf24"; // Jaune
            case WIND -> "#38bdf8"; // Bleu ciel
            case NUCLEAR -> "#22c55e"; // Vert
            case HYDRO -> "#0ea5e9"; // Bleu
            case GEOTHERMAL -> "#f97316"; // Orange
        };
    }

    // Getters
    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public double getBaseProduction() {
        return baseProduction;
    }

    public double getBaseCost() {
        return baseCost;
    }

    public double getMaintenanceCost() {
        return maintenanceCost;
    }

    public double getPollutionLevel() {
        return pollutionLevel;
    }

    public String getDescription() {
        return description;
    }

    public int getMinimumCityLevel() {
        return minimumCityLevel;
    }

    @Override
    public String toString() {
        return displayName;
    }
}