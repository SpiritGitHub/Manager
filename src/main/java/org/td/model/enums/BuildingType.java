package org.td.model.enums;

import java.util.Random;

/**
 * Types d'infrastructures et de bâtiments publics
 * REBALANCED: Reduced costs by ~60% for better game economy
 */
public enum BuildingType {
    // Commerces et services
    COMMERCIAL(
            "Centre Commercial",
            "🏪",
            3.0, // Bonus bonheur
            80, // Consommation énergie (kWh)
            8, // Coût maintenance (coins/h) - reduced from 20
            1500, // Coût construction (coins) - reduced from 3000
            1, // Niveau ville minimum
            BuildingCategory.COMMERCIAL,
            "Attire les acheteurs et génère des revenus"),

    // Divertissement
    ENTERTAINMENT(
            "Salle de Spectacle",
            "🎭",
            8.0, // Bonus bonheur
            150, // Consommation énergie (kWh)
            15, // Coût maintenance (coins/h) - reduced from 50
            4000, // Coût construction (coins) - reduced from 8000
            2, // Niveau ville minimum
            BuildingCategory.ENTERTAINMENT,
            "Concerts, théâtre et événements culturels"),

    STADIUM(
            "Stade",
            "🏟️",
            15.0, // Bonus bonheur
            300, // Consommation énergie (kWh)
            30, // Coût maintenance (coins/h) - reduced from 150
            12500, // Coût construction (coins) - reduced from 25000
            5, // Niveau ville minimum
            BuildingCategory.ENTERTAINMENT,
            "Grands événements sportifs, boost massif du bonheur"),

    MUSEUM(
            "Musée",
            "🏛️",
            6.0, // Bonus bonheur
            80, // Consommation énergie (kWh)
            12, // Coût maintenance (coins/h) - reduced from 40
            2500, // Coût construction (coins) - reduced from 5000
            3, // Niveau ville minimum
            BuildingCategory.ENTERTAINMENT,
            "Culture et histoire, attire les touristes"),

    // Espaces verts
    PARK(
            "Parc Public",
            "🌳",
            5.0, // Bonus bonheur
            20, // Consommation énergie (kWh)
            5, // Coût maintenance (coins/h) - reduced from 10
            1000, // Coût construction (coins) - reduced from 2000
            1, // Niveau ville minimum
            BuildingCategory.PARK,
            "Espace vert relaxant, améliore la qualité de vie"),

    BOTANICAL_GARDEN(
            "Jardin Botanique",
            "🌺",
            7.0, // Bonus bonheur
            40, // Consommation énergie (kWh)
            10, // Coût maintenance (coins/h) - reduced from 30
            3000, // Coût construction (coins) - reduced from 6000
            4, // Niveau ville minimum
            BuildingCategory.PARK,
            "Jardin exotique, réduit la pollution"),

    // Services publics
    HOSPITAL(
            "Hôpital",
            "🏥",
            10.0, // Bonus bonheur
            200, // Consommation énergie (kWh)
            25, // Coût maintenance (coins/h) - reduced from 100
            7500, // Coût construction (coins) - reduced from 15000
            2, // Niveau ville minimum
            BuildingCategory.PUBLIC_SERVICE,
            "Soins de santé essentiels pour la population"),

    SCHOOL(
            "École",
            "🏫",
            7.0, // Bonus bonheur
            100, // Consommation énergie (kWh)
            15, // Coût maintenance (coins/h) - reduced from 60
            5000, // Coût construction (coins) - reduced from 10000
            2, // Niveau ville minimum
            BuildingCategory.PUBLIC_SERVICE,
            "Éducation pour la jeunesse, essentiel au développement"),

    UNIVERSITY(
            "Université",
            "🎓",
            12.0, // Bonus bonheur
            250, // Consommation énergie (kWh)
            30, // Coût maintenance (coins/h) - reduced from 120
            15000, // Coût construction (coins) - reduced from 30000
            6, // Niveau ville minimum
            BuildingCategory.PUBLIC_SERVICE,
            "Enseignement supérieur, attire les étudiants"),

    LIBRARY(
            "Bibliothèque",
            "📚",
            4.0, // Bonus bonheur
            40, // Consommation énergie (kWh)
            8, // Coût maintenance (coins/h) - reduced from 25
            2000, // Coût construction (coins) - reduced from 4000
            2, // Niveau ville minimum
            BuildingCategory.PUBLIC_SERVICE,
            "Accès gratuit à la culture et au savoir"),

    // Sécurité
    POLICE_STATION(
            "Commissariat",
            "🚓",
            6.0, // Bonus bonheur
            80, // Consommation énergie (kWh)
            20, // Coût maintenance (coins/h) - reduced from 80
            3500, // Coût construction (coins) - reduced from 7000
            2, // Niveau ville minimum
            BuildingCategory.SECURITY,
            "Maintient l'ordre et la sécurité"),

    FIRE_STATION(
            "Caserne de Pompiers",
            "🚒",
            5.0, // Bonus bonheur
            60, // Consommation énergie (kWh)
            18, // Coût maintenance (coins/h) - reduced from 70
            3000, // Coût construction (coins) - reduced from 6000
            2, // Niveau ville minimum
            BuildingCategory.SECURITY,
            "Protection contre les incendies");

    private final String displayName;
    private final String icon;
    private final double happinessBonus;
    private final double energyConsumption;
    private final double maintenanceCost;
    private final double constructionCost;
    private final int minimumCityLevel;
    private final BuildingCategory category;
    private final String description;

    /**
     * Constructeur
     */
    BuildingType(String displayName, String icon,
            double happinessBonus, double energyConsumption,
            double maintenanceCost, double constructionCost,
            int minimumCityLevel, BuildingCategory category,
            String description) {
        this.displayName = displayName;
        this.icon = icon;
        this.happinessBonus = happinessBonus;
        this.energyConsumption = energyConsumption;
        this.maintenanceCost = maintenanceCost;
        this.constructionCost = constructionCost;
        this.minimumCityLevel = minimumCityLevel;
        this.category = category;
        this.description = description;
    }

    /**
     * Vérifie si débloqué pour un niveau de ville
     */
    public boolean isUnlockedAt(int cityLevel) {
        return cityLevel >= minimumCityLevel;
    }

    /**
     * Retourne le bonus de bonheur pour un niveau donné
     */
    public double getHappinessBonus(int level) {
        return happinessBonus * level;
    }

    /**
     * Retourne la consommation d'énergie pour un niveau donné
     */
    public double getEnergyConsumption(int level) {
        return energyConsumption * level;
    }

    /**
     * Retourne un type d'infrastructure aléatoire (pour croissance ville)
     */
    public static BuildingType randomInfrastructure(Random random) {
        BuildingType[] types = values();
        return types[random.nextInt(types.length)];
    }

    /**
     * Retourne un type aléatoire pour un niveau de ville donné
     */
    public static BuildingType randomForCityLevel(int cityLevel, Random random) {
        BuildingType[] available = java.util.Arrays.stream(values())
                .filter(type -> type.isUnlockedAt(cityLevel))
                .toArray(BuildingType[]::new);

        return available.length > 0 ? available[random.nextInt(available.length)] : PARK;
    }

    /**
     * Retourne la couleur représentative
     */
    public String getColor() {
        return switch (category) {
            case COMMERCIAL -> "#8b5cf6"; // Violet
            case ENTERTAINMENT -> "#ec4899"; // Rose
            case PARK -> "#10b981"; // Vert
            case PUBLIC_SERVICE -> "#3b82f6"; // Bleu
            case SECURITY -> "#ef4444"; // Rouge
        };
    }

    // Getters
    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public double getHappinessBonus() {
        return happinessBonus;
    }

    public double getEnergyConsumption() {
        return energyConsumption;
    }

    public double getMaintenanceCost() {
        return maintenanceCost;
    }

    public double getConstructionCost() {
        return constructionCost;
    }

    public int getMinimumCityLevel() {
        return minimumCityLevel;
    }

    public BuildingCategory getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
