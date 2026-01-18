package org.td.controller;

import javafx.beans.property.*;
import org.td.model.GameState;
import org.td.model.entities.*;
import org.td.model.enums.*;
import org.td.model.simulation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import javafx.application.Platform;

/**
 * Contrôleur principal du jeu
 * Coordonne tous les autres contrôleurs et gère l'état global
 */
public class GameController {
    private GameState gameState;
    private BuildingController buildingController;
    private TimeController timeController;

    // Properties pour binding avec l'UI (JavaFX)
    private DoubleProperty moneyProperty;
    private DoubleProperty happinessProperty;
    private IntegerProperty populationProperty;
    private DoubleProperty energyProductionProperty;
    private DoubleProperty energyDemandProperty;
    private StringProperty timeProperty;
    private IntegerProperty cityLevelProperty;
    private DoubleProperty pollutionProperty;

    // Properties énergétiques
    private DoubleProperty gridStabilityProperty;
    private DoubleProperty coverageRateProperty;

    // Properties économiques
    private DoubleProperty revenueProperty;
    private DoubleProperty expensesProperty;

    // Listeners pour notifications
    private List<GameEventListener> eventListeners;

    /**
     * Constructeur pour nouvelle partie
     */
    public GameController(String cityName, String playerName, int difficulty) {
        this.gameState = new GameState(cityName, playerName, difficulty);
        initialize();
    }

    /**
     * Constructeur pour charger une partie
     */
    public GameController(GameState loadedState) {
        this.gameState = loadedState;
        initialize();
    }

    /**
     * Initialise le contrôleur
     */
    private void initialize() {
        // Sous-contrôleurs
        this.buildingController = new BuildingController(this);
        this.timeController = new TimeController(this);

        // Properties
        this.moneyProperty = new SimpleDoubleProperty(gameState.getCity().getMoney());
        this.happinessProperty = new SimpleDoubleProperty(gameState.getCity().getHappiness());
        this.populationProperty = new SimpleIntegerProperty(gameState.getCity().getPopulation());
        this.energyProductionProperty = new SimpleDoubleProperty(0);
        this.energyDemandProperty = new SimpleDoubleProperty(0);
        this.timeProperty = new SimpleStringProperty("");
        this.cityLevelProperty = new SimpleIntegerProperty(gameState.getCity().getLevel());
        this.pollutionProperty = new SimpleDoubleProperty(0);
        this.gridStabilityProperty = new SimpleDoubleProperty(100);
        this.coverageRateProperty = new SimpleDoubleProperty(100);
        this.revenueProperty = new SimpleDoubleProperty(0);
        this.expensesProperty = new SimpleDoubleProperty(0);

        // Listeners
        this.eventListeners = new ArrayList<>();

        // Écouter les événements du TimeManager
        setupTimeListeners();
    }

    /**
     * Configure les listeners temporels
     */
    private void setupTimeListeners() {
        gameState.getTimeManager().addListener(new TimeListener() {
            @Override
            public void onTimeAdvanced(LocalDateTime currentTime) {
                javafx.application.Platform.runLater(() -> {
                    updateProperties();
                    checkGameEvents();
                });
            }

            @Override
            public void onNewDay(java.time.LocalDateTime currentTime) {
                Platform.runLater(() -> notifyEvent("Nouveau jour: " + currentTime.toLocalDate()));
            }

            @Override
            public void onNewMonth(java.time.LocalDateTime currentTime) {
                Platform.runLater(() -> {
                    notifyEvent("Nouveau mois: " + currentTime.getMonth());
                    generateMonthlyReport();
                });
            }

            @Override
            public void onGameOver(String reason) {
                Platform.runLater(() -> handleGameOver(reason));
            }
        });
    }

    /**
     * Démarre le jeu
     */
    public void startGame() {
        gameState.start();
        updateProperties();
        notifyEvent("Bienvenue dans " + gameState.getCity().getName() + "!");
    }

    /**
     * Met à jour toutes les properties (appelé chaque tick)
     */
    private void updateProperties() {
        City city = gameState.getCity();

        // Données ville
        moneyProperty.set(city.getMoney());
        happinessProperty.set(city.getHappiness());
        populationProperty.set(city.getPopulation());
        cityLevelProperty.set(city.getLevel());
        pollutionProperty.set(city.getTotalPollution());

        // Énergie
        energyProductionProperty.set(city.getTotalEnergyProduction());
        energyDemandProperty.set(city.getTotalEnergyDemand());

        // Simulateurs
        gridStabilityProperty.set(gameState.getEnergySimulator().getGridStability());
        coverageRateProperty.set(gameState.getEnergySimulator().getCoverageRate());

        // Économie
        revenueProperty.set(city.getTotalRevenue());
        expensesProperty.set(city.getTotalExpenses());

        // Temps
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        timeProperty.set(city.getCurrentTime().format(formatter));
    }

    /**
     * Vérifie les événements de jeu
     */
    private void checkGameEvents() {
        City city = gameState.getCity();

        // Alerte argent faible
        if (city.getMoney() < 5000 && city.getMoney() > 0) {
            notifyWarning("⚠️ Budget faible! Restant: " + (int) city.getMoney() + "€");
        }

        // Alerte bonheur faible
        if (city.getHappiness() < 30) {
            notifyWarning("😞 Satisfaction très basse! (" + (int) city.getHappiness() + "%)");
        }

        // Alerte pénurie énergétique
        if (gameState.getEnergySimulator().getCoverageRate() < 70) {
            notifyWarning("⚡ Pénurie d'électricité!");
        }

        // Alerte centrales à maintenir
        long needMaintenance = gameState.getEnergySimulator().getPlantsNeedingMaintenance();
        if (needMaintenance > 0) {
            notifyWarning("🔧 " + needMaintenance + " centrale(s) nécessitent maintenance");
        }
    }

    /**
     * Génère un rapport mensuel
     */
    private void generateMonthlyReport() {
        StringBuilder report = new StringBuilder();
        report.append("\n📊 RAPPORT MENSUEL\n");
        report.append("═══════════════════\n");
        report.append(String.format("Population: %d habitants\n",
                gameState.getCity().getPopulation()));
        report.append(String.format("Budget: %.0f €\n",
                gameState.getCity().getMoney()));
        report.append(String.format("Bonheur: %.0f%%\n",
                gameState.getCity().getHappiness()));
        report.append(String.format("Revenus du mois: %.0f €\n",
                gameState.getEconomyManager().getMonthlyRevenue()));
        report.append(String.format("Dépenses du mois: %.0f €\n",
                gameState.getEconomyManager().getMonthlyExpenses()));
        report.append(String.format("Bilan: %.0f €\n",
                gameState.getEconomyManager().getMonthlyNetIncome()));

        notifyEvent(report.toString());
    }

    // Interface fonctionnelle pour gérer le Game Over depuis la vue
    public interface GameOverHandler {
        void onGameOver(String reason, int score);
    }

    private GameOverHandler gameOverHandler;

    public void setGameOverHandler(GameOverHandler handler) {
        this.gameOverHandler = handler;
    }

    /**
     * Gère le game over
     */
    private void handleGameOver(String reason) {
        notifyEvent("🎮 GAME OVER: " + reason);
        int finalScore = gameState.calculateScore();
        notifyEvent("Score final: " + finalScore);

        if (gameOverHandler != null) {
            gameOverHandler.onGameOver(reason, finalScore);
        } else {
            // Fallback console si pas de vue attachée
            System.err.println("GAME OVER: " + reason);
            System.exit(0);
        }
    }

    /**
     * Redémarre le jeu
     */
    public void restartGame() {
        // Stop current game
        if (gameState != null && gameState.getTimeManager() != null) {
            gameState.getTimeManager().stop();
        }

        // Create new state
        this.gameState = new GameState("ÉnergiVille", "Joueur", 2);
        initialize(); // Re-bind properties and controllers

        // Restart logic
        startGame();
        notifyEvent("🔄 Nouvelle partie commencée !");
    }

    /**
     * Sauvegarde la partie
     */
    public boolean saveGame(String filename) {
        boolean success = gameState.save(filename);
        if (success) {
            notifyEvent("💾 Partie sauvegardée: " + filename);
        } else {
            notifyWarning("❌ Échec de la sauvegarde");
        }
        return success;
    }

    /**
     * Obtient les recommandations globales
     */
    public List<String> getAllRecommendations() {
        List<String> allRecommendations = new ArrayList<>();

        // Recommandations énergétiques
        allRecommendations.addAll(gameState.getEnergySimulator().getRecommendations());

        // Recommandations économiques
        allRecommendations.addAll(gameState.getEconomyManager().getFinancialRecommendations());

        // Recommandations démographiques
        allRecommendations.addAll(gameState.getPopulationManager().getRecommendations());

        return allRecommendations;
    }

    /**
     * Obtient un résumé de l'état du jeu
     */
    public GameSummary getGameSummary() {
        return new GameSummary(
                gameState.getCity().getPopulation(),
                gameState.getCity().getMoney(),
                gameState.getCity().getHappiness(),
                gameState.getEnergySimulator().getCoverageRate(),
                gameState.getEconomyManager().getFinancialHealth(),
                gameState.getPopulationManager().getQualityOfLifeScore());
    }

    /**
     * Obtient les statistiques détaillées
     */
    public String getDetailedStats() {
        return gameState.generateFullReport();
    }

    /**
     * Demande un prêt d'urgence
     */
    public boolean requestEmergencyLoan() {
        boolean granted = gameState.getEconomyManager().grantEmergencyLoan();
        if (granted) {
            updateProperties();
            notifyEvent("🏦 Prêt d'urgence accordé: 20,000€");
        } else {
            notifyWarning("❌ Prêt refusé (dette trop élevée)");
        }
        return granted;
    }

    /**
     * Ajuste le prix de l'électricité
     */
    public void adjustElectricityPrice(double newPrice) {
        gameState.getEconomyManager().adjustElectricityPrice(newPrice);
        notifyEvent("💡 Prix électricité ajusté: " +
                String.format("%.2f €/kWh", newPrice));
    }

    /**
     * Effectue maintenance sur toutes les centrales
     */
    public void performGlobalMaintenance() {
        double totalCost = 0;
        for (PowerPlant plant : gameState.getCity().getPowerPlants()) {
            if (plant.needsMaintenance()) { // ✅ Maintenant public et retourne boolean
                totalCost += plant.performMaintenance();
            }
        }

        if (totalCost > 0) {
            gameState.getCity().spendMoney(totalCost);
            updateProperties();
            notifyEvent("🔧 Maintenance globale effectuée: " +
                    String.format("%.0f €", totalCost));
        }
    }

    // === GESTION DES ÉVÉNEMENTS ===

    public void sendNotification(String message, EventType type) {
        for (GameEventListener listener : eventListeners) {
            listener.onGameEvent(message, type);
        }
    }

    private void notifyEvent(String message) {
        sendNotification(message, EventType.INFO);
    }

    private void notifyWarning(String message) {
        sendNotification(message, EventType.WARNING);
    }

    public GameState getGameState() {
        return gameState;
    }

    public void addEventListener(GameEventListener listener) {
        eventListeners.add(listener);
    }

    public City getCity() {
        return gameState.getCity();
    }

    public BuildingController getBuildingController() {
        return buildingController;
    }

    public TimeController getTimeController() {
        return timeController;
    }

    public EnergySimulator getEnergySimulator() {
        return gameState.getEnergySimulator();
    }

    public EconomyManager getEconomyManager() {
        return gameState.getEconomyManager();
    }

    public PopulationManager getPopulationManager() {
        return gameState.getPopulationManager();
    }

    // === PROPERTIES POUR BINDING ===

    public DoubleProperty moneyProperty() {
        return moneyProperty;
    }

    public DoubleProperty happinessProperty() {
        return happinessProperty;
    }

    public IntegerProperty populationProperty() {
        return populationProperty;
    }

    public DoubleProperty energyProductionProperty() {
        return energyProductionProperty;
    }

    public DoubleProperty energyDemandProperty() {
        return energyDemandProperty;
    }

    public StringProperty timeProperty() {
        return timeProperty;
    }

    public IntegerProperty cityLevelProperty() {
        return cityLevelProperty;
    }

    public DoubleProperty pollutionProperty() {
        return pollutionProperty;
    }

    public DoubleProperty gridStabilityProperty() {
        return gridStabilityProperty;
    }

    public DoubleProperty coverageRateProperty() {
        return coverageRateProperty;
    }

    public DoubleProperty revenueProperty() {
        return revenueProperty;
    }

    public DoubleProperty expensesProperty() {
        return expensesProperty;
    }
}

/**
 * Types d'événements
 */

/**
 * Résumé de l'état du jeu
 */
class GameSummary {
    public final int population;
    public final double money;
    public final double happiness;
    public final double energyCoverage;
    public final FinancialHealth financialHealth; // ✅ Import ajouté en haut
    public final double qualityOfLife;

    public GameSummary(int population, double money, double happiness,
            double energyCoverage, FinancialHealth financialHealth,
            double qualityOfLife) {
        this.population = population;
        this.money = money;
        this.happiness = happiness;
        this.energyCoverage = energyCoverage;
        this.financialHealth = financialHealth;
        this.qualityOfLife = qualityOfLife;
    }
}