package org.td.model.entities;

import org.td.model.enums.PowerPlantType;

import java.util.Random;

/**
 * Éolienne
 * - Avantages: Propre, coûts modérés
 * - Inconvénients: Production très variable selon le vent
 */
public class WindTurbine extends PowerPlant {
    private static final long serialVersionUID = 1L;

    // Caractéristiques spécifiques à l'éolien
    private double windSpeed; // Vitesse du vent actuelle (m/s)
    private double windMultiplier; // Multiplicateur de production selon vent
    private double optimalWindSpeed; // Vitesse optimale (m/s)
    private double minWindSpeed; // Vitesse minimale pour produire
    private double maxWindSpeed; // Vitesse maximale avant arrêt sécurité

    private Random random;

    /**
     * Constructeur
     */
    public WindTurbine(int level, int x, int y) {
        super(PowerPlantType.WIND, level, x, y,
                200 * level, // Production modérée
                8000 * level); // Coût modéré

        // Paramètres spécifiques
        this.maintenanceCostPerHour = 20 + (level * 8);
        this.pollutionLevel = 0.2; // Très propre
        this.operatingCostPerKWh = 0.005; // Très bas coût opérationnel
        this.maintenanceInterval = 1440; // 60 jours

        // Vent
        this.windSpeed = 8.0; // Vent moyen: 8 m/s
        this.optimalWindSpeed = 12.0; // Production max à 12 m/s
        this.minWindSpeed = 3.0; // Minimum: 3 m/s
        this.maxWindSpeed = 25.0; // Arrêt sécurité au-dessus
        this.windMultiplier = 0.5;

        this.random = new Random();
    }

    @Override
    protected void updateProduction() {
        // Simulation du vent variable
        updateWindSpeed();

        // Calcul du multiplicateur selon vitesse du vent
        windMultiplier = calculateWindMultiplier();

        // Production dépend du vent
        currentProduction = maxProduction * efficiency * windMultiplier;
    }

    /**
     * Met à jour la vitesse du vent (simulation météo)
     */
    private void updateWindSpeed() {
        // Variation aléatoire du vent (-2 à +2 m/s par heure)
        double change = (random.nextDouble() - 0.5) * 4;
        windSpeed += change;

        // Limites réalistes (0-35 m/s)
        windSpeed = Math.max(0, Math.min(35, windSpeed));
    }

    /**
     * Définit manuellement la vitesse du vent (pour simulation météo externe)
     */
    public void setWindSpeed(double speed) {
        this.windSpeed = Math.max(0, Math.min(35, speed));
    }

    /**
     * Calcule le multiplicateur de production selon le vent
     */
    private double calculateWindMultiplier() {
        if (windSpeed < minWindSpeed) {
            return 0.0; // Pas assez de vent
        }

        if (windSpeed > maxWindSpeed) {
            return 0.0; // Arrêt sécurité (trop de vent)
        }

        if (windSpeed <= optimalWindSpeed) {
            // Montée progressive: 0 -> 100%
            return (windSpeed - minWindSpeed) / (optimalWindSpeed - minWindSpeed);
        } else {
            // Descente après optimal jusqu'à arrêt sécurité
            return 1.0 - ((windSpeed - optimalWindSpeed) / (maxWindSpeed - optimalWindSpeed)) * 0.5;
        }
    }

    /**
     * Met à jour selon conditions météo
     */
    public void updateWeather(String weatherType) {
        // Ajustement du vent selon météo
        switch(weatherType) {
            case "CALM" -> windSpeed = 2 + random.nextDouble() * 3; // 2-5 m/s
            case "LIGHT_WIND" -> windSpeed = 5 + random.nextDouble() * 5; // 5-10 m/s
            case "MODERATE_WIND" -> windSpeed = 10 + random.nextDouble() * 5; // 10-15 m/s
            case "STRONG_WIND" -> windSpeed = 15 + random.nextDouble() * 8; // 15-23 m/s
            case "STORMY" -> windSpeed = 25 + random.nextDouble() * 10; // 25-35 m/s
        }
        updateProduction();
    }

    @Override
    protected void degradeEfficiency() {
        // Les éoliennes subissent l'usure du vent
        efficiency = Math.max(0.6, efficiency - 0.00008);
    }

    @Override
    public int getWidth() {
        return 1; // Les éoliennes sont plus petites
    }

    @Override
    public int getHeight() {
        return 1;
    }

    @Override
    public String getType() {
        return "WindTurbine";
    }

    @Override
    public String getStatus() {
        if (windSpeed > maxWindSpeed) return "Arrêt sécurité (vent trop fort)";
        if (windSpeed < minWindSpeed) return "Pas assez de vent";
        return super.getStatus();
    }

    @Override
    public String getDescription() {
        String windCondition = getWindCondition();
        return super.getDescription() +
                String.format("\nVent: %.1f m/s (%s)\nProduction: %.0f%%",
                        windSpeed, windCondition, windMultiplier * 100);
    }

    /**
     * Retourne une description des conditions de vent
     */
    private String getWindCondition() {
        if (windSpeed < minWindSpeed) return "Calme";
        if (windSpeed < 8) return "Faible";
        if (windSpeed < optimalWindSpeed) return "Modéré";
        if (windSpeed < 18) return "Fort";
        if (windSpeed < maxWindSpeed) return "Très fort";
        return "Tempête";
    }

    /**
     * Calcule la production moyenne estimée (sur base statistique)
     */
    public double getAverageDailyProduction() {
        // Estimation basée sur moyenne de vent de 8 m/s
        return maxProduction * efficiency * 0.35; // ~35% capacité moyenne
    }

    // Getters
    public double getWindSpeed() {
        return windSpeed;
    }

    public double getWindMultiplier() {
        return windMultiplier;
    }

    public double getOptimalWindSpeed() {
        return optimalWindSpeed;
    }

    public double getMinWindSpeed() {
        return minWindSpeed;
    }

    public double getMaxWindSpeed() {
        return maxWindSpeed;
    }
}