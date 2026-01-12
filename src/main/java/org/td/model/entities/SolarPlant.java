package org.td.model.entities;


import org.td.model.enums.PowerPlantType;

/**
 * Centrale solaire (panneaux photovoltaïques)
 * - Avantages: Très propre, faibles coûts opérationnels
 * - Inconvénients: Coûteuse, production variable selon l'heure, météo
 */
public class SolarPlant extends PowerPlant {
    private static final long serialVersionUID = 1L;

    // Caractéristiques spécifiques au solaire
    private double solarMultiplier; // Multiplicateur solaire actuel (0-1)
    private int currentHour; // Heure actuelle pour calcul production
    private double weatherMultiplier; // Multiplicateur météo (0.5-1.0)

    /**
     * Constructeur
     */
    public SolarPlant(int level, int x, int y) {
        super(PowerPlantType.SOLAR, level, x, y,
                300 * level, // Production de base (plus faible que charbon)
                15000 * level); // Très coûteux

        // Paramètres spécifiques
        this.maintenanceCostPerHour = 10 + (level * 5);
        this.pollutionLevel = 0.5; // Très propre
        this.operatingCostPerKWh = 0.01; // Très bas coût opérationnel
        this.maintenanceInterval = 2160; // 90 jours (nécessite moins de maintenance)

        this.solarMultiplier = 0.0;
        this.currentHour = 12; // Midi par défaut
        this.weatherMultiplier = 1.0; // Beau temps par défaut
    }

    @Override
    protected void updateProduction() {
        // Calcul du multiplicateur solaire selon l'heure
        solarMultiplier = calculateSolarMultiplier(currentHour);

        // Production dépend du soleil et de la météo
        currentProduction = maxProduction * efficiency * solarMultiplier * weatherMultiplier;
    }

    /**
     * Calcule le multiplicateur solaire selon l'heure
     * Production nulle la nuit, maximale à midi
     */
    private double calculateSolarMultiplier(int hour) {
        if (hour < 6 || hour >= 20) {
            return 0.0; // Nuit: pas de production
        } else if (hour >= 6 && hour < 8) {
            // Lever du soleil: 0% -> 60%
            return (hour - 6) * 0.3;
        } else if (hour >= 8 && hour < 11) {
            // Matin: 60% -> 95%
            return 0.6 + (hour - 8) * 0.117;
        } else if (hour >= 11 && hour <= 13) {
            // Midi: production maximale (95-100%)
            return 0.95 + Math.sin(Math.PI * (hour - 11) / 2.0) * 0.05;
        } else if (hour > 13 && hour <= 17) {
            // Après-midi: 95% -> 60%
            return 0.95 - (hour - 13) * 0.0875;
        } else {
            // Coucher du soleil: 60% -> 0%
            return Math.max(0, 0.6 - (hour - 17) * 0.2);
        }
    }

    /**
     * Met à jour l'heure pour le calcul de production
     */
    public void updateHour(int hour) {
        this.currentHour = hour % 24;
        updateProduction();
    }

    /**
     * Met à jour la météo (appelé par simulation météo)
     * @param weatherType Type de météo (SUNNY, CLOUDY, RAINY, STORMY)
     */
    public void updateWeather(String weatherType) {
        this.weatherMultiplier = switch(weatherType) {
            case "SUNNY" -> 1.0; // Plein ensoleillement
            case "PARTLY_CLOUDY" -> 0.8; // Partiellement nuageux
            case "CLOUDY" -> 0.5; // Nuageux
            case "RAINY" -> 0.3; // Pluie
            case "STORMY" -> 0.1; // Tempête
            default -> 1.0;
        };
        updateProduction();
    }

    @Override
    protected void degradeEfficiency() {
        // Les panneaux solaires se dégradent moins vite
        efficiency = Math.max(0.7, efficiency - 0.00005);
    }

    @Override
    public String getType() {
        return "SolarPlant";
    }

    @Override
    public String getDescription() {
        return super.getDescription() +
                String.format("\nEnsoleillement: %.0f%%\nMétéo: %.0f%%",
                        solarMultiplier * 100, weatherMultiplier * 100);
    }

    /**
     * Calcule la production moyenne sur 24h
     */
    public double getAverageDailyProduction(){
        double total = 0;
        for (int h = 0; h < 24; h++) {
            total += calculateSolarMultiplier(h);
        }
        return (total / 24.0) * maxProduction * efficiency * weatherMultiplier;
    }

    // Getters
    public double getSolarMultiplier() {
        return solarMultiplier;
    }

    public int getCurrentHour() {
        return currentHour;
    }

    public double getWeatherMultiplier() {
        return weatherMultiplier;
    }
}