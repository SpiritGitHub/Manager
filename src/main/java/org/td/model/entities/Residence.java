package org.td.model.entities;

import org.td.model.enums.ResidenceLevel;

import java.util.Random;

/**
 * Représente une résidence dans la ville
 * Génère de la demande énergétique et des revenus
 */
public class Residence extends Building {
    private static final long serialVersionUID = 1L;

    private ResidenceLevel residenceLevel;
    private double energyDemand; // Demande énergétique actuelle en kWh
    private double baseEnergyDemand; // Demande de base
    private int population; // Nombre d'habitants
    private double satisfaction; // Satisfaction des habitants (0-100)
    private double revenuePerHour; // Revenu généré par heure
    private boolean hasElectricity; // A de l'électricité actuellement
    private int hoursWithoutElectricity; // Heures consécutives sans électricité

    private Random random;

    /**
     * Constructeur
     */
    public Residence(ResidenceLevel level, int x, int y) {
        super(level.getLevel(), x, y);
        this.residenceLevel = level;
        this.random = new Random();
        this.satisfaction = 75.0;
        this.hasElectricity = true;
        this.hoursWithoutElectricity = 0;

        initializePopulation();
        calculateBaseEnergyDemand();
        calculateRevenue();

        this.constructionCost = level.getConstructionCost();
    }

    /**
     * Initialise la population selon le niveau
     */
    private void initializePopulation() {
        int basePopulation = switch (residenceLevel) {
            case BASIC -> 20 + random.nextInt(30); // 20-50 habitants
            case MEDIUM -> 50 + random.nextInt(50); // 50-100 habitants
            case ADVANCED -> 100 + random.nextInt(100); // 100-200 habitants
        };
        this.population = basePopulation;
    }

    /**
     * Calcule la demande énergétique de base
     */
    private void calculateBaseEnergyDemand() {
        double min = residenceLevel.getMinEnergyDemand();
        double max = residenceLevel.getMaxEnergyDemand();
        double variance = random.nextDouble();

        this.baseEnergyDemand = min + variance * (max - min);
        this.baseEnergyDemand *= (population / 35.0); // Ajustement selon population
        this.energyDemand = this.baseEnergyDemand;
    }

    /**
     * Calcule les revenus générés
     */
    private void calculateRevenue() {
        double min = residenceLevel.getMinRevenue();
        double max = residenceLevel.getMaxRevenue();
        this.revenuePerHour = min + random.nextDouble() * (max - min);
        this.revenuePerHour *= (population / 50.0); // Ajustement selon population
    }

    /**
     * Mise à jour de la résidence
     */
    @Override
    public void update() {
        super.update();

        // Gestion de l'électricité
        if (!hasElectricity) {
            hoursWithoutElectricity++;
            satisfaction -= 2.0; // Perte de satisfaction
        } else {
            hoursWithoutElectricity = 0;
            if (satisfaction < 100) {
                satisfaction += 0.5; // Récupération lente
            }
        }

        // Ajustement de la satisfaction
        satisfaction = Math.max(0, Math.min(100, satisfaction));

        // Risque d'abandon si satisfaction trop basse
        if (satisfaction < 20 && random.nextDouble() < 0.1) {
            // Perte de population
            population = Math.max(5, population - 5);
            calculateBaseEnergyDemand();
            calculateRevenue();
        }

        // Croissance de population si satisfaction élevée
        if (satisfaction > 60 && hasElectricity && random.nextDouble() < 0.05) {
            population += random.nextInt(3) + 1;
            calculateBaseEnergyDemand();
            calculateRevenue();
        }
    }

    /**
     * Met à jour la demande énergétique selon l'heure
     */
    public void updateDemand(int hour) {
        double multiplier = calculateHourlyMultiplier(hour);
        this.energyDemand = this.baseEnergyDemand * multiplier;

        // Variation aléatoire légère
        this.energyDemand *= (0.95 + random.nextDouble() * 0.1);
    }

    /**
     * Calcule le multiplicateur selon l'heure
     */
    private double calculateHourlyMultiplier(int hour) {
        if (hour >= 0 && hour < 6) {
            return 0.4; // Nuit - faible consommation
        } else if (hour >= 6 && hour < 9) {
            return 1.5; // Matin - pic
        } else if (hour >= 9 && hour < 17) {
            return 0.8; // Journée - moyenne
        } else if (hour >= 17 && hour < 22) {
            return 1.8; // Soirée - pic maximal
        } else {
            return 1.0; // Fin de soirée
        }
    }

    /**
     * Améliore la résidence au niveau suivant
     */
    @Override
    public boolean upgrade() {
        if (!canUpgrade())
            return false;

        ResidenceLevel nextLevel = switch (residenceLevel) {
            case BASIC -> ResidenceLevel.MEDIUM;
            case MEDIUM -> ResidenceLevel.ADVANCED;
            case ADVANCED -> null;
        };

        if (nextLevel != null) {
            this.residenceLevel = nextLevel;
            this.level = nextLevel.getLevel();
            this.population += 30 + random.nextInt(20);

            calculateBaseEnergyDemand();
            calculateRevenue();

            // Boost de satisfaction pour l'amélioration
            this.satisfaction = Math.min(100, satisfaction + 10);

            super.upgrade();
            return true;
        }
        return false;
    }

    /**
     * Définit si la résidence a de l'électricité
     */
    public void setHasElectricity(boolean hasElectricity) {
        this.hasElectricity = hasElectricity;
    }

    /**
     * Calcule la contribution au bonheur global
     */
    public double getHappinessContribution() {
        return satisfaction * (population / 100.0);
    }

    @Override
    public String getType() {
        return "Residence_" + residenceLevel.name();
    }

    @Override
    public int getWidth() {
        return 1; // 1 cellule de largeur
    }

    @Override
    public int getHeight() {
        return 1; // 1 cellule de hauteur
    }

    @Override
    public String getDescription() {
        return String.format("Résidence %s\nNiveau: %d\nPopulation: %d\nDemande: %.0f kWh\nSatisfaction: %.0f%%",
                residenceLevel.name(), level, population, energyDemand, satisfaction);
    }

    // Getters
    public ResidenceLevel getResidenceLevel() {
        return residenceLevel;
    }

    public double getEnergyDemand() {
        return energyDemand;
    }

    public double getBaseEnergyDemand() {
        return baseEnergyDemand;
    }

    public int getPopulation() {
        return population;
    }

    public double getSatisfaction() {
        return satisfaction;
    }

    public double getRevenuePerHour() {
        return hasElectricity ? revenuePerHour : revenuePerHour * 0.3; // Revenu réduit sans électricité
    }

    public boolean hasElectricity() {
        return hasElectricity;
    }

    public int getHoursWithoutElectricity() {
        return hoursWithoutElectricity;
    }
}