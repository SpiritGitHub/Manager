package org.td.model.simulation;

import org.td.model.entities.Building;
import org.td.model.entities.City;
import org.td.model.entities.Infrastructure;
import org.td.model.entities.PowerPlant;
import org.td.model.enums.FinancialHealth;

import java.util.ArrayList;
import java.util.List;

/**
 * Gère l'économie de la ville
 * Calcule revenus, dépenses, taxes, et propose des analyses financières
 */
public class EconomyManager {
    private City city;

    // Paramètres économiques
    private double electricityPricePerKWh; // Prix de vente électricité
    private double taxRate; // Taux de taxation (%)
    private double inflationRate; // Taux d'inflation annuel (%)

    // Statistiques financières
    private double totalRevenueAllTime;
    private double totalExpensesAllTime;
    private List<Transaction> recentTransactions;
    private static final int MAX_TRANSACTIONS = 100;

    // Budget mensuel
    private double monthlyRevenue;
    private double monthlyExpenses;
    private int currentMonth;

    /**
     * Constructeur
     */
    public EconomyManager(City city) {
        this.city = city;
        this.electricityPricePerKWh = 8.0; // 8 coins par kWh pour vente excess
        this.taxRate = 5.0; // 5% de taxe
        this.inflationRate = 2.0; // 2% par an
        this.recentTransactions = new ArrayList<>();
        this.totalRevenueAllTime = 0;
        this.totalExpensesAllTime = 0;
        this.monthlyRevenue = 0;
        this.monthlyExpenses = 0;
        this.currentMonth = city.getCurrentTime().getMonthValue();
    }

    /**
     * Met à jour l'économie (appelé chaque heure)
     */
    public void update() {
        checkNewMonth();
        calculateHourlyFinances();
        applyInflation();
    }

    /**
     * Vérifie si c'est un nouveau mois
     */
    private void checkNewMonth() {
        int month = city.getCurrentTime().getMonthValue();
        if (month != currentMonth) {
            generateMonthlyReport();
            monthlyRevenue = 0;
            monthlyExpenses = 0;
            currentMonth = month;
        }
    }

    /**
     * Calcule les finances horaires
     */
    private void calculateHourlyFinances() {
        double revenue = 0;
        double expenses = 0;

        // === REVENUS ===

        // 1. Factures des résidents (200 coins/mois = ~0.28/hour) -> Augmenté à ~0.50
        double residentBills = city.getPopulation() * 0.50; // 0.50 coins par habitant par heure
        revenue += residentBills;

        // 2. Vente d'électricité EXCÉDENTAIRE
        double excessPower = Math.max(0, city.getTotalEnergyProduction() - city.getTotalEnergyDemand());
        double electricityRevenue = excessPower * electricityPricePerKWh;
        revenue += electricityRevenue;

        // 3. Taxe municipale (1000 coins/mois par niveau = ~1.39/hour par niveau) ->
        // Augmenté à 5.0
        double cityTax = city.getLevel() * 5.0;
        revenue += cityTax;

        // 4. Revenus des infrastructures commerciales
        double infraRevenue = city.getInfrastructures().stream()
                .mapToDouble(Infrastructure::getHourlyRevenue)
                .sum();
        revenue += infraRevenue;

        // === DÉPENSES ===

        // 1. Maintenance des centrales
        double plantMaintenance = city.getPowerPlants().stream()
                .filter(Building::isActive)
                .mapToDouble(PowerPlant::getHourlyCost)
                .sum();
        expenses += plantMaintenance;

        // 2. Maintenance des infrastructures
        double infraMaintenance = city.getInfrastructures().stream()
                .filter(Building::isActive)
                .mapToDouble(Infrastructure::getMaintenanceCost)
                .sum();
        expenses += infraMaintenance;

        // 3. Coûts administratifs (basé sur taille ville)
        double adminCosts = city.getPopulation() * 0.02; // 2 centimes par habitant
        expenses += adminCosts;

        // Enregistrement
        recordTransaction("Revenus horaires", revenue, TransactionType.REVENUE);
        recordTransaction("Dépenses horaires", expenses, TransactionType.EXPENSE);

        monthlyRevenue += revenue;
        monthlyExpenses += expenses;
        totalRevenueAllTime += revenue;
        totalExpensesAllTime += expenses;
    }

    /**
     * Applique l'inflation (mensuelle)
     */
    private void applyInflation() {
        int hour = city.getCurrentTime().getHour();
        int day = city.getCurrentTime().getDayOfMonth();

        // Applique l'inflation le 1er du mois à minuit
        if (day == 1 && hour == 0) {
            double monthlyInflation = inflationRate / 12.0 / 100.0;
            electricityPricePerKWh *= (1 + monthlyInflation);
        }
    }

    /**
     * Enregistre une transaction
     */
    private void recordTransaction(String description, double amount, TransactionType type) {
        Transaction transaction = new Transaction(
                city.getCurrentTime(),
                description,
                amount,
                type);

        recentTransactions.add(transaction);

        // Limite le nombre de transactions gardées
        if (recentTransactions.size() > MAX_TRANSACTIONS) {
            recentTransactions.remove(0);
        }
    }

    /**
     * Calcule le bilan net (profit/perte)
     */
    public double getNetIncome() {
        return city.getTotalRevenue() - city.getTotalExpenses();
    }

    /**
     * Calcule le bilan mensuel
     */
    public double getMonthlyNetIncome() {
        return monthlyRevenue - monthlyExpenses;
    }

    /**
     * Calcule le bilan total depuis création
     */
    public double getTotalNetIncome() {
        return totalRevenueAllTime - totalExpensesAllTime;
    }

    /**
     * Vérifie si la ville est en bonne santé financière
     */
    public FinancialHealth getFinancialHealth() {
        double money = city.getMoney();
        double netIncome = getNetIncome();

        if (money < 0)
            return FinancialHealth.CRITICAL;
        if (money < 5000 || netIncome < -100)
            return FinancialHealth.POOR;
        if (money < 20000 || netIncome < 0)
            return FinancialHealth.MODERATE;
        if (netIncome > 500)
            return FinancialHealth.EXCELLENT;
        return FinancialHealth.GOOD;
    }

    /**
     * Calcule le temps avant faillite si revenus actuels
     */
    public int getHoursUntilBankruptcy() {
        double netIncome = getNetIncome();
        if (netIncome >= 0)
            return -1; // Pas de faillite

        double money = city.getMoney();
        if (money <= 0)
            return 0; // Déjà en faillite

        return (int) (money / Math.abs(netIncome));
    }

    /**
     * Recommandations financières
     */
    public List<String> getFinancialRecommendations() {
        List<String> recommendations = new ArrayList<>();

        double money = city.getMoney();
        double netIncome = getNetIncome();

        // Budget critique
        if (money < 5000) {
            recommendations.add("💰 Budget critique! Réduire les dépenses immédiatement");
        }

        // Perte d'argent
        if (netIncome < -50) {
            recommendations.add("📉 Déficit important: " + String.format("%.0f €/h", netIncome));
            recommendations.add("   → Augmenter production ou réduire maintenance");
        }

        // Centrales non rentables
        long expensivePlants = city.getPowerPlants().stream()
                .filter(p -> p.getHourlyCost() > p.getCurrentProduction() * electricityPricePerKWh)
                .count();
        if (expensivePlants > 0) {
            recommendations.add("⚠️ " + expensivePlants +
                    " centrale(s) coûtent plus qu'elles ne rapportent");
        }

        // Budget excédentaire
        if (money > 100000 && netIncome > 1000) {
            recommendations.add("💎 Budget excellent! Envisager des investissements");
        }

        // Manque de diversification
        if (city.getPowerPlants().size() < 3 && city.getPopulation() > 500) {
            recommendations.add("🏭 Diversifier les sources d'énergie");
        }

        return recommendations;
    }

    /**
     * Génère un rapport mensuel
     */
    private void generateMonthlyReport() {
        System.out.println("\n=== RAPPORT MENSUEL ===");
        System.out.println("Revenus: " + String.format("%.0f €", monthlyRevenue));
        System.out.println("Dépenses: " + String.format("%.0f €", monthlyExpenses));
        System.out.println("Bilan: " + String.format("%.0f €", getMonthlyNetIncome()));
        System.out.println("======================\n");
    }

    /**
     * Calcule le retour sur investissement d'un bâtiment
     */
    public double calculateROI(Building building) {
        double cost = building.getConstructionCost();
        double hourlyProfit = 0;

        if (building instanceof PowerPlant plant) {
            double revenue = plant.getCurrentProduction() * electricityPricePerKWh;
            double costs = plant.getHourlyCost();
            hourlyProfit = revenue - costs;
        } else if (building instanceof Infrastructure infra) {
            hourlyProfit = infra.getHourlyRevenue() - infra.getMaintenanceCost();
        }

        if (hourlyProfit <= 0)
            return -1; // Jamais rentabilisé

        return cost / hourlyProfit; // Heures pour rentabiliser
    }

    /**
     * Ajuste le prix de l'électricité
     */
    public void adjustElectricityPrice(double newPrice) {
        this.electricityPricePerKWh = Math.max(0.05, Math.min(0.50, newPrice));
        recordTransaction("Ajustement prix électricité", 0, TransactionType.ADJUSTMENT);
    }

    /**
     * Accorde un prêt d'urgence
     */
    public boolean grantEmergencyLoan() {
        if (city.getMoney() < -10000)
            return false; // Dette trop importante

        double loanAmount = 20000;
        recordTransaction("Prêt d'urgence", loanAmount, TransactionType.LOAN);
        return true;
    }

    // Getters
    public double getElectricityPricePerKWh() {
        return electricityPricePerKWh;
    }

    public double getTaxRate() {
        return taxRate;
    }

    public double getInflationRate() {
        return inflationRate;
    }

    public double getMonthlyRevenue() {
        return monthlyRevenue;
    }

    public double getMonthlyExpenses() {
        return monthlyExpenses;
    }

    public List<Transaction> getRecentTransactions() {
        return new ArrayList<>(recentTransactions);
    }

    public double getTotalRevenueAllTime() {
        return totalRevenueAllTime;
    }

    public double getTotalExpensesAllTime() {
        return totalExpensesAllTime;
    }
}

/**
 * Représente une transaction financière
 */
class Transaction {
    private java.time.LocalDateTime timestamp;
    private String description;
    private double amount;
    private TransactionType type;

    public Transaction(java.time.LocalDateTime timestamp, String description,
            double amount, TransactionType type) {
        this.timestamp = timestamp;
        this.description = description;
        this.amount = amount;
        this.type = type;
    }

    public java.time.LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }
}

/**
 * Types de transactions
 */
enum TransactionType {
    REVENUE, EXPENSE, CONSTRUCTION, UPGRADE, MAINTENANCE, LOAN, ADJUSTMENT
}

/**
 * Santé financière de la ville
 */
