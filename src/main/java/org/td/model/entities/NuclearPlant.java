package org.td.model.entities;

import org.td.model.enums.PowerPlantType;

import java.util.Random;

/**
 * Centrale nucléaire
 * - Avantages: Production massive et stable, faible pollution
 * - Inconvénients: Très coûteuse, risque d'incident, maintenance critique
 */
public class NuclearPlant extends PowerPlant {
    private static final long serialVersionUID = 1L;

    // Caractéristiques spécifiques au nucléaire
    private double safetyLevel; // Niveau de sécurité (0-1)
    private double radioactiveWaste; // Déchets radioactifs accumulés (en kg)
    private double fuelReserve; // Réserve de combustible (en kg)
    private double fuelConsumptionRate; // Consommation combustible par kWh
    private double temperature; // Température du réacteur (°C)
    private int incidentRiskLevel; // Niveau de risque d'incident (0-10)

    private Random random;

    /**
     * Constructeur
     */
    public NuclearPlant(int level, int x, int y) {
        super(PowerPlantType.NUCLEAR, level, x, y,
                2000 * level, // Production massive
                50000 * level); // Très coûteux

        // Paramètres spécifiques
        this.maintenanceCostPerHour = 150 + (level * 50);
        this.pollutionLevel = 2.0; // Faible pollution directe
        this.operatingCostPerKWh = 0.03; // Coût opérationnel moyen
        this.maintenanceInterval = 360; // 15 jours (maintenance fréquente obligatoire)

        // Sécurité et risques
        this.safetyLevel = 1.0; // Sécurité maximale au départ
        this.radioactiveWaste = 0.0;
        this.temperature = 300.0; // Température normale: 300°C
        this.incidentRiskLevel = 0;

        // Combustible
        this.fuelReserve = 500.0; // 500 kg d'uranium enrichi
        this.fuelConsumptionRate = 0.01; // 0.01 kg par kWh

        this.random = new Random();
    }

    @Override
    protected void updateProduction() {
        if (fuelReserve <= 0) {
            // Plus de combustible
            currentProduction = 0;
            temperature = Math.max(20, temperature - 10); // Refroidissement
            return;
        }

        // Production normale
        currentProduction = maxProduction * efficiency * safetyLevel;

        // Consommation de combustible
        double fuelUsed = currentProduction * fuelConsumptionRate;
        fuelReserve = Math.max(0, fuelReserve - fuelUsed);

        // Production de déchets radioactifs
        radioactiveWaste += fuelUsed * 0.1; // 10% deviennent des déchets

        // Gestion de la température
        updateTemperature();

        // Calcul du risque
        calculateIncidentRisk();
    }

    /**
     * Met à jour la température du réacteur
     */
    private void updateTemperature() {
        double targetTemp = 300 + (currentProduction / maxProduction) * 200; // 300-500°C

        // Convergence progressive vers température cible
        if (temperature < targetTemp) {
            temperature += 5;
        } else if (temperature > targetTemp) {
            temperature -= 3;
        }

        // Surchauffe si maintenance négligée
        if (hoursSinceLastMaintenance > maintenanceInterval * 1.5) {
            temperature += random.nextDouble() * 10;
        }
    }

    /**
     * Calcule le risque d'incident
     */
    private void calculateIncidentRisk() {
        incidentRiskLevel = 0;

        // Facteurs de risque
        if (safetyLevel < 0.5) incidentRiskLevel += 4;
        else if (safetyLevel < 0.7) incidentRiskLevel += 2;

        if (temperature > 600) incidentRiskLevel += 3;
        else if (temperature > 500) incidentRiskLevel += 1;

        if (hoursSinceLastMaintenance > maintenanceInterval * 2) incidentRiskLevel += 3;
        else if (hoursSinceLastMaintenance > maintenanceInterval) incidentRiskLevel += 1;

        // Vérification incident
        if (incidentRiskLevel >= 7 && random.nextDouble() < 0.01) {
            triggerIncident();
        }
    }

    /**
     * Déclenche un incident nucléaire
     */
    private void triggerIncident() {
        // Arrêt d'urgence
        isActive = false;
        currentProduction = 0;
        efficiency = 0.3;
        safetyLevel = 0.2;

        // Contamination massive
        pollutionLevel = 100.0;

        System.err.println("⚠️ INCIDENT NUCLÉAIRE dans la centrale " + getId());
    }

    @Override
    protected void degradeEfficiency() {
        // Dégradation plus lente mais critique
        efficiency = Math.max(0.4, efficiency - 0.00003);

        // Dégradation de la sécurité
        safetyLevel = Math.max(0.3, safetyLevel - 0.00005);
    }

    @Override
    protected void applyMaintenancePenalty() {
        super.applyMaintenancePenalty();

        // Pénalité spécifique nucléaire
        safetyLevel = Math.max(0.2, safetyLevel - 0.1);
        temperature += 20; // surchauffe progressive si maintenance ignorée
    }


    @Override
    public double performMaintenance() {
        double baseCost = super.performMaintenance();

        // Maintenance spécifique nucléaire
        safetyLevel = Math.min(1.0, safetyLevel + 0.5);
        temperature = 300.0; // Réinitialise température
        incidentRiskLevel = 0;

        // Coût supplémentaire pour traitement des déchets
        double wasteCost = radioactiveWaste * 1000; // 1000€ par kg
        radioactiveWaste = 0;

        return baseCost + wasteCost;
    }

    /**
     * Réapprovisionne en combustible nucléaire
     */
    public double refuel(double kg) {
        fuelReserve += kg;
        return kg * 50000; // 50,000€ par kg d'uranium enrichi
    }

    /**
     * Effectue un arrêt d'urgence
     */
    public void emergencyShutdown() {
        isActive = false;
        currentProduction = 0;
        temperature = Math.max(20, temperature - 50);
    }

    /**
     * Redémarre la centrale après arrêt
     */
    public boolean restart() {
        if (safetyLevel > 0.7 && temperature < 400 && fuelReserve > 100) {
            isActive = true;
            return true;
        }
        return false;
    }

    @Override
    public String getType() {
        return "NuclearPlant";
    }

    @Override
    public String getStatus() {
        if (!isActive && pollutionLevel > 50) return "⚠️ INCIDENT NUCLÉAIRE";
        if (incidentRiskLevel >= 7) return "DANGER - Risque critique";
        if (incidentRiskLevel >= 4) return "ATTENTION - Risque élevé";
        return super.getStatus();
    }

    @Override
    public String getDescription() {
        return super.getDescription() +
                String.format("\nSécurité: %.0f%%\nTempérature: %.0f°C\n" +
                                "Combustible: %.1f kg\nDéchets: %.1f kg\nRisque: %d/10",
                        safetyLevel * 100, temperature, fuelReserve,
                        radioactiveWaste, incidentRiskLevel);
    }

    // Getters
    public double getSafetyLevel() {
        return safetyLevel;
    }

    public double getRadioactiveWaste() {
        return radioactiveWaste;
    }

    public double getFuelReserve() {
        return fuelReserve;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getIncidentRiskLevel() {
        return incidentRiskLevel;
    }

    public boolean isInDanger() {
        return incidentRiskLevel >= 7;
    }
}