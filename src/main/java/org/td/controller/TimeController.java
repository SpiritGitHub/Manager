package org.td.controller;

import org.td.model.enums.GameSpeed;
import org.td.model.simulation.TimeManager;
import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;

/**
 * Contrôleur pour la gestion du temps de jeu
 * Pause, vitesse, sauts temporels
 */
public class TimeController {
    private GameController gameController;
    private TimeManager timeManager;

    // Properties
    private BooleanProperty isPausedProperty;
    private ObjectProperty<GameSpeed> currentSpeedProperty;
    private StringProperty timeDisplayProperty;
    private StringProperty dateDisplayProperty;
    private DoubleProperty gameProgressProperty;

    // Formatters
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private static final DateTimeFormatter FULL_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Constructeur
     */
    public TimeController(GameController gameController) {
        this.gameController = gameController;
        this.timeManager = gameController.getGameState().getTimeManager();

        // Initialisation properties
        this.isPausedProperty = new SimpleBooleanProperty(false);
        this.currentSpeedProperty = new SimpleObjectProperty<>(GameSpeed.NORMAL);
        this.timeDisplayProperty = new SimpleStringProperty("00:00");
        this.dateDisplayProperty = new SimpleStringProperty("01 Janvier 2025");
        this.gameProgressProperty = new SimpleDoubleProperty(0.0);

        // Mise à jour initiale
        updateTimeDisplay();
    }

    /**
     * Met en pause / reprend le jeu
     */
    public void togglePause() {
        timeManager.togglePause();
        isPausedProperty.set(timeManager.isPaused());
    }

    /**
     * Met en pause
     */
    public void pause() {
        timeManager.pause();
        isPausedProperty.set(true);
    }

    /**
     * Reprend le jeu
     */
    public void resume() {
        timeManager.resume();
        isPausedProperty.set(false);
    }

    /**
     * Change la vitesse de jeu
     */
    public void setSpeed(GameSpeed speed) {
        timeManager.setSpeed(speed);
        currentSpeedProperty.set(speed);
    }

    /**
     * Augmente la vitesse
     */
    public void increaseSpeed() {
        GameSpeed current = currentSpeedProperty.get();
        GameSpeed next = switch(current) {
            case SLOW -> GameSpeed.NORMAL;
            case NORMAL -> GameSpeed.FAST;
            case FAST -> GameSpeed.ULTRA_FAST;
            case ULTRA_FAST -> GameSpeed.ULTRA_FAST; // Déjà max
        };
        setSpeed(next);
    }

    /**
     * Diminue la vitesse
     */
    public void decreaseSpeed() {
        GameSpeed current = currentSpeedProperty.get();
        GameSpeed previous = switch(current) {
            case SLOW -> GameSpeed.SLOW; // Déjà min
            case NORMAL -> GameSpeed.SLOW;
            case FAST -> GameSpeed.NORMAL;
            case ULTRA_FAST -> GameSpeed.FAST;
        };
        setSpeed(previous);
    }

    /**
     * Saute des heures (mode debug/cheat)
     */
    public void skipHours(int hours) {
        boolean wasPaused = timeManager.isPaused();

        if (!wasPaused) {
            timeManager.pause();
        }

        timeManager.skipHours(hours);
        updateTimeDisplay();

        if (!wasPaused) {
            timeManager.resume();
        }
    }

    /**
     * Saute au prochain jour (minuit)
     */
    public void skipToNextDay() {
        LocalDateTime current = gameController.getCity().getCurrentTime();
        int hoursUntilMidnight = 24 - current.getHour();
        skipHours(hoursUntilMidnight);
    }

    /**
     * Saute au prochain mois
     */
    public void skipToNextMonth() {
        LocalDateTime current = gameController.getCity().getCurrentTime();
        LocalDateTime nextMonth = current.plusMonths(1).withDayOfMonth(1).withHour(0);
        long hoursToSkip = Duration.between(current, nextMonth).toHours();
        skipHours((int)hoursToSkip);
    }

    /**
     * Met à jour l'affichage du temps
     */
    public void updateTimeDisplay() {
        LocalDateTime currentTime = gameController.getCity().getCurrentTime();

        timeDisplayProperty.set(currentTime.format(TIME_FORMATTER));
        dateDisplayProperty.set(currentTime.format(DATE_FORMATTER));

        // Calcul progression (pour barre de progression jour/mois)
        double hourProgress = currentTime.getHour() / 24.0;
        gameProgressProperty.set(hourProgress);
    }

    /**
     * Obtient le temps de jeu écoulé
     */
    public String getGameTimeElapsed() {
        LocalDateTime start = gameController.getCity().getFoundationDate();
        LocalDateTime current = gameController.getCity().getCurrentTime();

        Duration elapsed = Duration.between(start, current);

        long days = elapsed.toDays();
        long hours = elapsed.toHours() % 24;

        if (days > 0) {
            return String.format("%d jours, %d heures", days, hours);
        } else {
            return String.format("%d heures", hours);
        }
    }

    /**
     * Obtient des infos sur le temps
     */
    public TimeInfo getTimeInfo() {
        LocalDateTime current = gameController.getCity().getCurrentTime();
        Duration elapsed = timeManager.getRealTimeElapsed();

        return new TimeInfo(
                current.format(FULL_FORMATTER),
                getGameTimeElapsed(),
                formatDuration(elapsed),
                getCurrentPeriodOfDay(),
                getCurrentSeason()
        );
    }

    /**
     * Détermine la période de la journée
     */
    private String getCurrentPeriodOfDay() {
        int hour = gameController.getCity().getCurrentTime().getHour();

        if (hour >= 5 && hour < 12) return "Matin";
        if (hour >= 12 && hour < 18) return "Après-midi";
        if (hour >= 18 && hour < 22) return "Soirée";
        return "Nuit";
    }

    /**
     * Détermine la saison
     */
    private String getCurrentSeason() {
        int month = gameController.getCity().getCurrentTime().getMonthValue();

        if (month >= 3 && month <= 5) return "Printemps";
        if (month >= 6 && month <= 8) return "Été";
        if (month >= 9 && month <= 11) return "Automne";
        return "Hiver";
    }

    /**
     * Formate une durée en texte lisible
     */
    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;

        if (hours > 0) {
            return String.format("%dh %dmin", hours, minutes);
        } else {
            return String.format("%d min", minutes);
        }
    }

    /**
     * Calcule l'estimation pour atteindre une date
     */
    public String estimateTimeToReach(LocalDateTime targetDate) {
        long msToGo = timeManager.getEstimatedRealTimeToReach(targetDate);
        long minutesToGo = msToGo / 60000;

        if (minutesToGo < 60) {
            return minutesToGo + " minutes";
        } else {
            long hours = minutesToGo / 60;
            long mins = minutesToGo % 60;
            return String.format("%dh %dmin", hours, mins);
        }
    }

    /**
     * Obtient des statistiques temporelles
     */
    public TimeStats getTimeStats() {
        LocalDateTime current = gameController.getCity().getCurrentTime();
        Duration gameTime = Duration.between(
                gameController.getCity().getFoundationDate(),
                current
        );

        GameSpeed speed = currentSpeedProperty.get();

        return new TimeStats(
                gameTime.toDays(),
                gameTime.toHours(),
                speed.getGameHoursPerRealHour(),
                speed.getGameDaysPerRealHour(),
                isPausedProperty.get()
        );
    }

    // === GETTERS & PROPERTIES ===

    public boolean isPaused() {
        return timeManager.isPaused();
    }

    public GameSpeed getCurrentSpeed() {
        return currentSpeedProperty.get();
    }

    public BooleanProperty isPausedProperty() {
        return isPausedProperty;
    }

    public ObjectProperty<GameSpeed> currentSpeedProperty() {
        return currentSpeedProperty;
    }

    public StringProperty timeDisplayProperty() {
        return timeDisplayProperty;
    }

    public StringProperty dateDisplayProperty() {
        return dateDisplayProperty;
    }

    public DoubleProperty gameProgressProperty() {
        return gameProgressProperty;
    }
}

/**
 * Informations sur le temps
 */
class TimeInfo {
    public final String fullDateTime;
    public final String gameTimeElapsed;
    public final String realTimeElapsed;
    public final String periodOfDay;
    public final String season;

    public TimeInfo(String fullDateTime, String gameTimeElapsed,
                    String realTimeElapsed, String periodOfDay, String season) {
        this.fullDateTime = fullDateTime;
        this.gameTimeElapsed = gameTimeElapsed;
        this.realTimeElapsed = realTimeElapsed;
        this.periodOfDay = periodOfDay;
        this.season = season;
    }
}

/**
 * Statistiques temporelles
 */
class TimeStats {
    public final long gameDays;
    public final long gameHours;
    public final double gameHoursPerRealHour;
    public final double gameDaysPerRealHour;
    public final boolean isPaused;

    public TimeStats(long gameDays, long gameHours,
                     double gameHoursPerRealHour, double gameDaysPerRealHour,
                     boolean isPaused) {
        this.gameDays = gameDays;
        this.gameHours = gameHours;
        this.gameHoursPerRealHour = gameHoursPerRealHour;
        this.gameDaysPerRealHour = gameDaysPerRealHour;
        this.isPaused = isPaused;
    }

    @Override
    public String toString() {
        return String.format(
                "Temps de jeu: %d jours (%d heures)\n" +
                        "Vitesse: %.1fx (%.1f jours/h réelle)\n" +
                        "État: %s",
                gameDays, gameHours,
                gameHoursPerRealHour / 24.0,
                gameDaysPerRealHour,
                isPaused ? "En pause" : "En cours"
        );
    }
}