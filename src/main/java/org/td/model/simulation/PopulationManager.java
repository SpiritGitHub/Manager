package org.td.model.simulation;


import org.td.model.enums.BuildingType;
import org.td.model.entities.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Gère la population de la ville
 * Migration, satisfaction, besoins et événements démographiques
 */
public class PopulationManager {
    private City city;
    private Random random;

    // Statistiques démographiques
    private int immigrationCount;
    private int emigrationCount;
    private double birthRate; // Par 1000 habitants par an
    private double migrationRate; // Taux de migration net

    // Besoins de la population
    private Map<String, Double> needsSatisfaction; // Santé, éducation, sécurité, etc.

    // Historique
    private int peakPopulation;
    private int previousPopulation;

    /**
     * Constructeur
     */
    public PopulationManager(City city) {
        this.city = city;
        this.random = new Random();
        this.immigrationCount = 0;
        this.emigrationCount = 0;
        this.birthRate = 12.0; // 12 naissances pour 1000 habitants/an
        this.migrationRate = 0;
        this.needsSatisfaction = new HashMap<>();
        this.peakPopulation = 0;
        this.previousPopulation = city.getPopulation();

        initializeNeeds();
    }

    /**
     * Initialise les besoins
     */
    private void initializeNeeds() {
        needsSatisfaction.put("Énergie", 100.0);
        needsSatisfaction.put("Santé", 50.0);
        needsSatisfaction.put("Éducation", 50.0);
        needsSatisfaction.put("Sécurité", 50.0);
        needsSatisfaction.put("Loisirs", 50.0);
        needsSatisfaction.put("Commerce", 50.0);
    }

    /**
     * Met à jour la population (appelé chaque heure)
     */
    public void update() {
        updateNeeds();
        handleNaturalGrowth();
        handleMigration();
        updateStatistics();
    }

    /**
     * Met à jour la satisfaction des besoins
     */
    private void updateNeeds() {
        int population = city.getPopulation();
        if (population == 0) return;

        // Énergie
        double energyRatio = city.getTotalEnergyDemand() > 0 ?
                city.getTotalEnergyProduction() / city.getTotalEnergyDemand() : 1.0;
        needsSatisfaction.put("Énergie", Math.min(100, energyRatio * 100));

        // Santé (basé sur hôpitaux)
        long hospitals = city.getInfrastructures().stream()
                .filter(i -> i.getInfrastructureType() == BuildingType.HOSPITAL)
                .count();
        double healthSatisfaction = Math.min(100, (hospitals * 150.0 / population) * 100);
        needsSatisfaction.put("Santé", healthSatisfaction);

        // Éducation (basé sur écoles + universités)
        long schools = city.getInfrastructures().stream()
                .filter(i -> i.getInfrastructureType() == BuildingType.SCHOOL ||
                        i.getInfrastructureType() == BuildingType.UNIVERSITY)
                .count();
        double eduSatisfaction = Math.min(100, (schools * 400.0 / population) * 100);
        needsSatisfaction.put("Éducation", eduSatisfaction);

        // Sécurité (police + pompiers)
        long security = city.getInfrastructures().stream()
                .filter(i -> i.getInfrastructureType() == BuildingType.POLICE_STATION ||
                        i.getInfrastructureType() == BuildingType.FIRE_STATION)
                .count();
        double securitySatisfaction = Math.min(100, (security * 100.0 / population) * 100);
        needsSatisfaction.put("Sécurité", securitySatisfaction);

        // Loisirs (divertissement + parcs)
        long entertainment = city.getInfrastructures().stream()
                .filter(i -> i.getInfrastructureType() == BuildingType.ENTERTAINMENT ||
                        i.getInfrastructureType() == BuildingType.PARK ||
                        i.getInfrastructureType() == BuildingType.STADIUM)
                .count();
        double leisureSatisfaction = Math.min(100, (entertainment * 300.0 / population) * 100);
        needsSatisfaction.put("Loisirs", leisureSatisfaction);

        // Commerce
        long commercial = city.getInfrastructures().stream()
                .filter(i -> i.getInfrastructureType() == BuildingType.COMMERCIAL)
                .count();
        double commerceSatisfaction = Math.min(100, (commercial * 200.0 / population) * 100);
        needsSatisfaction.put("Commerce", commerceSatisfaction);
    }

    /**
     * Gère la croissance naturelle (naissances)
     */
    private void handleNaturalGrowth() {
        int hour = city.getCurrentTime().getHour();
        if (hour != 0) return; // Une fois par jour

        int population = city.getPopulation();
        if (population == 0) return;

        // Taux de naissance quotidien
        double dailyBirthRate = birthRate / 1000.0 / 365.0;

        // Naissances possibles
        if (random.nextDouble() < dailyBirthRate * population) {
            // Ajoute des habitants à une résidence aléatoire
            if (!city.getResidences().isEmpty()) {
                Residence residence = city.getResidences().get(
                        random.nextInt(city.getResidences().size())
                );
                // La croissance est gérée dans Residence.update()
            }
        }
    }

    /**
     * Gère la migration (immigration/émigration)
     */
    private void handleMigration() {
        int hour = city.getCurrentTime().getHour();
        if (hour % 6 != 0) return; // Toutes les 6 heures

        double happiness = city.getHappiness();
        double avgNeedsSatisfaction = needsSatisfaction.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(50);

        // Calcul attractivité de la ville (0-100)
        double attractiveness = (happiness * 0.6 + avgNeedsSatisfaction * 0.4);

        // Immigration si ville attractive
        if (attractiveness > 60 && random.nextDouble() < 0.3) {
            handleImmigration((int)((attractiveness - 60) / 10));
        }

        // Émigration si ville peu attractive
        if (attractiveness < 40 && random.nextDouble() < 0.4) {
            handleEmigration((int)((40 - attractiveness) / 10));
        }

        // Calcul taux de migration
        int currentPop = city.getPopulation();
        if (previousPopulation > 0) {
            migrationRate = ((double)(currentPop - previousPopulation) /
                    previousPopulation) * 100;
        }
        previousPopulation = currentPop;
    }

    /**
     * Gère l'immigration
     */
    private void handleImmigration(int intensity) {
        // Créer une nouvelle petite résidence
        if (city.getMoney() > 5000 && random.nextDouble() < 0.5) {
            // La croissance automatique de la ville gérera ça
            immigrationCount += 5 + random.nextInt(intensity * 3);
            System.out.println("👥 Immigration: Nouveaux arrivants dans la ville");
        }
    }

    /**
     * Gère l'émigration
     */
    private void handleEmigration(int intensity) {
        if (!city.getResidences().isEmpty()) {
            // Réduction de population dans résidences existantes
            for (int i = 0; i < Math.min(intensity, city.getResidences().size()); i++) {
                Residence residence = city.getResidences().get(
                        random.nextInt(city.getResidences().size())
                );
                // La réduction est gérée dans Residence.update()
            }
            emigrationCount += 3 + random.nextInt(intensity * 2);
            System.out.println("📉 Émigration: Des habitants quittent la ville");
        }
    }

    /**
     * Met à jour les statistiques
     */
    private void updateStatistics() {
        int population = city.getPopulation();
        if (population > peakPopulation) {
            peakPopulation = population;
        }
    }

    /**
     * Calcule la densité de population
     */
    public double getPopulationDensity() {
        int residences = city.getResidences().size();
        if (residences == 0) return 0;
        return (double)city.getPopulation() / residences;
    }

    /**
     * Retourne les besoins non satisfaits
     */
    public Map<String, Double> getUnsatisfiedNeeds() {
        Map<String, Double> unsatisfied = new HashMap<>();
        for (Map.Entry<String, Double> entry : needsSatisfaction.entrySet()) {
            if (entry.getValue() < 50) {
                unsatisfied.put(entry.getKey(), entry.getValue());
            }
        }
        return unsatisfied;
    }

    /**
     * Recommandations pour améliorer la satisfaction
     */
    public java.util.List<String> getRecommendations() {
        java.util.List<String> recommendations = new java.util.ArrayList<>();

        for (Map.Entry<String, Double> entry : needsSatisfaction.entrySet()) {
            String need = entry.getKey();
            double satisfaction = entry.getValue();

            if (satisfaction < 30) {
                String recommendation = switch(need) {
                    case "Santé" -> "🏥 Construire un hôpital d'urgence";
                    case "Éducation" -> "🏫 Construire des écoles";
                    case "Sécurité" -> "🚓 Construire commissariat/caserne";
                    case "Loisirs" -> "🎭 Créer des espaces de loisirs";
                    case "Commerce" -> "🏪 Développer zones commerciales";
                    case "Énergie" -> "⚡ Augmenter production électrique";
                    default -> "";
                };
                if (!recommendation.isEmpty()) {
                    recommendations.add(recommendation +
                            String.format(" (Satisfaction: %.0f%%)", satisfaction));
                }
            }
        }

        // Surpopulation
        if (getPopulationDensity() > 100) {
            recommendations.add("🏘️ Densité élevée: Construire plus de logements");
        }

        // Migration négative
        if (migrationRate < -5) {
            recommendations.add("📉 Émigration importante: Améliorer conditions de vie");
        }

        return recommendations;
    }

    /**
     * Génère un rapport démographique
     */
    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== RAPPORT DÉMOGRAPHIQUE ===\n\n");

        report.append(String.format("Population totale: %d habitants\n",
                city.getPopulation()));
        report.append(String.format("Population max: %d\n", peakPopulation));
        report.append(String.format("Densité: %.1f hab/résidence\n",
                getPopulationDensity()));
        report.append(String.format("Taux migration: %.1f%%\n", migrationRate));
        report.append(String.format("Immigration: %d\n", immigrationCount));
        report.append(String.format("Émigration: %d\n\n", emigrationCount));

        report.append("Satisfaction des besoins:\n");
        for (Map.Entry<String, Double> entry : needsSatisfaction.entrySet()) {
            report.append(String.format("  %s: %.0f%%\n",
                    entry.getKey(), entry.getValue()));
        }

        return report.toString();
    }

    /**
     * Calcule le score de qualité de vie (0-100)
     */
    public double getQualityOfLifeScore() {
        double avgNeeds = needsSatisfaction.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(50);

        double happiness = city.getHappiness();

        // Pénalité pollution
        double pollutionPenalty = Math.min(20, city.getTotalPollution() / 10.0);

        return Math.max(0, Math.min(100,
                (avgNeeds * 0.5 + happiness * 0.5) - pollutionPenalty));
    }

    // Getters
    public int getImmigrationCount() {
        return immigrationCount;
    }

    public int getEmigrationCount() {
        return emigrationCount;
    }

    public double getBirthRate() {
        return birthRate;
    }

    public double getMigrationRate() {
        return migrationRate;
    }

    public Map<String, Double> getNeedsSatisfaction() {
        return new HashMap<>(needsSatisfaction);
    }

    public int getPeakPopulation() {
        return peakPopulation;
    }
}