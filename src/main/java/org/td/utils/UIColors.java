package org.td.utils;

import javafx.scene.paint.Color;

/**
 * Palette de couleurs pour l'interface
 * Design moderne avec thème sombre (Slate/Teal)
 */
public class UIColors {

    // === FOND ===
    public static final Color BACKGROUND_DARK = Color.web("#0f172a"); // Slate 900
    public static final Color BACKGROUND_MEDIUM = Color.web("#1e293b"); // Slate 800
    public static final Color BACKGROUND_LIGHT = Color.web("#334155"); // Slate 700

    // === ACCENTS ===
    public static final Color PRIMARY = Color.web("#2563eb"); // Blue 600
    public static final Color PRIMARY_LIGHT = Color.web("#3b82f6"); // Blue 500
    public static final Color SECONDARY = Color.web("#7c3aed"); // Violet 600

    // === TEXTE ===
    public static final Color TEXT_PRIMARY = Color.web("#e2e8f0");
    public static final Color TEXT_SECONDARY = Color.web("#94a3b8");
    public static final Color TEXT_DISABLED = Color.web("#64748b");

    // === STATUT ===
    public static final Color SUCCESS = Color.web("#10b981");
    public static final Color WARNING = Color.web("#f59e0b");
    public static final Color ERROR = Color.web("#ef4444");
    public static final Color INFO = Color.web("#3b82f6");

    // === BÂTIMENTS ===
    public static final Color RESIDENCE_BASIC = Color.web("#64748b");
    public static final Color RESIDENCE_MEDIUM = Color.web("#3b82f6");
    public static final Color RESIDENCE_ADVANCED = Color.web("#8b5cf6");

    public static final Color PLANT_COAL = Color.web("#78716c");
    public static final Color PLANT_SOLAR = Color.web("#fbbf24");
    public static final Color PLANT_WIND = Color.web("#38bdf8");
    public static final Color PLANT_NUCLEAR = Color.web("#22c55e");

    public static final Color INFRA_COMMERCIAL = Color.web("#8b5cf6");
    public static final Color INFRA_ENTERTAINMENT = Color.web("#ec4899");
    public static final Color INFRA_PARK = Color.web("#10b981");
    public static final Color INFRA_PUBLIC = Color.web("#3b82f6");
    public static final Color INFRA_SECURITY = Color.web("#ef4444");

    // === GRILLE ===
    public static final Color GRID_LINE = Color.web("#1a1f2e");
    public static final Color GRID_HOVER = Color.web("#2a3f5e");

    // === ÉNERGIE ===
    public static final Color ENERGY_HIGH = Color.web("#10b981");
    public static final Color ENERGY_MEDIUM = Color.web("#f59e0b");
    public static final Color ENERGY_LOW = Color.web("#ef4444");

    // === BONHEUR ===
    public static final Color HAPPINESS_HIGH = Color.web("#10b981");
    public static final Color HAPPINESS_MEDIUM = Color.web("#f59e0b");
    public static final Color HAPPINESS_LOW = Color.web("#ef4444");

    // === EFFETS ===
    public static final Color GLOW = Color.web("#60a5fa");
    public static final Color SHADOW = Color.web("#000000");
    public static final Color HIGHLIGHT = Color.web("#ffffff");

    /**
     * Retourne une couleur avec opacité
     */
    public static Color withOpacity(Color color, double opacity) {
        return new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                opacity);
    }

    /**
     * Retourne une couleur selon le pourcentage (0-100)
     */
    public static Color getColorForPercentage(double percentage) {
        if (percentage >= 70)
            return SUCCESS;
        if (percentage >= 40)
            return WARNING;
        return ERROR;
    }

    /**
     * Retourne une couleur selon niveau énergie
     */
    public static Color getEnergyColor(double ratio) {
        if (ratio >= 1.0)
            return ENERGY_HIGH;
        if (ratio >= 0.7)
            return ENERGY_MEDIUM;
        return ENERGY_LOW;
    }

    /**
     * Retourne une couleur selon bonheur
     */
    public static Color getHappinessColor(double happiness) {
        if (happiness >= 70)
            return HAPPINESS_HIGH;
        if (happiness >= 40)
            return HAPPINESS_MEDIUM;
        return HAPPINESS_LOW;
    }

    /**
     * Convertit Color en string CSS
     */
    public static String toCss(Color color) {
        return String.format("rgba(%d,%d,%d,%.2f)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                color.getOpacity());
    }

    private UIColors() {
    } // Empêche l'instanciation
}