package org.td.model.entities;

import org.td.model.enums.PowerPlantType;

/**
 * Classe abstraite représentant une centrale électrique
 * Produit de l'énergie et génère des coûts
 */
public abstract class PowerPlant extends Building {
    private static final long serialVersionUID = 1L;

    protected PowerPlantType plantType;
    protected double maxProduction; // Production maximale en kWh
    protected double currentProduction; // Production actuelle en kWh
    protected double efficiency; // Efficacité (0.0 - 1.0)
    protected double maintenanceCostPerHour; // Coût de maintenance par heure
    protected double pollutionLevel; // Niveau de pollution (0-10)
    protected double operatingCostPerKWh; // Coût opérationnel par kWh produit

    // Statistiques
    protected double totalEnergyProduced; // Total d'énergie produite
    protected int hoursSinceLastMaintenance; // Heures depuis dernière maintenance
    protected int maintenanceInterval; // Intervalle de maintenance (en heures)

    /**
     * Constructeur
     */
    public PowerPlant(PowerPlantType type, int level, int x, int y,
                      double maxProduction, double constructionCost) {
        super(level, x, y);
        this.plantType = type;
        this.maxProduction = maxProduction;
        this.currentProduction = maxProduction;
        this.efficiency = 1.0;
        this.constructionCost = constructionCost;
        this.totalEnergyProduced = 0;
        this.hoursSinceLastMaintenance = 0;
        this.maintenanceInterval = 720; // 30 jours par défaut
    }


    /**
     * Mise à jour de la centrale (chaque heure de jeu)
     */
    @Override
    public void update() {
        super.update();

        if (!isActive || isUnderConstruction) {
            currentProduction = 0;
            return;
        }

        // Calcul de la production
        updateProduction();

        // Dégradation de l'efficacité
        degradeEfficiency();

        // Accumulation statistiques
        totalEnergyProduced += currentProduction;
        hoursSinceLastMaintenance++;

        // Vérification maintenance
        if (hoursSinceLastMaintenance >= maintenanceInterval) {
            applyMaintenancePenalty();
        }
    }
    /**
     * Calcule la production actuelle (à surcharger par sous-classes si nécessaire)
     */
    protected void updateProduction() {
        currentProduction = maxProduction * efficiency;
    }

    /**
     * Dégrade l'efficacité au fil du temps
     */
    protected void degradeEfficiency() {
        // Dégradation progressive (0.01% par heure)
        efficiency = Math.max(0.5, efficiency - 0.0001);
    }


    /**
     * Effectue la maintenance de la centrale
     */
    public double performMaintenance() {
        double cost = maintenanceCostPerHour * 24 * 7; // Une semaine de maintenance
        efficiency = Math.min(1.0, efficiency + 0.3);
        hoursSinceLastMaintenance = 0;
        return cost;
    }

    /**
     * Améliore la centrale
     */
    @Override
    public boolean upgrade() {
        if (!canUpgrade()) return false;

        level++;
        maxProduction *= 1.5; // +50% de production
        efficiency = 1.0; // Réinitialise l'efficacité
        maintenanceCostPerHour *= 1.3; // +30% de coûts
        hoursSinceLastMaintenance = 0;

        super.upgrade();
        return true;
    }

    /**
     * Calcule le coût total par heure (maintenance + opération)
     */
    public double getHourlyCost() {
        double operatingCost = currentProduction * operatingCostPerKWh;
        return maintenanceCostPerHour + operatingCost;
    }

    /**
     * Calcule la pollution générée par heure
     */
    public double getHourlyPollution() {
        return pollutionLevel * (currentProduction / maxProduction);
    }

    /**
     * Retourne le statut de la centrale
     */
    public String getStatus() {
        if (!isActive) return "Inactive";
        if (isUnderConstruction) return "En construction";
        if (efficiency < 0.5) return "Nécessite maintenance urgente";
        if (efficiency < 0.7) return "Maintenance recommandée";
        return "Opérationnelle";
    }

    /**
     * Vérifie si la maintenance est nécessaire
     */
    /**
     * Vérifie si la maintenance est nécessaire
     */
    public boolean needsMaintenance() {  // ✅ PUBLIC et retourne BOOLEAN
        return efficiency < 0.7 || hoursSinceLastMaintenance >= maintenanceInterval;
    }

    /**
     * Appelé quand la maintenance n'est pas effectuée à temps
     */
    protected void applyMaintenancePenalty() {
        // Réduction supplémentaire d'efficacité si pas entretenue
        efficiency = Math.max(0.3, efficiency - 0.05);
    }

    @Override
    public int getWidth() {
        return 2; // Les centrales font 2x2 cellules
    }

    @Override
    public int getHeight() {
        return 2;
    }

    @Override
    public String getDescription() {
        return String.format(
                "%s - Niveau %d\n" +
                        "Production: %.0f / %.0f kWh\n" +
                        "Efficacité: %.0f%%\n" +
                        "Coût/h: %.2f €\n" +
                        "Pollution: %.1f\n" +
                        "Statut: %s",
                plantType.getDisplayName(), level,
                currentProduction, maxProduction,
                efficiency * 100,
                getHourlyCost(),
                pollutionLevel,
                getStatus()
        );
    }

    // Getters
    public PowerPlantType getPlantType() {
        return plantType;
    }

    public double getMaxProduction() {
        return maxProduction;
    }

    public double getCurrentProduction() {
        return currentProduction;
    }

    public double getEfficiency() {
        return efficiency;
    }

    public double getMaintenanceCostPerHour() {
        return maintenanceCostPerHour;
    }

    public double getPollutionLevel() {
        return pollutionLevel;
    }

    public double getOperatingCostPerKWh() {
        return operatingCostPerKWh;
    }

    public double getTotalEnergyProduced() {
        return totalEnergyProduced;
    }

    public int getHoursSinceLastMaintenance() {
        return hoursSinceLastMaintenance;
    }

    public int getMaintenanceInterval() {
        return maintenanceInterval;
    }

    // Setters
    public void setEfficiency(double efficiency) {
        this.efficiency = Math.max(0, Math.min(1.0, efficiency));
    }
}