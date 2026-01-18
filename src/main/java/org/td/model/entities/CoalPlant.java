package org.td.model.entities;

import org.td.model.enums.PowerPlantType;

/**
 * Centrale à charbon
 * - Avantages: Peu coûteuse, production stable, facile à construire
 * - Inconvénients: Très polluante, coûts opérationnels élevés
 */
public class CoalPlant extends PowerPlant {
    private static final long serialVersionUID = 1L;

    // Caractéristiques spécifiques au charbon
    private double coalReserve; // Réserve de charbon (en tonnes)
    private double coalConsumptionRate; // Consommation de charbon par kWh
    private double coalCostPerTon; // Coût du charbon par tonne

    /**
     * Constructeur
     */
    public CoalPlant(int level, int x, int y) {
        super(PowerPlantType.COAL, level, x, y,
                500 * level, // Production de base
                5000 * level); // Coût de construction

        // Paramètres spécifiques
        this.maintenanceCostPerHour = 30 + (level * 10);
        this.pollutionLevel = 8.0 + (level * 0.5); // Très polluant
        this.operatingCostPerKWh = 0.08; // 8 centimes par kWh
        this.maintenanceInterval = 120; // 5 jours ! Il faut bosser.

        // Charbon
        this.coalReserve = 200.0; // 200 tonnes au départ (s'épuise plus vite)
        this.coalConsumptionRate = 0.4; // 0.4 kg par kWh
        this.coalCostPerTon = 100; // 100€ la tonne
    }

    @Override
    protected void updateProduction() {
        if (coalReserve <= 0) {
            // Plus de charbon, production nulle
            currentProduction = 0;
            return;
        }

        // Production normale
        currentProduction = maxProduction * efficiency;

        // Consommation de charbon
        double coalUsed = (currentProduction * coalConsumptionRate) / 1000.0; // Conversion kg -> tonnes
        coalReserve = Math.max(0, coalReserve - coalUsed);
    }

    /**
     * Réapprovisionne en charbon
     * 
     * @param tons Nombre de tonnes à acheter
     * @return Coût total
     */
    public double buyCoal(double tons) {
        coalReserve += tons;
        return tons * coalCostPerTon;
    }

    /**
     * Calcule le coût horaire incluant le charbon
     */
    @Override
    public double getHourlyCost() {
        double baseCost = super.getHourlyCost();
        double coalCost = (currentProduction * coalConsumptionRate / 1000.0) * coalCostPerTon;
        return baseCost + coalCost;
    }

    @Override
    public String getType() {
        return "CoalPlant";
    }

    @Override
    public String getDescription() {
        return super.getDescription() +
                String.format("\nRéserve charbon: %.1f tonnes", coalReserve);
    }

    // Getters spécifiques
    public double getCoalReserve() {
        return coalReserve;
    }

    public double getCoalConsumptionRate() {
        return coalConsumptionRate;
    }

    public double getCoalCostPerTon() {
        return coalCostPerTon;
    }

    /**
     * Estime combien d'heures la réserve actuelle peut tenir
     */
    public double getHoursUntilEmpty() {
        if (currentProduction == 0)
            return Double.POSITIVE_INFINITY;
        double hourlyConsumption = (currentProduction * coalConsumptionRate) / 1000.0;
        return coalReserve / hourlyConsumption;
    }
}