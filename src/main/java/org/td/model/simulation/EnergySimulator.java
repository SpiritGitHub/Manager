package org.td.model.simulation;

import org.td.model.entities.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Simule le système énergétique de la ville
 * Gère la production, distribution et optimisation
 */
public class EnergySimulator {
    private City city;
    private Random random;

    // État du système énergétique
    private double gridStability; // Stabilité du réseau (0-100)
    private double transmissionLoss; // Pertes de transmission (%)
    private List<PowerOutage> activeOutages; // Pannes actives

    // Statistiques
    private double peakDemand; // Demande maximale observée
    private double peakProduction; // Production maximale observée
    private int totalOutages; // Nombre total de pannes

    /**
     * Constructeur
     */
    public EnergySimulator(City city) {
        this.city = city;
        this.random = new Random();
        this.gridStability = 100.0;
        this.transmissionLoss = 5.0; // 5% de perte par défaut
        this.activeOutages = new ArrayList<>();
        this.peakDemand = 0;
        this.peakProduction = 0;
        this.totalOutages = 0;
    }

    /**
     * Met à jour la simulation énergétique
     */
    public void update() {
        updateGridStability();
        updateTransmissionLoss();
        checkForOutages();
        updateStatistics();
        optimizeDistribution();
    }

    /**
     * Met à jour la stabilité du réseau
     */
    private void updateGridStability() {
        double production = city.getTotalEnergyProduction();
        double demand = city.getTotalEnergyDemand();

        if (demand == 0) {
            gridStability = 100.0;
            return;
        }

        double ratio = production / demand;

        // Stabilité optimale entre 1.0 et 1.25 (production légèrement supérieure)
        // REFACTOR: Logique plus souple pour éviter les chutes drastiques
        if (ratio >= 1.0 && ratio <= 1.25) {
            gridStability = Math.min(100, gridStability + 1.0);
        } else if (ratio < 0.8) {
            // Pénurie importante
            gridStability = Math.max(0, gridStability - 1.0);
        } else if (ratio < 1.0) {
            // Pénurie légère
            gridStability = Math.max(0, gridStability - 0.2);
        } else if (ratio > 1.5) {
            // Surproduction excessive (gaspillage)
            gridStability = Math.max(90, gridStability - 0.1);
        }

        // Impact des centrales mal entretenues
        for (PowerPlant plant : city.getPowerPlants()) {
            if (plant.getEfficiency() < 0.4) { // Seuil abaissé à 40%
                gridStability = Math.max(0, gridStability - 0.05);
            }
        }
    }

    /**
     * Calcule les pertes de transmission
     */
    private void updateTransmissionLoss() {
        // Perte de base: 5%
        transmissionLoss = 5.0;

        // Augmente avec la taille du réseau
        int totalBuildings = city.getResidences().size() +
                city.getInfrastructures().size();
        transmissionLoss += totalBuildings * 0.02; // +0.02% par bâtiment

        // Réduite si bonne stabilité
        if (gridStability > 80) {
            transmissionLoss *= 0.8;
        }

        // Limite à 15%
        transmissionLoss = Math.min(15.0, transmissionLoss);
    }

    /**
     * Vérifie et génère des pannes aléatoires
     */
    private void checkForOutages() {
        // Supprime les pannes terminées
        activeOutages.removeIf(PowerOutage::isResolved);

        // Risque de panne si stabilité faible
        if (gridStability < 30 && random.nextDouble() < 0.05) {
            createOutage("Instabilité du réseau");
        }

        // Panne aléatoire rare
        if (random.nextDouble() < 0.001) { // 0.1% par heure
            createOutage("Incident technique");
        }

        // Panne si centrale nucléaire en danger
        for (PowerPlant plant : city.getPowerPlants()) {
            if (plant instanceof NuclearPlant nuclear) {
                if (nuclear.isInDanger() && random.nextDouble() < 0.1) {
                    createOutage("Problème centrale nucléaire");
                }
            }
        }
    }

    /**
     * Crée une nouvelle panne
     */
    private void createOutage(String cause) {
        int duration = 1 + random.nextInt(6); // 1-6 heures
        double affectedPercentage = 10 + random.nextDouble() * 40; // 10-50%

        PowerOutage outage = new PowerOutage(cause, duration, affectedPercentage);
        activeOutages.add(outage);
        totalOutages++;

        System.out.println("⚠️ PANNE ÉLECTRIQUE: " + cause +
                " (" + (int) affectedPercentage + "% affecté, " +
                duration + "h)");
    }

    /**
     * Met à jour les statistiques
     */
    private void updateStatistics() {
        peakDemand = Math.max(peakDemand, city.getTotalEnergyDemand());
        peakProduction = Math.max(peakProduction, city.getTotalEnergyProduction());
    }

    /**
     * Optimise la distribution d'énergie
     */
    private void optimizeDistribution() {
        double production = city.getTotalEnergyProduction();
        double demand = city.getTotalEnergyDemand();

        // Applique les pertes de transmission
        double availableEnergy = production * (1 - transmissionLoss / 100.0);

        // Applique l'effet des pannes
        for (PowerOutage outage : activeOutages) {
            availableEnergy *= (1 - outage.getAffectedPercentage() / 100.0);
        }

        // Détermine si l'électricité est suffisante
        boolean sufficient = availableEnergy >= demand * 0.9;

        // Distribue aux résidences
        for (Residence residence : city.getResidences()) {
            residence.setHasElectricity(sufficient);
        }
    }

    /**
     * Calcule le taux de couverture énergétique
     */
    public double getCoverageRate() {
        double demand = city.getTotalEnergyDemand();
        if (demand == 0)
            return 100.0;

        double production = city.getTotalEnergyProduction();
        double available = production * (1 - transmissionLoss / 100.0);

        return Math.min(100.0, (available / demand) * 100.0);
    }

    /**
     * Calcule l'efficacité moyenne des centrales
     */
    public double getAverageEfficiency() {
        List<PowerPlant> plants = city.getPowerPlants();
        if (plants.isEmpty())
            return 0;

        return plants.stream()
                .mapToDouble(PowerPlant::getEfficiency)
                .average()
                .orElse(0) * 100.0;
    }

    /**
     * Retourne le nombre de centrales nécessitant maintenance
     */
    public long getPlantsNeedingMaintenance() {
        return city.getPowerPlants().stream()
                .filter(PowerPlant::needsMaintenance)
                .count();
    }

    /**
     * Calcule la capacité de réserve (%)
     */
    public double getReserveCapacity() {
        double demand = city.getTotalEnergyDemand();
        if (demand == 0)
            return 100.0;

        double production = city.getTotalEnergyProduction();
        return ((production - demand) / demand) * 100.0;
    }

    /**
     * Recommande des actions d'optimisation
     */
    public List<String> getRecommendations() {
        List<String> recommendations = new ArrayList<>();

        // Vérifier capacité
        double coverage = getCoverageRate();
        if (coverage < 90) {
            recommendations.add("⚠️ Construire des centrales supplémentaires");
        } else if (coverage > 150) {
            recommendations.add("💡 Surproduction: Envisager de désactiver des centrales");
        }

        // Vérifier stabilité
        if (gridStability < 50) {
            recommendations.add("⚠️ Stabilité faible: Vérifier l'équilibre production/demande");
        }

        // Vérifier maintenance
        long needMaintenance = getPlantsNeedingMaintenance();
        if (needMaintenance > 0) {
            recommendations.add("🔧 " + needMaintenance +
                    " centrale(s) nécessitent une maintenance");
        }

        // Vérifier efficacité
        double avgEfficiency = getAverageEfficiency();
        if (avgEfficiency < 70) {
            recommendations.add("📉 Efficacité moyenne faible: Effectuer la maintenance");
        }

        // Vérifier pannes
        if (!activeOutages.isEmpty()) {
            recommendations.add("⚡ " + activeOutages.size() + " panne(s) en cours");
        }

        return recommendations;
    }

    /**
     * Génère un rapport énergétique détaillé
     */
    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== RAPPORT ÉNERGÉTIQUE ===\n\n");

        report.append(String.format("Production totale: %.0f kWh\n",
                city.getTotalEnergyProduction()));
        report.append(String.format("Demande totale: %.0f kWh\n",
                city.getTotalEnergyDemand()));
        report.append(String.format("Taux de couverture: %.1f%%\n", getCoverageRate()));
        report.append(String.format("Stabilité réseau: %.1f%%\n", gridStability));
        report.append(String.format("Pertes transmission: %.1f%%\n", transmissionLoss));
        report.append(String.format("Efficacité moyenne: %.1f%%\n\n", getAverageEfficiency()));

        report.append("Centrales actives:\n");
        for (PowerPlant plant : city.getPowerPlants()) {
            report.append(String.format("  - %s: %.0f/%.0f kWh (%.0f%%)\n",
                    plant.getType(),
                    plant.getCurrentProduction(),
                    plant.getMaxProduction(),
                    plant.getEfficiency() * 100));
        }

        return report.toString();
    }

    // Getters
    public double getGridStability() {
        return gridStability;
    }

    public double getTransmissionLoss() {
        return transmissionLoss;
    }

    public List<PowerOutage> getActiveOutages() {
        return new ArrayList<>(activeOutages);
    }

    public double getPeakDemand() {
        return peakDemand;
    }

    public double getPeakProduction() {
        return peakProduction;
    }

    public int getTotalOutages() {
        return totalOutages;
    }
}

/**
 * Représente une panne électrique
 */
class PowerOutage {
    private String cause;
    private int durationHours;
    private int hoursRemaining;
    private double affectedPercentage;

    public PowerOutage(String cause, int duration, double affectedPercentage) {
        this.cause = cause;
        this.durationHours = duration;
        this.hoursRemaining = duration;
        this.affectedPercentage = affectedPercentage;
    }

    public void decrementHour() {
        hoursRemaining--;
    }

    public boolean isResolved() {
        return hoursRemaining <= 0;
    }

    public String getCause() {
        return cause;
    }

    public int getHoursRemaining() {
        return hoursRemaining;
    }

    public double getAffectedPercentage() {
        return affectedPercentage;
    }
}