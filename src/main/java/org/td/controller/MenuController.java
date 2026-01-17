package org.td.controller;

import javafx.stage.Stage;
import org.td.view.MainView;

/**
 * Contrôleur pour le menu principal
 */
public class MenuController {

    private final Stage stage;

    public MenuController(Stage stage) {
        this.stage = stage;
    }

    /**
     * Lance une nouvelle partie
     */
    public void startNewGame() {
        try {
            // Lancer la vue principale du jeu
            MainView gameView = new MainView();
            // Nous devons appeler start() manuellement car MainView est une Application
            // Mais ici nous voulons juste changer la scène sur le stage existant
            gameView.start(stage);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du lancement du jeu");
        }
    }

    /**
     * Quitte l'application
     */
    public void quitGame() {
        System.exit(0);
    }
}
