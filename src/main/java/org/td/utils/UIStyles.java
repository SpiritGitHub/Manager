package org.td.utils;

/**
 * Styles CSS pour les composants JavaFX
 */
public class UIStyles {

    // === BOUTONS ===
    public static final String BUTTON_PRIMARY =
            "-fx-background-color: #0f3460; " +
                    "-fx-text-fill: #e2e8f0; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 8 16; " +
                    "-fx-background-radius: 6; " +
                    "-fx-cursor: hand; " +
                    "-fx-font-size: 13px;";

    public static final String BUTTON_PRIMARY_HOVER =
            "-fx-background-color: #1a5490; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 8 16; " +
                    "-fx-background-radius: 6; " +
                    "-fx-cursor: hand; " +
                    "-fx-font-size: 13px;";

    public static final String BUTTON_SUCCESS =
            "-fx-background-color: #10b981; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 8 16; " +
                    "-fx-background-radius: 6; " +
                    "-fx-cursor: hand;";

    public static final String BUTTON_WARNING =
            "-fx-background-color: #f59e0b; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 8 16; " +
                    "-fx-background-radius: 6; " +
                    "-fx-cursor: hand;";

    public static final String BUTTON_DANGER =
            "-fx-background-color: #ef4444; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 8 16; " +
                    "-fx-background-radius: 6; " +
                    "-fx-cursor: hand;";

    // === PANNEAUX ===
    public static final String PANEL_DARK =
            "-fx-background-color: #1a1a2e; " +
                    "-fx-background-radius: 8; " +
                    "-fx-padding: 15;";

    public static final String PANEL_MEDIUM =
            "-fx-background-color: #16213e; " +
                    "-fx-background-radius: 8; " +
                    "-fx-padding: 15;";

    public static final String PANEL_BORDERED =
            "-fx-background-color: #16213e; " +
                    "-fx-border-color: #0f3460; " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 8; " +
                    "-fx-background-radius: 8; " +
                    "-fx-padding: 15;";

    // === LABELS ===
    public static final String LABEL_TITLE =
            "-fx-font-size: 20px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-text-fill: #e2e8f0;";

    public static final String LABEL_SUBTITLE =
            "-fx-font-size: 16px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-text-fill: #94a3b8;";

    public static final String LABEL_NORMAL =
            "-fx-font-size: 13px; " +
                    "-fx-text-fill: #e2e8f0;";

    public static final String LABEL_SMALL =
            "-fx-font-size: 11px; " +
                    "-fx-text-fill: #94a3b8;";

    public static final String LABEL_VALUE =
            "-fx-font-size: 18px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-text-fill: #e2e8f0;";

    // === INPUTS ===
    public static final String TEXT_FIELD =
            "-fx-background-color: #0f3460; " +
                    "-fx-text-fill: #e2e8f0; " +
                    "-fx-prompt-text-fill: #64748b; " +
                    "-fx-padding: 8; " +
                    "-fx-background-radius: 6; " +
                    "-fx-border-color: transparent; " +
                    "-fx-font-size: 13px;";

    public static final String COMBO_BOX =
            "-fx-background-color: #0f3460; " +
                    "-fx-text-fill: #e2e8f0; " +
                    "-fx-padding: 6 12; " +
                    "-fx-background-radius: 6; " +
                    "-fx-cursor: hand;";

    // === BARRES DE PROGRESSION ===
    public static final String PROGRESS_BAR =
            "-fx-accent: #10b981; " +
                    "-fx-background-color: #1a1a2e; " +
                    "-fx-background-radius: 4;";

    public static final String PROGRESS_BAR_WARNING =
            "-fx-accent: #f59e0b; " +
                    "-fx-background-color: #1a1a2e; " +
                    "-fx-background-radius: 4;";

    public static final String PROGRESS_BAR_DANGER =
            "-fx-accent: #ef4444; " +
                    "-fx-background-color: #1a1a2e; " +
                    "-fx-background-radius: 4;";

    // === TOOLTIPS ===
    public static final String TOOLTIP =
            "-fx-background-color: #1a1a2e; " +
                    "-fx-text-fill: #e2e8f0; " +
                    "-fx-background-radius: 6; " +
                    "-fx-padding: 8 12; " +
                    "-fx-font-size: 12px; " +
                    "-fx-border-color: #0f3460; " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 6;";

    // === SCROLLBAR ===
    public static final String SCROLLBAR =
            "-fx-background-color: transparent; " +
                    "-fx-background-insets: 0; " +
                    "-fx-padding: 0;";

    // === CARTES ===
    public static final String CARD =
            "-fx-background-color: #16213e; " +
                    "-fx-background-radius: 10; " +
                    "-fx-padding: 20; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 4);";

    public static final String CARD_HOVER =
            "-fx-background-color: #1a2847; " +
                    "-fx-background-radius: 10; " +
                    "-fx-padding: 20; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 15, 0, 0, 6); " +
                    "-fx-cursor: hand;";

    /**
     * Crée un style de bouton personnalisé
     */
    public static String createButtonStyle(String bgColor, String textColor) {
        return String.format(
                "-fx-background-color: %s; " +
                        "-fx-text-fill: %s; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 8 16; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand; " +
                        "-fx-font-size: 13px;",
                bgColor, textColor
        );
    }

    /**
     * Crée un style de label personnalisé
     */
    public static String createLabelStyle(int fontSize, String color, boolean bold) {
        return String.format(
                "-fx-font-size: %dpx; " +
                        "-fx-text-fill: %s; " +
                        (bold ? "-fx-font-weight: bold;" : ""),
                fontSize, color
        );
    }

    private UIStyles() {} // Empêche l'instanciation
}