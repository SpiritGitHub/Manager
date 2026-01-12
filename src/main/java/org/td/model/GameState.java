package org.td.model;

import org.td.model.entities.*;
import org.td.model.simulation.*;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente l'état global du jeu
 * Centralise la ville et tous les gestionnaires de simulation
 */
public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    // Composants principaux
    private City city;
    private TimeManager timeManager;
    private EnergySimulator energySimulator;
    private EconomyManager economyManager;
    private PopulationManager populationManager;

    // État du jeu
    private GameStatus status;
    private String playerName;
    private int difficulty; // 1=Facile, 2=Normal, 3=Difficile

    // Statistiques de jeu
    private LocalDateTime gameStartTime;
    private long totalGameTimeMinutes; // Temps réel passé à jouer
    private int achievementsUnlocked;
    private List<Achievement> achievements;

    // Objectifs actuels
    private List<Objective> currentObjectives;
    private List<Objective> completedObjectives;

    /**
     * Constructeur
     */
    public GameState(String cityName, String playerName, int difficulty) {
        this.city = new City(cityName);
        this.playerName = playerName;
        this.difficulty = difficulty;
        this.status = GameStatus.PLAYING;
        this.gameStartTime = LocalDateTime.now();
        this.totalGameTimeMinutes = 0;
        this.achievementsUnlocked = 0;

        // Initialisation des gestionnaires
        this.timeManager = new TimeManager(city);
        this.energySimulator = new EnergySimulator(city);
        this.economyManager = new EconomyManager(city);
        this.populationManager = new PopulationManager(city);

        // Objectifs et succès
        this.achievements = new ArrayList<>();
        this.currentObjectives = new ArrayList<>();
        this.completedObjectives = new ArrayList<>();

        initializeAchievements();
        initializeObjectives();
        applyDifficulty();
    }

    /**
     * Initialise les succès (achievements)
     */
    private void initializeAchievements() {
        achievements.add(new Achievement(
                "Premier Pas",
                "Construire votre première centrale",
                false
        ));
        achievements.add(new Achievement(
                "Métropole",
                "Atteindre 1000 habitants",
                false
        ));
        achievements.add(new Achievement(
                "Millionnaire",
                "Avoir 100 000€ en caisse",
                false
        ));
        achievements.add(new Achievement(
                "Ville Verte",
                "100% d'énergie renouvelable",
                false
        ));
        achievements.add(new Achievement(
                "Ville Heureuse",
                "Maintenir 90% de bonheur pendant 30 jours",
                false
        ));
        achievements.add(new Achievement(
                "Survivant",
                "Survivre 1 an de jeu",
                false
        ));
        achievements.add(new Achievement(
                "Expert Énergétique",
                "Avoir 10 centrales actives",
                false
        ));
        achievements.add(new Achievement(
                "Mégapole",
                "Atteindre 5000 habitants",
                false
        ));
    }

    /**
     * Initialise les objectifs
     */
    private void initializeObjectives() {
        currentObjectives.add(new Objective(
                "Objectif de départ",
                "Atteindre 500 habitants",
                ObjectiveType.POPULATION,
                500
        ));
        currentObjectives.add(new Objective(
                "Stabilité énergétique",
                "Maintenir 100% de couverture pendant 7 jours",
                ObjectiveType.ENERGY,
                168 // 7 jours * 24 heures
        ));
    }

    /**
     * Applique la difficulté choisie
     */
    private void applyDifficulty() {
        switch(difficulty) {
            case 1: // Facile
                city.spendMoney(-20000); // +20k bonus
                break;
            case 3: // Difficile
                city.spendMoney(15000); // -15k
                break;
            // 2 (Normal) : pas de changement
        }
    }

    /**
     * Met à jour l'état du jeu (appelé chaque frame/tick)
     */
    public void update() {
        if (status != GameStatus.PLAYING) return;

        // Mise à jour des gestionnaires
        energySimulator.update();
        economyManager.update();
        populationManager.update();

        // Vérification objectifs et succès
        checkObjectives();
        checkAchievements();

        // Vérification game over
        if (city.isGameOver()) {
            status = GameStatus.GAME_OVER;
        }
    }

    /**
     * Vérifie les objectifs
     */
    private void checkObjectives() {
        for (Objective objective : new ArrayList<>(currentObjectives)) {
            if (objective.isCompleted()) continue;

            boolean completed = switch(objective.getType()) {
                case POPULATION -> city.getPopulation() >= objective.getTarget();
                case ENERGY -> energySimulator.getCoverageRate() >= 100;
                case MONEY -> city.getMoney() >= objective.getTarget();
                case HAPPINESS -> city.getHappiness() >= objective.getTarget();
                case BUILDINGS -> (city.getPowerPlants().size() +
                        city.getInfrastructures().size()) >= objective.getTarget();
            };

            if (completed) {
                objective.setProgress(objective.getTarget());
                objective.setCompleted(true);
                completedObjectives.add(objective);
                currentObjectives.remove(objective);
                System.out.println("🎯 Objectif complété: " + objective.getName());

                // Récompense
                city.spendMoney(-5000); // 5000€ bonus
            }
        }
    }

    /**
     * Vérifie les succès
     */
    private void checkAchievements() {
        for (Achievement achievement : achievements) {
            if (achievement.isUnlocked()) continue;

            boolean unlocked = switch(achievement.getName()) {
                case "Premier Pas" -> city.getPowerPlants().size() > 0;
                case "Métropole" -> city.getPopulation() >= 1000;
                case "Millionnaire" -> city.getMoney() >= 100000;
                case "Ville Verte" -> isFullyRenewable();
                case "Ville Heureuse" -> city.getHappiness() > 90;
                case "Survivant" -> getGameYears() >= 1;
                case "Expert Énergétique" -> city.getPowerPlants().size() >= 10;
                case "Mégapole" -> city.getPopulation() >= 5000;
                default -> false;
            };

            if (unlocked) {
                achievement.unlock();
                achievementsUnlocked++;
                System.out.println("🏆 Succès débloqué: " + achievement.getName());
            }
        }
    }

    /**
     * Vérifie si l'énergie est 100% renouvelable
     */
    private boolean isFullyRenewable() {
        return city.getPowerPlants().stream()
                .allMatch(plant ->
                        plant instanceof SolarPlant ||
                                plant instanceof WindTurbine ||
                                plant.getType().contains("Hydro") ||
                                plant.getType().contains("Geothermal"));
    }

    /**
     * Démarre le jeu
     */
    public void start() {
        timeManager.start();
        status = GameStatus.PLAYING;
    }

    /**
     * Met en pause
     */
    public void pause() {
        timeManager.pause();
        status = GameStatus.PAUSED;
    }

    /**
     * Reprend
     */
    public void resume() {
        timeManager.resume();
        status = GameStatus.PLAYING;
    }

    /**
     * Sauvegarde le jeu
     */
    public boolean save(String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(filename))) {
            oos.writeObject(this);
            System.out.println("💾 Jeu sauvegardé: " + filename);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Erreur sauvegarde: " + e.getMessage());
            return false;
        }
    }

    /**
     * Charge une sauvegarde
     */
    public static GameState load(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(filename))) {
            GameState state = (GameState) ois.readObject();
            System.out.println("📂 Jeu chargé: " + filename);
            return state;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("❌ Erreur chargement: " + e.getMessage());
            return null;
        }
    }

    /**
     * Génère un rapport complet
     */
    public String generateFullReport() {
        StringBuilder report = new StringBuilder();

        report.append("╔════════════════════════════════════════╗\n");
        report.append("║       RAPPORT DE VILLE COMPLET         ║\n");
        report.append("╚════════════════════════════════════════╝\n\n");

        report.append(city.getSummary()).append("\n\n");
        report.append(energySimulator.generateReport()).append("\n");
        report.append(populationManager.generateReport()).append("\n");

        report.append("\n=== STATISTIQUES DE JEU ===\n");
        report.append(String.format("Temps joué: %d heures\n", totalGameTimeMinutes / 60));
        report.append(String.format("Succès: %d/%d\n", achievementsUnlocked, achievements.size()));
        report.append(String.format("Objectifs complétés: %d\n", completedObjectives.size()));

        return report.toString();
    }

    /**
     * Calcule le score final
     */
    public int calculateScore() {
        int score = 0;

        // Population (max 3000 points)
        score += Math.min(3000, city.getPopulation());

        // Argent (max 2000 points)
        score += Math.min(2000, (int)(city.getMoney() / 100));

        // Bonheur (max 1500 points)
        score += (int)(city.getHappiness() * 15);

        // Succès (500 points chacun)
        score += achievementsUnlocked * 500;

        // Objectifs (300 points chacun)
        score += completedObjectives.size() * 300;

        // Longévité (100 points par année)
        score += getGameYears() * 100;

        // Bonus difficulté
        score *= difficulty;

        return score;
    }

    /**
     * Retourne le nombre d'années de jeu
     */
    private int getGameYears() {
        Duration duration = Duration.between(
                city.getFoundationDate(),
                city.getCurrentTime()
        );
        return (int)(duration.toDays() / 365);
    }

    // === GETTERS ===

    public City getCity() {
        return city;
    }

    public TimeManager getTimeManager() {
        return timeManager;
    }

    public EnergySimulator getEnergySimulator() {
        return energySimulator;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public PopulationManager getPopulationManager() {
        return populationManager;
    }

    public GameStatus getStatus() {
        return status;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public List<Achievement> getAchievements() {
        return new ArrayList<>(achievements);
    }

    public List<Objective> getCurrentObjectives() {
        return new ArrayList<>(currentObjectives);
    }

    public List<Objective> getCompletedObjectives() {
        return new ArrayList<>(completedObjectives);
    }
}

/**
 * Statuts possibles du jeu
 */
enum GameStatus {
    PLAYING, PAUSED, GAME_OVER, WON
}

/**
 * Représente un succès débloquable
 */
class Achievement implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String description;
    private boolean unlocked;
    private LocalDateTime unlockedDate;

    public Achievement(String name, String description, boolean unlocked) {
        this.name = name;
        this.description = description;
        this.unlocked = unlocked;
    }

    public void unlock() {
        this.unlocked = true;
        this.unlockedDate = LocalDateTime.now();
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isUnlocked() { return unlocked; }
    public LocalDateTime getUnlockedDate() { return unlockedDate; }
}

/**
 * Représente un objectif à accomplir
 */
class Objective implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String description;
    private ObjectiveType type;
    private double target;
    private double progress;
    private boolean completed;

    public Objective(String name, String description, ObjectiveType type, double target) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.target = target;
        this.progress = 0;
        this.completed = false;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public ObjectiveType getType() { return type; }
    public double getTarget() { return target; }
    public double getProgress() { return progress; }
    public boolean isCompleted() { return completed; }

    public void setProgress(double progress) { this.progress = progress; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public double getProgressPercentage() {
        return (progress / target) * 100.0;
    }
}

/**
 * Types d'objectifs
 */
enum ObjectiveType {
    POPULATION, ENERGY, MONEY, HAPPINESS, BUILDINGS
}
