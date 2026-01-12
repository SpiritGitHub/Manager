package org.td.model.enums;

/**
 * Types de centrales électriques disponibles
 * REBALANCED: Reduced costs by ~80% for better game economy
 */
public enum PowerPlantType {
    COAL(
            "Centrale à Charbon",
            "🏭",
            500, // Production de base (kWh)
            2500, // Coût de construction (coins) - reduced from 5000
            10, // Maintenance par heure (coins) - reduced from 30
            8.0, // Niveau de pollution (0-10)
            "Production stable et peu coûteuse, mais très polluante",
            1// Niveau ville minimum requis
    ),

    SOLAR(
            "Centrale Solaire",
            "☀️",
            300, // Production de base (kWh)
            7500, // Coût de construction (coins) - reduced from 15000
            5, // Maintenance par heure (coins) - reduced from 10
            0.5, // Niveau de pollution (0-10)
            "Énergie propre mais production variable selon l'ensoleillement",
            1// Niveau ville minimum requis
    ),

    WIND(
            "Éolienne",
            "💨",
            200, // Production de base (kWh)
            4000, // Coût de construction (coins) - reduced from 8000
            5, // Maintenance par heure (coins) - reduced from 20
            0.2, // Niveau de pollution (0-10)
            "Énergie propre, production dépend du vent",
            2// Niveau ville minimum requis
    ),

    NUCLEAR(
            "Centrale Nucléaire",
            "☢️",
            2000, // Production de base (kWh)
            25000, // Coût de construction (coins) - reduced from 50000
            20, // Maintenance par heure (coins) - reduced from 150
            2.0, // Niveau de pollution (0-10)
            "Production massive et stable, nécessite maintenance stricte",
            5// Niveau ville minimum requis
    ),

    HYDRO(
            "Centrale Hydraulique",
            "🌊",
            800, // Production de base (kWh)
            12500, // Coût de construction (coins) - reduced from 25000
            8, // Maintenance par heure (coins) - reduced from 40
            1.0, // Niveau de pollution (0-10)
            "Production stable et propre, nécessite un cours d'eau",
            3// Niveau ville minimum requis
    ),

    GEOTHERMAL(
            "Centrale Géothermique",
            "🌋",
            600, // Production de base (kWh)
            10000, // Coût de construction (coins) - reduced from 20000
            10, // Maintenance par heure (coins) - reduced from 50
            0.8, // Niveau de pollution (0-10)
            "Énergie constante et propre, nécessite zone géothermique",
            4// Niveau ville minimum requis
    );

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