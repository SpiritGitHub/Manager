package org.td.model.simulation;

import org.td.model.entities.*;
import org.td.model.enums.GameSpeed;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Gère le temps de jeu et l'avancement de la simulation
 */
public class TimeManager {
    private City city;
    private GameSpeed currentSpeed;
    private boolean isPaused;
    private boolean isRunning;

    // Vitesses de jeu en millisecondes par heure de jeu
    private static final long SLOW_MS = 2000; // 2 secondes = 1 heure
    private static final long NORMAL_MS = 1000; // 1 seconde = 1 heure
    private static final long FAST_MS = 500; // 0.5 seconde = 1 heure
    private static final long ULTRA_FAST_MS = 200; // 0.2 seconde = 1 heure

    // Listeners pour notifications
    private List<TimeListener> listeners;

    // Thread de simulation
    private Thread simulationThread;
    private long lastUpdateTime;
    private long accumulatedTime;

    // Compteur pour Game Over
    private int consecutiveZeroHappiness = 0;

    /**
     * Constructeur
     */
    public TimeManager(City city) {
        this.city = city;
        this.currentSpeed = GameSpeed.NORMAL;
        this.isPaused = false;
        this.isRunning = false;
        this.listeners = new ArrayList<>();
        this.lastUpdateTime = System.currentTimeMillis();
        this.accumulatedTime = 0;
    }

    /**
     * Démarre la simulation
     */
    public void start() {
        if (isRunning)
            return;

        isRunning = true;
        isPaused = false;

        simulationThread = new Thread(this::runSimulation);
        simulationThread.setDaemon(true);
        simulationThread.setName("TimeManager-Thread");
        simulationThread.start();

        System.out.println("⏰ Simulation démarrée");
    }

    /**
     * Arrête la simulation
     */
    public void stop() {
        isRunning = false;
        if (simulationThread != null) {
            simulationThread.interrupt();
        }
        System.out.println("⏰ Simulation arrêtée");
    }

    /**
     * Boucle principale de simulation
     */
    private void runSimulation() {
        while (isRunning && !Thread.interrupted()) {
            try {
                if (!isPaused && !city.isGameOver()) {
                    long currentTime = System.currentTimeMillis();
                    long deltaTime = currentTime - lastUpdateTime;
                    lastUpdateTime = currentTime;

                    accumulatedTime += deltaTime;

                    // Vérifier si assez de temps s'est écoulé pour avancer d'une heure
                    long msPerHour = currentSpeed.getMillisecondsPerHour();

                    if (accumulatedTime >= msPerHour) {
                        // Avancer d'une heure
                        city.advanceTime();
                        accumulatedTime -= msPerHour;

                        // Notifier les listeners
                        notifyTimeAdvanced();

                        // Vérifications spéciales
                        checkSpecialEvents();
                    }
                }

                // Petit sleep pour éviter de surcharger le CPU
                Thread.sleep(10);

            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                System.err.println("Erreur dans la simulation: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Vérifie les événements spéciaux selon l'heure/date
     */
    private void checkSpecialEvents() {
        LocalDateTime time = city.getCurrentTime();
        int hour = time.getHour();
        int day = time.getDayOfMonth();

        // Événement de minuit (nouveau jour)
        if (hour == 0) {
            notifyNewDay();
        }

        // Événement de début de mois
        if (day == 1 && hour == 0) {
            notifyNewMonth();
        }

        // Game Over - Bonheur critique
        if (city.isGameOver()) {
            notifyGameOver();
            pause();
        }

        // Game Over - Pénurie critique (Black-out prolongé)
        // Si la production couvre moins de 50% de la demande pendant 24h consécutives
        double energyProduction = city.getTotalEnergyProduction();
        double energyDemand = city.getTotalEnergyDemand();

        boolean isBlackout = energyDemand > 0 && (energyProduction / energyDemand < 0.5);

        if (isBlackout) {
            consecutiveZeroHappiness++;

            // Warnings pour prévenir le joueur
            if (consecutiveZeroHappiness == 12) {
                notifyWarning("⚠️ ATTENTION : Black-out prolongé ! Risque d'émeutes.");
            } else if (consecutiveZeroHappiness == 36) {
                notifyWarning("🚨 DANGER : Émeutes imminentes ! Rétablissez le courant VITE !");
            }
        } else {
            // On ne reset que si le bonheur est AUSSI bon.
            // Si on a du courant mais que les gens sont malheureux (0%), on garde le
            // compteur
            if (city.getHappiness() > 0) {
                consecutiveZeroHappiness = 0;
            }
        }

        // Seuil: 48h de crise (au lieu de 24h)
        if (consecutiveZeroHappiness >= 48) {
            city.setGameOverReason("ÉMEUTES ! La ville a subi un black-out total pendant trop longtemps. (48h)");
            notifyGameOver();
            pause();
        }
    }

    /**
     * Met en pause / reprend la simulation
     */
    public void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            System.out.println("⏸️ Simulation en pause");
            notifyPaused();
        } else {
            System.out.println("▶️ Simulation reprise");
            lastUpdateTime = System.currentTimeMillis();
            notifyResumed();
        }
    }

    /**
     * Met en pause
     */
    public void pause() {
        if (!isPaused) {
            isPaused = true;
            notifyPaused();
        }
    }

    /**
     * Reprend
     */
    public void resume() {
        if (isPaused) {
            isPaused = false;
            lastUpdateTime = System.currentTimeMillis();
            notifyResumed();
        }
    }

    /**
     * Change la vitesse de jeu
     */
    public void setSpeed(GameSpeed speed) {
        this.currentSpeed = speed;
        System.out.println("⚡ Vitesse changée: " + speed.getDisplayName());
        notifySpeedChanged();
    }

    /**
     * Avance manuellement d'un certain nombre d'heures
     */
    public void skipHours(int hours) {
        for (int i = 0; i < hours && !city.isGameOver(); i++) {
            city.advanceTime();
        }
        notifyTimeAdvanced();
    }

    /**
     * Calcule le temps réel écoulé depuis la fondation
     */
    public Duration getRealTimeElapsed() {
        return Duration.between(city.getFoundationDate(), city.getCurrentTime());
    }

    /**
     * Calcule le temps de jeu en heures
     */
    public long getGameHoursElapsed() {
        return Duration.between(city.getFoundationDate(), city.getCurrentTime()).toHours();
    }

    /**
     * Retourne une estimation du temps réel pour atteindre une date
     */
    public long getEstimatedRealTimeToReach(LocalDateTime targetDate) {
        long hoursToGo = Duration.between(city.getCurrentTime(), targetDate).toHours();
        return hoursToGo * currentSpeed.getMillisecondsPerHour();
    }

    // === LISTENERS ===

    public void addListener(TimeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(TimeListener listener) {
        listeners.remove(listener);
    }

    private void notifyTimeAdvanced() {
        for (TimeListener listener : listeners) {
            listener.onTimeAdvanced(city.getCurrentTime());
        }
    }

    private void notifyNewDay() {
        for (TimeListener listener : listeners) {
            listener.onNewDay(city.getCurrentTime());
        }
    }

    private void notifyNewMonth() {
        for (TimeListener listener : listeners) {
            listener.onNewMonth(city.getCurrentTime());
        }
    }

    private void notifyPaused() {
        for (TimeListener listener : listeners) {
            listener.onPaused();
        }
    }

    private void notifyResumed() {
        for (TimeListener listener : listeners) {
            listener.onResumed();
        }
    }

    private void notifySpeedChanged() {
        for (TimeListener listener : listeners) {
            listener.onSpeedChanged(currentSpeed);
        }
    }

    private void notifyGameOver() {
        for (TimeListener listener : listeners) {
            listener.onGameOver(city.getGameOverReason());
        }
    }

    private void notifyWarning(String message) {
        for (TimeListener listener : listeners) {
            listener.onWarning(message);
        }
    }

    // === GETTERS ===

    public GameSpeed getCurrentSpeed() {
        return currentSpeed;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public City getCity() {
        return city;
    }

}

/**
 * Vitesses de jeu disponibles
 */
