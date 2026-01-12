package org.td.model.entities;

import org.td.model.enums.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Classe principale représentant la ville et son état global
 */
public class City implements Serializable {
    private static final long serialVersionUID = 1L;

    // Informations de base
    private String name;
    private int level; // Niveau de la ville (1-10)
    private LocalDateTime currentTime;
    private LocalDateTime foundationDate;

    // Ressources
    private double money; // Budget disponible
    private double happiness; // Satisfaction globale (0-100)
    private int population; // Population totale

    // Énergie
    private double totalEnergyProduction;
    private double totalEnergyDemand;
    private double totalEnergyStorage; // Stockage disponible (batteries)
    private double energyBalance; // Production - Demande

    // Statistiques
    private double totalPollution;
    private double totalRevenue;
    private double totalExpenses;
    private int consecutiveHappyHours; // Heures consécutives avec bonheur > 70
    private int consecutiveUnhappyHours; // Heures consécutives avec bonheur < 30

    // Collections de bâtiments
    private List<Residence> residences;
    private List<PowerPlant> powerPlants;
    private List<Infrastructure> infrastructures;

    // Historique (pour graphiques)
    private List<Double> moneyHistory;
    private List<Double> happinessHistory;
    private List<Double> energyHistory;

    private Random random;

    /**
     * Constructeur
     */
    public City(String name) {
        this.name = name;
        this.level = 1;
        this.currentTime = LocalDateTime.of(2025, 1, 1, 0, 0);
        this.foundationDate = LocalDateTime.of(2025, 1, 1, 0, 0);

        // Ressources initiales
        this.money = 50000;
        this.happiness = 75.0;
        this.population = 0;

        // Initialisation collections
        this.residences = new ArrayList<>();
        this.powerPlants = new ArrayList<>();
        this.infrastructures = new ArrayList<>();

        this.moneyHistory = new ArrayList<>();
        this.happinessHistory = new ArrayList<>();
        this.energyHistory = new ArrayList<>();

        this.random = new Random();

        // Ville de départ
        initializeStartingCity();
    }

    /**
     * Initialise la ville de départ
     */
    private void initializeStartingCity() {
        // Quelques résidences de base
        for (int i = 0; i < 8; i++) {
            int x = 100 + (i % 4) * 120;
            int y = 100 + (i / 4) * 120;
            Residence res = new Residence(ResidenceLevel.BASIC, x, y);
            residences.add(res);
        }

        // Une centrale à charbon de départ
        powerPlants.add(new CoalPlant(1, 400, 400));

        // Un parc
        infrastructures.add(new Infrastructure(BuildingType.PARK, 300, 100));

        updatePopulation();
    }

    /**
     * Avance le temps d'une heure de jeu
     */
    public void advanceTime() {
        currentTime = currentTime.plusHours(1);
        int currentHour = currentTime.getHour();

        // Mise à jour de tous les bâtiments
        updateBuildings(currentHour);

        // Calculs globaux
        updateEnergyBalance();
        updateEconomy();
        updatePopulation();
        updateHappiness();
        updatePollution();

        // Vérification croissance/déclin
        checkCityEvolution();

        // Mise à jour niveau ville
        updateCityLevel();

        // Sauvegarde historique (toutes les 24h)
        if (currentHour == 0) {
            saveHistory();
        }
    }

    /**
     * Met à jour tous les bâtiments
     */
    private void updateBuildings(int hour) {
        // Résidences
        for (Residence res : residences) {
            res.updateDemand(hour);
            res.update();
        }

        // Centrales
        for (PowerPlant plant : powerPlants) {
            // Mise à jour heure pour solaire
            if (plant instanceof SolarPlant) {
                ((SolarPlant) plant).updateHour(hour);
            }
            plant.update();
        }

        // Infrastructures
        for (Infrastructure infra : infrastructures) {
            infra.update();
        }
    }

    /**
     * Calcule le bilan énergétique
     */
    private void updateEnergyBalance() {
        totalEnergyProduction = powerPlants.stream()
                .filter(Building::isActive)
                .mapToDouble(PowerPlant::getCurrentProduction)
                .sum();

        totalEnergyDemand = residences.stream()
                .mapToDouble(Residence::getEnergyDemand)
                .sum();

        totalEnergyDemand += infrastructures.stream()
                .filter(Building::isActive)
                .mapToDouble(Infrastructure::getEnergyConsumption)
                .sum();

        energyBalance = totalEnergyProduction - totalEnergyDemand;

        // Distribution d'électricité
        distributeElectricity();
    }

    /**
     * Distribue l'électricité aux bâtiments
     */
    private void distributeElectricity() {
        double ratio = totalEnergyDemand > 0 ?
                totalEnergyProduction / totalEnergyDemand : 1.0;

        boolean hasElectricity = ratio >= 0.9; // Seuil 90%

        for (Residence res : residences) {
            res.setHasElectricity(hasElectricity);
        }
    }

    /**
     * Met à jour l'économie de la ville
     */
    private void updateEconomy() {
        totalRevenue = 0;
        totalExpenses = 0;

        // Revenus des résidences (vente électricité)
        double energySold = Math.min(totalEnergyProduction, totalEnergyDemand);
        totalRevenue += energySold * 0.15; // 0.15€ par kWh

        // Revenus des infrastructures
        totalRevenue += infrastructures.stream()
                .mapToDouble(Infrastructure::getHourlyRevenue)
                .sum();

        // Dépenses des centrales
        totalExpenses += powerPlants.stream()
                .filter(Building::isActive)
                .mapToDouble(PowerPlant::getHourlyCost)
                .sum();

        // Dépenses des infrastructures
        totalExpenses += infrastructures.stream()
                .filter(Building::isActive)
                .mapToDouble(Infrastructure::getMaintenanceCost)
                .sum();

        // Mise à jour budget
        double netIncome = totalRevenue - totalExpenses;
        money += netIncome;
    }

    /**
     * Met à jour la population totale
     */
    private void updatePopulation() {
        population = residences.stream()
                .mapToInt(Residence::getPopulation)
                .sum();
    }

    /**
     * Met à jour le bonheur global
     */
    private void updateHappiness() {
        double previousHappiness = happiness;

        // Facteur énergie
        double energyRatio = totalEnergyDemand > 0 ?
                totalEnergyProduction / totalEnergyDemand : 1.0;

        if (energyRatio < 0.7) {
            happiness -= 2.0; // Pénurie sévère
        } else if (energyRatio < 0.9) {
            happiness -= 0.5; // Pénurie légère
        } else if (energyRatio >= 1.0) {
            happiness += 0.2; // Approvisionnement stable
        }

        // Contribution des résidences
        if (!residences.isEmpty()) {
            double avgSatisfaction = residences.stream()
                    .mapToDouble(Residence::getSatisfaction)
                    .average()
                    .orElse(50);
            happiness = happiness * 0.7 + avgSatisfaction * 0.3;
        }

        // Contribution des infrastructures
        double infraBonus = infrastructures.stream()
                .mapToDouble(Infrastructure::getHappinessContribution)
                .sum() / Math.max(1, population / 100.0);
        happiness += infraBonus * 0.1;

        // Effet de la pollution
        if (totalPollution > population / 10.0) {
            happiness -= 0.3;
        }

        // Limites
        happiness = Math.max(0, Math.min(100, happiness));

        // Compteurs consécutifs
        if (happiness > 70) {
            consecutiveHappyHours++;
            consecutiveUnhappyHours = 0;
        } else if (happiness < 30) {
            consecutiveUnhappyHours++;
            consecutiveHappyHours = 0;
        } else {
            consecutiveHappyHours = 0;
            consecutiveUnhappyHours = 0;
        }
    }

    /**
     * Met à jour la pollution totale
     */
    private void updatePollution() {
        totalPollution = powerPlants.stream()
                .filter(Building::isActive)
                .mapToDouble(PowerPlant::getHourlyPollution)
                .sum();
    }

    /**
     * Vérifie et applique l'évolution de la ville
     */
    private void checkCityEvolution() {
        int hour = currentTime.getHour();

        // Vérification une fois par jour à minuit
        if (hour != 0) return;

        // Croissance si conditions favorables
        if (happiness > 70 && energyBalance > totalEnergyDemand * 0.2) {
            if (random.nextDouble() < 0.4) { // 40% de chance
                growCity();
            }
        }

        // Déclin si conditions défavorables
        if (happiness < 30 || consecutiveUnhappyHours > 72) {
            if (random.nextDouble() < 0.3) { // 30% de chance
                shrinkCity();
            }
        }

        // Upgrade automatique de résidences
        if (happiness > 80 && random.nextDouble() < 0.2) {
            upgradeRandomResidence();
        }
    }

    /**
     * Fait grandir la ville
     */
    private void growCity() {
        // Nouvelle résidence (70% de chance)
        if (random.nextDouble() < 0.7) {
            ResidenceLevel newLevel = determineResidenceLevel();
            int x = random.nextInt(1800) + 100;
            int y = random.nextInt(1300) + 100;

            Residence newRes = new Residence(newLevel, x, y);
            if (!overlapsExisting(newRes)) {
                residences.add(newRes);
                System.out.println("📍 Nouvelle résidence construite: " + newLevel);
            }
        }
        // Nouvelle infrastructure (30% de chance)
        else {
            BuildingType type = BuildingType.randomInfrastructure(random);
            int x = random.nextInt(1800) + 100;
            int y = random.nextInt(1300) + 100;

            Infrastructure newInfra = new Infrastructure(type, x, y);
            if (!overlapsExisting(newInfra)) {
                infrastructures.add(newInfra);
                System.out.println("🏗️ Nouvelle infrastructure: " + type.getDisplayName());
            }
        }
    }

    /**
     * Réduit la ville (départ habitants)
     */
    private void shrinkCity() {
        if (residences.size() > 3 && random.nextDouble() < 0.5) {
            Residence removed = residences.remove(residences.size() - 1);
            System.out.println("📉 Une résidence a été abandonnée");
        }
    }

    /**
     * Améliore une résidence aléatoire
     */
    private void upgradeRandomResidence() {
        List<Residence> upgradeable = residences.stream()
                .filter(Residence::canUpgrade)
                .collect(Collectors.toList());

        if (!upgradeable.isEmpty()) {
            Residence toUpgrade = upgradeable.get(random.nextInt(upgradeable.size()));
            toUpgrade.upgrade();
            System.out.println("⬆️ Résidence améliorée au niveau " + toUpgrade.getLevel());
        }
    }

    /**
     * Détermine le niveau de nouvelle résidence selon niveau ville
     */
    private ResidenceLevel determineResidenceLevel() {
        if (level >= 5 && happiness > 85) return ResidenceLevel.ADVANCED;
        if (level >= 3 && happiness > 75) return ResidenceLevel.MEDIUM;
        return ResidenceLevel.BASIC;
    }

    /**
     * Vérifie si un bâtiment chevauche d'autres
     */
    private boolean overlapsExisting(Building newBuilding) {
        for (Residence res : residences) {
            if (newBuilding.overlaps(res)) return true;
        }
        for (PowerPlant plant : powerPlants) {
            if (newBuilding.overlaps(plant)) return true;
        }
        for (Infrastructure infra : infrastructures) {
            if (newBuilding.overlaps(infra)) return true;
        }
        return false;
    }

    /**
     * Met à jour le niveau de la ville
     */
    private void updateCityLevel() {
        int newLevel = 1;

        if (population >= 5000) newLevel = 10;
        else if (population >= 3000) newLevel = 8;
        else if (population >= 2000) newLevel = 7;
        else if (population >= 1500) newLevel = 6;
        else if (population >= 1000) newLevel = 5;
        else if (population >= 700) newLevel = 4;
        else if (population >= 500) newLevel = 3;
        else if (population >= 250) newLevel = 2;

        if (newLevel > level) {
            level = newLevel;
            System.out.println("🎉 La ville atteint le niveau " + level + "!");
        }
    }

    /**
     * Sauvegarde l'historique
     */
    private void saveHistory() {
        moneyHistory.add(money);
        happinessHistory.add(happiness);
        energyHistory.add(energyBalance);

        // Garde seulement 90 jours d'historique
        if (moneyHistory.size() > 90) {
            moneyHistory.remove(0);
            happinessHistory.remove(0);
            energyHistory.remove(0);
        }
    }

    /**
     * Vérifie si le jeu est terminé (game over)
     */
    public boolean isGameOver() {
        return happiness <= 5 || money < -50000 ||
                (consecutiveUnhappyHours > 168); // 1 semaine
    }

    /**
     * Retourne le message de game over
     */
    public String getGameOverReason() {
        if (happiness <= 5) return "Tous les habitants ont quitté la ville...";
        if (money < -50000) return "La ville est en faillite!";
        if (consecutiveUnhappyHours > 168) return "Le maire vous retire la gestion!";
        return "";
    }

    /**
     * Vérifie si on peut se permettre un coût
     */
    public boolean canAfford(double cost) {
        return money >= cost;
    }

    /**
     * Dépense de l'argent
     */
    public void spendMoney(double amount) {
        money -= amount;
    }

    /**
     * Ajoute un bâtiment à la ville
     */
    public boolean addBuilding(Building building) {
        if (overlapsExisting(building)) {
            return false;
        }

        if (building instanceof Residence) {
            residences.add((Residence) building);
        } else if (building instanceof PowerPlant) {
            powerPlants.add((PowerPlant) building);
        } else if (building instanceof Infrastructure) {
            infrastructures.add((Infrastructure) building);
        }
        return true;
    }

    /**
     * Supprime un bâtiment
     */
    public boolean removeBuilding(Building building) {
        boolean removed = residences.remove(building) ||
                powerPlants.remove(building) ||
                infrastructures.remove(building);

        if (removed) {
            // Remboursement partiel (50%)
            money += building.getConstructionCost() * 0.5;
        }
        return removed;
    }

    /**
     * Retourne un résumé de l'état de la ville
     */
    public String getSummary() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return String.format(
                "=== %s - Niveau %d ===\n" +
                        "Date: %s\n" +
                        "Population: %d habitants\n" +
                        "Budget: %.0f €\n" +
                        "Bonheur: %.1f%%\n" +
                        "Énergie: %.0f / %.0f kWh (%.0f%%)\n" +
                        "Résidences: %d | Centrales: %d | Infrastructures: %d",
                name, level, currentTime.format(formatter),
                population, money, happiness,
                totalEnergyProduction, totalEnergyDemand,
                (totalEnergyDemand > 0 ? totalEnergyProduction/totalEnergyDemand*100 : 100),
                residences.size(), powerPlants.size(), infrastructures.size()
        );
    }

    // === GETTERS ===
    public String getName() { return name; }
    public int getLevel() { return level; }
    public LocalDateTime getCurrentTime() { return currentTime; }
    public LocalDateTime getFoundationDate() { return foundationDate; }
    public double getMoney() { return money; }
    public double getHappiness() { return happiness; }
    public int getPopulation() { return population; }
    public double getTotalEnergyProduction() { return totalEnergyProduction; }
    public double getTotalEnergyDemand() { return totalEnergyDemand; }
    public double getEnergyBalance() { return energyBalance; }
    public double getTotalPollution() { return totalPollution; }
    public double getTotalRevenue() { return totalRevenue; }
    public double getTotalExpenses() { return totalExpenses; }
    public List<Residence> getResidences() { return residences; }
    public List<PowerPlant> getPowerPlants() { return powerPlants; }
    public List<Infrastructure> getInfrastructures() { return infrastructures; }
    public List<Double> getMoneyHistory() { return moneyHistory; }
    public List<Double> getHappinessHistory() { return happinessHistory; }
    public List<Double> getEnergyHistory() { return energyHistory; }
}
