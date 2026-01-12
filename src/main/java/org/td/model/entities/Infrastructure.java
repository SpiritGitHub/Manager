package org.td.model.entities;

import org.td.model.enums.BuildingType;

/**
 * Infrastructure publique (commercial, divertissement, parcs, etc.)
 * Améliore le bonheur et attire population
 */
public class Infrastructure extends Building {
    private static final long serialVersionUID = 1L;

    private BuildingType infrastructureType;
    private double happinessBonus; // Bonus de bonheur apporté
    private double energyConsumption; // Consommation énergétique
    private double maintenanceCost; // Coût de maintenance par heure
    private int visitorCapacity; // Capacité de visiteurs
    private int currentVisitors; // Visiteurs actuels
    private double revenuePerVisitor; // Revenu par visiteur

    /**
     * Constructeur
     */
    public Infrastructure(BuildingType type, int x, int y) {
        super(1, x, y);
        this.infrastructureType = type;

        initializeByType();
        this.constructionCost = getConstructionCostByType();
    }

    /**
     * Initialise les paramètres selon le type
     */
    private void initializeByType() {
        switch(infrastructureType) {
            case BOTANICAL_GARDEN -> {
                happinessBonus = 7.0;
                energyConsumption = 40;
                maintenanceCost = 30;
                visitorCapacity = 250;
                revenuePerVisitor = 1.0;
            }

            case UNIVERSITY -> {
                happinessBonus = 12.0;
                energyConsumption = 250;
                maintenanceCost = 120;
                visitorCapacity = 800;
                revenuePerVisitor = 2.5;
            }

            case COMMERCIAL -> {
                happinessBonus = 3.0;
                energyConsumption = 80;
                maintenanceCost = 20;
                visitorCapacity = 200;
                revenuePerVisitor = 0.5;
            }
            case ENTERTAINMENT -> {
                happinessBonus = 8.0;
                energyConsumption = 150;
                maintenanceCost = 50;
                visitorCapacity = 500;
                revenuePerVisitor = 2.0;
            }
            case PARK -> {
                happinessBonus = 5.0;
                energyConsumption = 20;
                maintenanceCost = 10;
                visitorCapacity = 300;
                revenuePerVisitor = 0.0; // Gratuit
            }
            case HOSPITAL -> {
                happinessBonus = 10.0;
                energyConsumption = 200;
                maintenanceCost = 100;
                visitorCapacity = 150;
                revenuePerVisitor = 5.0;
            }
            case SCHOOL -> {
                happinessBonus = 7.0;
                energyConsumption = 100;
                maintenanceCost = 60;
                visitorCapacity = 400;
                revenuePerVisitor = 0.0; // Public
            }
            case POLICE_STATION -> {
                happinessBonus = 6.0;
                energyConsumption = 80;
                maintenanceCost = 80;
                visitorCapacity = 50;
                revenuePerVisitor = 0.0; // Service public
            }
            case FIRE_STATION -> {
                happinessBonus = 5.0;
                energyConsumption = 60;
                maintenanceCost = 70;
                visitorCapacity = 30;
                revenuePerVisitor = 0.0; // Service public
            }
            case STADIUM -> {
                happinessBonus = 15.0;
                energyConsumption = 300;
                maintenanceCost = 150;
                visitorCapacity = 2000;
                revenuePerVisitor = 3.0;
            }
            case MUSEUM -> {
                happinessBonus = 6.0;
                energyConsumption = 80;
                maintenanceCost = 40;
                visitorCapacity = 250;
                revenuePerVisitor = 1.0;
            }
            case LIBRARY -> {
                happinessBonus = 4.0;
                energyConsumption = 40;
                maintenanceCost = 25;
                visitorCapacity = 150;
                revenuePerVisitor = 0.0; // Gratuit
            }
        }
    }

    /**
     * Retourne le coût de construction selon le type
     */
    private double getConstructionCostByType() {
        return switch(infrastructureType) {
            case COMMERCIAL -> 3000;
            case ENTERTAINMENT -> 8000;
            case PARK -> 2000;
            case HOSPITAL -> 15000;
            case SCHOOL -> 10000;
            case POLICE_STATION -> 7000;
            case FIRE_STATION -> 6000;
            case STADIUM -> 25000;
            case MUSEUM -> 5000;
            case LIBRARY -> 4000;
            case BOTANICAL_GARDEN -> 6000;
            case UNIVERSITY -> 30000;
        };
    }

    @Override
    public void update() {
        super.update();

        if (!isActive || isUnderConstruction) {
            currentVisitors = 0;
            return;
        }

        // Simulation de visiteurs (varie selon heure et type)
        updateVisitors();
    }

    /**
     * Met à jour le nombre de visiteurs
     */
    private void updateVisitors() {
        // Variation selon type et heure
        double occupancyRate = 0.3 + Math.random() * 0.5; // 30-80%

        // Ajustement selon le type
        if (infrastructureType == BuildingType.ENTERTAINMENT ||
                infrastructureType == BuildingType.STADIUM) {
            occupancyRate *= 0.8; // Plus de monde en soirée
        }

        currentVisitors = (int)(visitorCapacity * occupancyRate);
    }

    /**
     * Calcule les revenus générés par heure
     */
    public double getHourlyRevenue() {
        if (!isActive || !hasElectricity()) return 0;
        return currentVisitors * revenuePerVisitor;
    }

    /**
     * Calcule la contribution au bonheur (pondérée par l'activité)
     */
    public double getHappinessContribution() {
        if (!isActive || !hasElectricity()) return 0;

        double activityMultiplier = currentVisitors / (double)visitorCapacity;
        return happinessBonus * (0.5 + activityMultiplier * 0.5);
    }

    /**
     * Vérifie si l'infrastructure a de l'électricité
     */
    private boolean hasElectricity() {
        // Sera géré par le système de distribution
        return true; // Placeholder
    }

    @Override
    public boolean upgrade() {
        if (!canUpgrade()) return false;

        level++;
        happinessBonus *= 1.3;
        visitorCapacity = (int)(visitorCapacity * 1.5);
        energyConsumption *= 1.4;
        maintenanceCost *= 1.3;

        super.upgrade();
        return true;
    }

    @Override
    public String getType() {
        return infrastructureType.name();
    }

    @Override
    public int getWidth() {
        return switch(infrastructureType) {
            case STADIUM -> 3;
            case HOSPITAL, SCHOOL, UNIVERSITY -> 2;
            default -> 1;
        };
    }

    @Override
    public int getHeight() {
        return switch(infrastructureType) {
            case STADIUM -> 3;
            case HOSPITAL, SCHOOL, UNIVERSITY -> 2;
            default -> 1;
        };
    }

    @Override
    public String getDescription() {
        return String.format(
                "%s - Niveau %d\n" +
                        "Visiteurs: %d/%d\n" +
                        "Bonheur: +%.1f\n" +
                        "Énergie: %.0f kWh\n" +
                        "Maintenance: %.2f €/h\n" +
                        "Revenu: %.2f €/h",
                infrastructureType.getDisplayName(), level,
                currentVisitors, visitorCapacity,
                getHappinessContribution(),
                energyConsumption,
                maintenanceCost,
                getHourlyRevenue()
        );
    }

    // Getters
    public BuildingType getInfrastructureType() {
        return infrastructureType;
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

    public int getVisitorCapacity() {
        return visitorCapacity;
    }

    public int getCurrentVisitors() {
        return currentVisitors;
    }

    public double getRevenuePerVisitor() {
        return revenuePerVisitor;
    }
}