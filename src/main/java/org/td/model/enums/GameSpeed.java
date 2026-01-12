package org.td.model.enums;

/**
 * Vitesses de jeu disponibles
 * Définit à quelle vitesse le temps s'écoule dans le jeu
 */
public enum GameSpeed {
    SLOW(
            "Lent",
            "🐌",
            2000,      // 2 secondes réelles = 1 heure de jeu
            0.5,       // 0.5x vitesse normale
            "Idéal pour débutants et planification détaillée"
    ),

    NORMAL(
            "Normal",
            "▶️",
            1000,      // 1 seconde réelle = 1 heure de jeu
            1.0,       // 1x vitesse normale
            "Vitesse équilibrée pour jouer confortablement"
    ),

    FAST(
            "Rapide",
            "⏩",
            500,       // 0.5 seconde réelle = 1 heure de jeu
            2.0,       // 2x vitesse normale
            "Pour accélérer le développement de la ville"
    ),

    ULTRA_FAST(
            "Ultra Rapide",
            "⚡",
            200,       // 0.2 seconde réelle = 1 heure de jeu
            5.0,       // 5x vitesse normale
            "Vitesse maximale pour tester rapidement"
    );

    private final String displayName;
    private final String icon;
    private final long millisecondsPerHour;
    private final double speedMultiplier;
    private final String description;

    /**
     * Constructeur
     */
    GameSpeed(String displayName, String icon, long millisecondsPerHour,
              double speedMultiplier, String description) {
        this.displayName = displayName;
        this.icon = icon;
        this.millisecondsPerHour = millisecondsPerHour;
        this.speedMultiplier = speedMultiplier;
        this.description = description;
    }

    /**
     * Retourne combien d'heures de jeu passent en 1 heure réelle
     */
    public double getGameHoursPerRealHour() {
        return 3600000.0 / millisecondsPerHour;
    }

    /**
     * Retourne combien de jours de jeu passent en 1 heure réelle
     */
    public double getGameDaysPerRealHour() {
        return getGameHoursPerRealHour() / 24.0;
    }

    /**
     * Retourne combien de temps réel pour 1 jour de jeu
     */
    public double getRealMinutesPerGameDay() {
        double hoursPerDay = 24;
        double realSecondsPerDay = hoursPerDay * (millisecondsPerHour / 1000.0);
        return realSecondsPerDay / 60.0;
    }

    /**
     * Retourne combien de temps réel pour 1 année de jeu
     */
    public double getRealHoursPerGameYear() {
        return getRealMinutesPerGameDay() * 365 / 60.0;
    }

    /**
     * Obtient une description détaillée avec calculs
     */
    public String getDetailedDescription() {
        return String.format(
                "%s %s\n" +
                        "%s\n\n" +
                        "📊 Détails:\n" +
                        "• %.1f heures de jeu par heure réelle\n" +
                        "• %.1f jours de jeu par heure réelle\n" +
                        "• %.1f minutes réelles par jour de jeu\n" +
                        "• %.1f heures réelles pour 1 an de jeu",
                icon, displayName,
                description,
                getGameHoursPerRealHour(),
                getGameDaysPerRealHour(),
                getRealMinutesPerGameDay(),
                getRealHoursPerGameYear()
        );
    }

    /**
     * Retourne la vitesse suivante
     */
    public GameSpeed next() {
        return switch(this) {
            case SLOW -> NORMAL;
            case NORMAL -> FAST;
            case FAST -> ULTRA_FAST;
            case ULTRA_FAST -> ULTRA_FAST; // Max
        };
    }

    /**
     * Retourne la vitesse précédente
     */
    public GameSpeed previous() {
        return switch(this) {
            case SLOW -> SLOW; // Min
            case NORMAL -> SLOW;
            case FAST -> NORMAL;
            case ULTRA_FAST -> FAST;
        };
    }

    /**
     * Vérifie si c'est la vitesse minimale
     */
    public boolean isMin() {
        return this == SLOW;
    }

    /**
     * Vérifie si c'est la vitesse maximale
     */
    public boolean isMax() {
        return this == ULTRA_FAST;
    }

    // Getters
    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public long getMillisecondsPerHour() {
        return millisecondsPerHour;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return icon + " " + displayName;
    }
}