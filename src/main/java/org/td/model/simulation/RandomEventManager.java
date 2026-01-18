package org.td.model.simulation;

import org.td.model.entities.City;
import org.td.model.entities.Residence;
import org.td.model.enums.ResidenceLevel;
import org.td.model.enums.EventType;
import org.td.controller.GameEventListener;

import java.io.Serializable;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

/**
 * Gère les événements aléatoires (Canicule, Crise, etc.)
 */
public class RandomEventManager implements Serializable {
    private static final long serialVersionUID = 1L;

    private City city;
    private Random random;
    private List<GameEventListener> listeners;

    // État de l'événement en cours
    private GameEvent currentEvent;
    private int eventDurationRemaining; // En heures de jeu

    // Paramètres
    private static final double EVENT_PROBABILITY = 0.005; // 0.5% de chance par heure (~1 événement tous les 8-10
                                                           // jours)

    public RandomEventManager(City city) {
        this.city = city;
        this.random = new Random();
        this.listeners = new ArrayList<>();
    }

    public void update() {
        // Si un événement est en cours
        if (currentEvent != null) {
            eventDurationRemaining--;
            if (eventDurationRemaining <= 0) {
                endCurrentEvent();
            }
            return; // Pas de nouvel événement tant qu'un est actif
        }

        // Tirage au sort d'un nouvel événement
        if (random.nextDouble() < EVENT_PROBABILITY) {
            triggerRandomEvent();
        }
    }

    private void triggerRandomEvent() {
        GameEvent[] events = GameEvent.values();
        GameEvent newEvent = events[random.nextInt(events.length)];
        startEvent(newEvent);
    }

    public void startEvent(GameEvent event) {
        this.currentEvent = event;
        // Durée aléatoire entre min et max (en heures)
        this.eventDurationRemaining = event.getMinDuration()
                + random.nextInt(event.getMaxDuration() - event.getMinDuration());

        applyEventEffects(event, true);
        notifyEvent("⚠️ ÉVÉNEMENT : " + event.getDisplayName(), EventType.WARNING);
        notifyEvent(event.getDescription(), EventType.INFO);
    }

    private void endCurrentEvent() {
        if (currentEvent != null) {
            notifyEvent("✅ FIN DE L'ÉVÉNEMENT : " + currentEvent.getDisplayName(), EventType.SUCCESS);
            applyEventEffects(currentEvent, false);
            currentEvent = null;
        }
    }

    private void applyEventEffects(GameEvent event, boolean isActive) {
        switch (event) {
            case HEATWAVE:
                // +50% Demande énergétique
                city.setGlobalEnergyDemandMultiplier(isActive ? 1.5 : 1.0);
                break;
            case COLD_SNAP:
                // +30% Demande énergétique
                city.setGlobalEnergyDemandMultiplier(isActive ? 1.3 : 1.0);
                break;
            case ECONOMIC_CRISIS:
                // -40% Revenus
                city.setGlobalRevenueMultiplier(isActive ? 0.6 : 1.0);
                break;
            case BABY_BOOM:
                if (isActive) {
                    // Effet instantané : on ajoute 3 à 5 résidences
                    int buildingsToAdd = 3 + random.nextInt(3);
                    for (int i = 0; i < buildingsToAdd; i++) {
                        int x = 100 + random.nextInt(800);
                        int y = 100 + random.nextInt(600);
                        city.addBuilding(new Residence(ResidenceLevel.BASIC, x, y));
                    }
                    notifyEvent("👶 " + buildingsToAdd + " nouvelles résidences construites !", EventType.SUCCESS);
                    // Cet événement n'a pas de durée continue, on le termine tout de suite
                    currentEvent = null;
                }
                break;
            case GRID_FAILURE:
                // TODO: Implémenter impact stabilité réseau si on avait ce détail
                // Pour l'instant on simule par une hausse massive de demande (court-circuit ?)
                // ou on pourrait toucher à l'efficacité des centrales.
                // Disons +20% Demand pour simuler les pertes
                city.setGlobalEnergyDemandMultiplier(isActive ? 1.2 : 1.0);
                break;
        }
    }

    // === GESTION LISTENERS ===

    public void addListener(GameEventListener listener) {
        listeners.add(listener);
    }

    private void notifyEvent(String message, EventType type) {
        for (GameEventListener listener : listeners) {
            listener.onGameEvent(message, type);
        }
    }

    // === ENUM DES EVENTS ===

    public enum GameEvent {
        HEATWAVE("Canicule", "La chaleur est insupportable ! La demande en climatisation explose.", 48, 72),
        COLD_SNAP("Vague de Froid", "Un froid polaire s'abat sur la ville. Le chauffage tourne à plein régime.", 48,
                72),
        ECONOMIC_CRISIS("Crise Économique", "Le marché s'effondre. Les revenus sont réduits de 40%.", 72, 120),
        BABY_BOOM("Boom Démographique", "La ville attire de nouveaux habitants ! Construction immédiate de logements.",
                1, 2),
        GRID_FAILURE("Instabilité du Réseau", "Des perturbations magnétiques affectent le transport d'électricité.", 12,
                24);

        private final String displayName;
        private final String description;
        private final int minDuration;
        private final int maxDuration;

        GameEvent(String displayName, String description, int minDuration, int maxDuration) {
            this.displayName = displayName;
            this.description = description;
            this.minDuration = minDuration;
            this.maxDuration = maxDuration;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        public int getMinDuration() {
            return minDuration;
        }

        public int getMaxDuration() {
            return maxDuration;
        }
    }
}
