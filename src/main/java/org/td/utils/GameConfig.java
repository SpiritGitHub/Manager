package org.td.utils;

/**
 * Configuration globale du jeu
 */
public class GameConfig {
    // === FEN ÊTRE ===
    public static final int WINDOW_WIDTH = 1024;
    public static final int WINDOW_HEIGHT = 768;

    // === CARTE ===
    // Carte réduite pour être entièrement visible (Window - Sidebar - marges)
    public static final int CANVAS_WIDTH = 680; // 1024 - 300 (sidebar) - 44 (marges)
    public static final int CANVAS_HEIGHT = 640; // 768 - 100 (stats) - 28 (marges)
    public static final int CELL_SIZE = 40;

    // === GRILLE DE JEU (taille basée sur niveau ville) ===
    public static final int BASE_GRID_SIZE = 10; // Grille de base (Level 1)
    public static final int GRID_EXPANSION_PER_LEVEL = 5; // Cases ajoutées par niveau

    // === PERFORMANCE ===
    public static final int TARGET_FPS = 60;
    public static final long FRAME_TIME_MS = 1000 / TARGET_FPS;
    public static final boolean SHOW_GRID = true;
    public static final boolean SHOW_DEBUG_INFO = false;

    // === GAMEPLAY ===
    public static final double STARTING_MONEY_EASY = 100000;
    public static final double STARTING_MONEY_NORMAL = 75000;
    public static final double STARTING_MONEY_HARD = 50000;

    public static final int GAME_OVER_HAPPINESS_THRESHOLD = 5;
    public static final double GAME_OVER_DEBT_THRESHOLD = -50000;

    // === ÉVOLUTION VILLE ===
    // Seuils pour passer au niveau suivant
    public static final int[] LEVEL_UP_POPULATION = { 0, 200, 500, 1000, 2000, 4000, 8000, 15000, 30000, 50000 };
    public static final double LEVEL_UP_HAPPINESS_MIN = 50.0; // Bonheur minimum pour level up

    // === UI ===
    public static final int SIDEBAR_WIDTH = 300;
    public static final int TOOLBAR_HEIGHT = 80;
    public static final int STATS_PANEL_HEIGHT = 100;

    // === ANIMATIONS ===
    public static final int CONSTRUCTION_ANIMATION_DURATION = 300; // ms
    public static final int NOTIFICATION_DURATION = 3000; // ms
    public static final int TOOLTIP_DELAY = 500; // ms

    // === SONS (si implémenté) ===
    public static final boolean SOUND_ENABLED = true;
    public static final double MASTER_VOLUME = 0.7;
    public static final double MUSIC_VOLUME = 0.5;
    public static final double SFX_VOLUME = 0.8;

    // === SAUVEGARDE ===
    public static final String SAVE_DIRECTORY = "saves/";
    public static final String SAVE_EXTENSION = ".energyville";
    public static final int MAX_SAVE_SLOTS = 10;

    // === GRAPHIQUES ===
    public static final boolean ANTIALIASING = true;
    public static final boolean SHOW_SHADOWS = true;
    public static final boolean PARTICLE_EFFECTS = true;

    /**
     * Retourne la taille de grille pour un niveau de ville donné
     */
    public static int getGridSizeForLevel(int cityLevel) {
        return BASE_GRID_SIZE + (cityLevel - 1) * GRID_EXPANSION_PER_LEVEL;
    }

    /**
     * Retourne le seuil de population pour atteindre le niveau suivant
     */
    public static int getPopulationThresholdForLevel(int targetLevel) {
        if (targetLevel <= 0 || targetLevel >= LEVEL_UP_POPULATION.length) {
            return Integer.MAX_VALUE;
        }
        return LEVEL_UP_POPULATION[targetLevel];
    }

    private GameConfig() {
    } // Empêche l'instanciation
}