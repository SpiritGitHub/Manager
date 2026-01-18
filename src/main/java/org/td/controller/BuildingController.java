package org.td.controller;

import org.td.model.entities.*;
import org.td.model.enums.*;
import javafx.beans.property.*;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Random;
import java.util.Collections;

/**
 * Contrôleur pour la gestion des bâtiments
 * Construction, amélioration, suppression, maintenance
 */
public class BuildingController {
    private GameController gameController;
    private City city;

    // Mode de construction actuel
    private BuildingMode buildMode;
    private PowerPlantType selectedPlantType;
    private BuildingType selectedInfraType;

    // Properties
    private ObjectProperty<BuildingMode> buildModeProperty;
    private StringProperty buildInfoProperty;

    /**
     * Constructeur
     */
    public BuildingController(GameController gameController) {
        this.gameController = gameController;
        this.city = gameController.getCity();
        this.buildMode = BuildingMode.NONE;

        this.buildModeProperty = new SimpleObjectProperty<>(BuildingMode.NONE);
        this.buildInfoProperty = new SimpleStringProperty("");
    }

    /**
     * Active le mode construction de centrale
     */
    public void setBuildPowerPlantMode(PowerPlantType type) {
        this.buildMode = BuildingMode.POWER_PLANT;
        this.selectedPlantType = type;
        this.buildModeProperty.set(BuildingMode.POWER_PLANT);

        double cost = calculatePowerPlantCost(type, 1);
        buildInfoProperty.set(String.format(
                "Construction: %s (Coût: %.0f €)",
                type.getDisplayName(), cost));
    }

    /**
     * Active le mode construction d'infrastructure
     */
    public void setBuildInfrastructureMode(BuildingType type) {
        this.buildMode = BuildingMode.INFRASTRUCTURE;
        this.selectedInfraType = type;
        this.buildModeProperty.set(BuildingMode.INFRASTRUCTURE);

        double cost = type.getConstructionCost();
        buildInfoProperty.set(String.format(
                "Construction: %s (Coût: %.0f €)",
                type.getDisplayName(), cost));
    }

    /**
     * Active le mode construction aléatoire
     */
    public void setRandomBuildMode() {
        this.buildMode = BuildingMode.RANDOM;
        this.buildModeProperty.set(BuildingMode.RANDOM);
        this.buildInfoProperty.set("Construction: Aléatoire (Surprise !)");
    }

    /**
     * Désactive le mode construction
     */
    public void cancelBuildMode() {
        this.buildMode = BuildingMode.NONE;
        this.buildModeProperty.set(BuildingMode.NONE);
        this.buildInfoProperty.set("");
    }

    /**
     * Construit une centrale électrique
     */
    public BuildResult buildPowerPlant(PowerPlantType type, int x, int y) {
        // Vérifier niveau ville requis
        if (!type.isUnlockedAt(city.getLevel())) {
            return new BuildResult(false,
                    "Niveau ville insuffisant (requis: " + type.getMinimumCityLevel() + ")");
        }

        // Vérifier coût
        double cost = calculatePowerPlantCost(type, 1);
        if (!city.canAfford(cost)) {
            return new BuildResult(false,
                    String.format("Budget insuffisant (requis: %.0f €)", cost));
        }

        // REFACTOR: Contrainte de construction (Niveau <= 2 : Max 1 centrale par type)
        if (city.getLevel() <= 2) {
            long existingCount = city.getPowerPlants().stream()
                    .filter(p -> p.getType().equals(type.getDisplayName()))
                    .count();

            if (existingCount >= 1) {
                return new BuildResult(false,
                        "Niveau 3 requis pour avoir plusieurs centrales du même type !");
            }
        }

        // Créer la centrale
        PowerPlant plant = createPowerPlant(type, 1, x, y);
        if (plant == null) {
            return new BuildResult(false, "Erreur lors de la création");
        }

        // Vérifier chevauchement
        if (!city.addBuilding(plant)) {
            return new BuildResult(false, "Emplacement occupé ou invalide");
        }

        // Débiter le coût
        city.spendMoney(cost);

        return new BuildResult(true,
                String.format("%s construite avec succès!", type.getDisplayName()));
    }

    /**
     * Construit une infrastructure
     */
    public BuildResult buildInfrastructure(BuildingType type, int x, int y) {
        // Vérifier niveau ville requis
        if (!type.isUnlockedAt(city.getLevel())) {
            return new BuildResult(false,
                    "Niveau ville insuffisant (requis: " + type.getMinimumCityLevel() + ")");
        }

        // Vérifier coût
        double cost = type.getConstructionCost();
        if (!city.canAfford(cost)) {
            return new BuildResult(false,
                    String.format("Budget insuffisant (requis: %.0f €)", cost));
        }

        // Créer l'infrastructure
        Infrastructure infrastructure = new Infrastructure(type, x, y);

        // Vérifier chevauchement
        if (!city.addBuilding(infrastructure)) {
            return new BuildResult(false, "Emplacement occupé ou invalide");
        }

        // Débiter le coût
        city.spendMoney(cost);

        return new BuildResult(true,
                String.format("%s construit avec succès!", type.getDisplayName()));
    }

    /**
     * Construit un bâtiment aléatoire
     */
    public BuildResult buildRandom(int x, int y) {
        // Méthode conservée pour compatibilité, mais le mode RANDOM sera retiré
        return new BuildResult(false, "Mode désactivé");
    }

    /**
     * Construit immédiatement une centrale à une position aléatoire valide
     */
    public BuildResult buildRandomly(PowerPlantType type) {
        // Vérifier niveau et coût
        if (!type.isUnlockedAt(city.getLevel())) {
            return new BuildResult(false, "Niveau insuffisant");
        }
        if (!city.canAfford(calculatePowerPlantCost(type, 1))) {
            return new BuildResult(false, "Fonds insuffisants");
        }

        // Trouver une position valide
        Random rand = new Random();
        for (int i = 0; i < 50; i++) { // 50 tentatives
            int x = rand.nextInt(org.td.utils.GameConfig.CANVAS_WIDTH - 100) + 20;
            int y = rand.nextInt(org.td.utils.GameConfig.CANVAS_HEIGHT - 100) + 20;

            // Tenter de construire
            PowerPlant plant = createPowerPlant(type, 1, x, y);
            if (city.addBuilding(plant)) {
                city.spendMoney(calculatePowerPlantCost(type, 1));
                return new BuildResult(true, type.getDisplayName() + " construit !");
            }
        }

        return new BuildResult(false, "Impossible de trouver un emplacement libre");
    }

    /**
     * Construit immédiatement une infrastructure à une position aléatoire valide
     */
    public BuildResult buildRandomly(BuildingType type) {
        // Vérifier niveau et coût
        if (!type.isUnlockedAt(city.getLevel())) {
            return new BuildResult(false, "Niveau insuffisant");
        }
        if (!city.canAfford(type.getConstructionCost())) {
            return new BuildResult(false, "Fonds insuffisants");
        }

        // Trouver une position valide
        Random rand = new Random();
        for (int i = 0; i < 50; i++) { // 50 tentatives
            int x = rand.nextInt(org.td.utils.GameConfig.CANVAS_WIDTH - 100) + 20;
            int y = rand.nextInt(org.td.utils.GameConfig.CANVAS_HEIGHT - 100) + 20;

            // Tenter de construire
            Infrastructure infra = new Infrastructure(type, x, y);
            if (city.addBuilding(infra)) {
                city.spendMoney(type.getConstructionCost());
                return new BuildResult(true, type.getDisplayName() + " construit !");
            }
        }

        return new BuildResult(false, "Impossible de trouver un emplacement libre");
    }

    /**
     * Tente de construire au clic (selon mode actuel)
     */
    public BuildResult attemptBuild(int x, int y) {
        BuildResult result = switch (buildMode) {
            case POWER_PLANT -> buildPowerPlant(selectedPlantType, x, y);
            case INFRASTRUCTURE -> buildInfrastructure(selectedInfraType, x, y);
            case RANDOM -> buildRandom(x, y);
            case NONE -> new BuildResult(false, "Aucun mode de construction actif");
        };

        // Annuler mode construction après succès
        if (result.success) {
            cancelBuildMode();
        }

        return result;
    }

    /**
     * Améliore un bâtiment
     */
    public BuildResult upgradeBuilding(Building building) {
        if (!building.canUpgrade()) {
            return new BuildResult(false, "Niveau maximum atteint");
        }

        double cost = building.getUpgradeCost();
        if (!city.canAfford(cost)) {
            return new BuildResult(false,
                    String.format("Budget insuffisant (requis: %.0f €)", cost));
        }

        city.spendMoney(cost);
        boolean success = building.upgrade();

        if (success) {
            return new BuildResult(true,
                    String.format("%s amélioré au niveau %d!",
                            building.getType(), building.getLevel()));
        } else {
            // Rembourser si échec
            city.spendMoney(-cost);
            return new BuildResult(false, "Échec de l'amélioration");
        }
    }

    /**
     * Supprime un bâtiment
     */
    public BuildResult demolishBuilding(Building building) {
        boolean removed = city.removeBuilding(building);

        if (removed) {
            return new BuildResult(true,
                    String.format("%s démoli (remboursement: %.0f €)",
                            building.getType(), building.getConstructionCost() * 0.5));
        } else {
            return new BuildResult(false, "Impossible de démolir ce bâtiment");
        }
    }

    /**
     * Active/Désactive un bâtiment
     */
    public void toggleBuilding(Building building) {
        building.toggleActive();
    }

    /**
     * Effectue la maintenance d'une centrale
     */
    public BuildResult performMaintenance(PowerPlant plant) {
        double cost = plant.performMaintenance();

        if (!city.canAfford(cost)) {
            return new BuildResult(false,
                    String.format("Budget insuffisant (requis: %.0f €)", cost));
        }

        city.spendMoney(cost);
        return new BuildResult(true,
                String.format("Maintenance effectuée (%.0f €)", cost));
    }

    /**
     * Réapprovisionne une centrale à charbon
     */
    public BuildResult refuelCoalPlant(CoalPlant plant, double tons) {
        double cost = plant.buyCoal(tons);

        if (!city.canAfford(cost)) {
            return new BuildResult(false,
                    String.format("Budget insuffisant (requis: %.0f €)", cost));
        }

        city.spendMoney(cost);
        return new BuildResult(true,
                String.format("%.0f tonnes de charbon achetées", tons));
    }

    /**
     * Réapprovisionne une centrale nucléaire
     */
    public BuildResult refuelNuclearPlant(NuclearPlant plant, double kg) {
        double cost = plant.refuel(kg);

        if (!city.canAfford(cost)) {
            return new BuildResult(false,
                    String.format("Budget insuffisant (requis: %.0f €)", cost));
        }

        city.spendMoney(cost);
        return new BuildResult(true,
                String.format("%.0f kg de combustible nucléaire achetés", kg));
    }

    /**
     * Arrêt d'urgence d'une centrale nucléaire
     */
    public void emergencyShutdownNuclear(NuclearPlant plant) {
        plant.emergencyShutdown();
    }

    /**
     * Redémarre une centrale nucléaire
     */
    public BuildResult restartNuclearPlant(NuclearPlant plant) {
        boolean success = plant.restart();

        if (success) {
            return new BuildResult(true, "Centrale nucléaire redémarrée");
        } else {
            return new BuildResult(false,
                    "Impossible de redémarrer (sécurité ou combustible insuffisant)");
        }
    }

    /**
     * Obtient tous les bâtiments
     */
    public List<Building> getAllBuildings() {
        List<Building> all = new ArrayList<>();
        all.addAll(city.getResidences());
        all.addAll(city.getPowerPlants());
        all.addAll(city.getInfrastructures());
        return all;
    }

    /**
     * Trouve le bâtiment à une position
     */
    public Building getBuildingAt(int x, int y) {
        for (Building building : getAllBuildings()) {
            int bx = building.getX();
            int by = building.getY();
            int width = building.getWidth() * 40; // CELL_SIZE = 40
            int height = building.getHeight() * 40;

            if (x >= bx && x < bx + width && y >= by && y < by + height) {
                return building;
            }
        }
        return null;
    }

    /**
     * Obtient les centrales nécessitant maintenance
     */
    public List<PowerPlant> getPlantsNeedingMaintenance() {
        return city.getPowerPlants().stream()
                .filter(PowerPlant::needsMaintenance)
                .collect(Collectors.toList());
    }

    /**
     * Obtient les types de centrales disponibles pour le niveau actuel
     */
    public List<PowerPlantType> getAvailablePowerPlantTypes() {
        int cityLevel = city.getLevel();
        List<PowerPlantType> available = new ArrayList<>();

        for (PowerPlantType type : PowerPlantType.values()) {
            if (type.isUnlockedAt(cityLevel)) {
                available.add(type);
            }
        }

        return available;
    }

    /**
     * Obtient les types d'infrastructures disponibles
     */
    public List<BuildingType> getAvailableInfrastructureTypes() {
        int cityLevel = city.getLevel();
        List<BuildingType> available = new ArrayList<>();

        for (BuildingType type : BuildingType.values()) {
            if (type.isUnlockedAt(cityLevel)) {
                available.add(type);
            }
        }

        return available;
    }

    /**
     * Calcule le coût d'une centrale
     */
    private double calculatePowerPlantCost(PowerPlantType type, int level) {
        return type.getConstructionCost(level);
    }

    /**
     * Crée une instance de centrale
     */
    private PowerPlant createPowerPlant(PowerPlantType type, int level, int x, int y) {
        return switch (type) {
            case COAL -> new CoalPlant(level, x, y);
            case SOLAR -> new SolarPlant(level, x, y);
            case WIND -> new WindTurbine(level, x, y);
            case NUCLEAR -> new NuclearPlant(level, x, y);
            // Pour HYDRO et GEOTHERMAL, on réutilise des types existants
            // (ou créer de nouvelles classes si nécessaire)
            case HYDRO -> new CoalPlant(level, x, y); // Temporaire
            case GEOTHERMAL -> new SolarPlant(level, x, y); // Temporaire
        };
    }

    /**
     * Obtient les statistiques de bâtiments
     */
    public BuildingStats getBuildingStats() {
        return new BuildingStats(
                city.getResidences().size(),
                city.getPowerPlants().size(),
                city.getInfrastructures().size(),
                (int) city.getPowerPlants().stream().filter(Building::isActive).count(),
                (int) getPlantsNeedingMaintenance().size());
    }

    // === GETTERS ===

    public BuildingMode getBuildMode() {
        return buildMode;
    }

    public PowerPlantType getSelectedPlantType() {
        return selectedPlantType;
    }

    public BuildingType getSelectedInfraType() {
        return selectedInfraType;
    }

    public ObjectProperty<BuildingMode> buildModeProperty() {
        return buildModeProperty;
    }

    public StringProperty buildInfoProperty() {
        return buildInfoProperty;
    }
}

/**
 * Modes de construction
 */

/**
 * Résultat d'une opération sur bâtiment
 */

/**
 * Statistiques des bâtiments
 */
class BuildingStats {
    public final int residences;
    public final int powerPlants;
    public final int infrastructures;
    public final int activePlants;
    public final int plantsNeedingMaintenance;

    public BuildingStats(int residences, int powerPlants, int infrastructures,
            int activePlants, int plantsNeedingMaintenance) {
        this.residences = residences;
        this.powerPlants = powerPlants;
        this.infrastructures = infrastructures;
        this.activePlants = activePlants;
        this.plantsNeedingMaintenance = plantsNeedingMaintenance;
    }
}
