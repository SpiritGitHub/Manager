package org.td.view;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.td.controller.MenuController;
import org.td.utils.GameConfig;
import org.td.utils.UIColors;
import org.td.utils.UIStyles;

/**
 * Interface d'accueil du jeu
 */
public class WelcomeView {

    private final Stage stage;
    private final MenuController controller;

    public WelcomeView(Stage stage) {
        this.stage = stage;
        this.controller = new MenuController(stage);
    }

    public void show() {
        VBox root = new VBox(25);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: " + UIColors.toCss(UIColors.BACKGROUND_DARK) + ";");

        // Titre
        Label titleLabel = new Label("ÉnergiVille");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        titleLabel.setTextFill(Color.web("#e2e8f0"));

        // Effet d'ombre sur le titre
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(3.0);
        dropShadow.setOffsetY(3.0);
        dropShadow.setColor(Color.color(0, 0, 0, 0.5));
        titleLabel.setEffect(dropShadow);

        // Sous-titre
        Label subtitleLabel = new Label("Tycoon Énergétique");
        subtitleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 24));
        subtitleLabel.setTextFill(Color.web("#94a3b8"));

        // Boutons
        VBox buttonsBox = new VBox(15);
        buttonsBox.setAlignment(Pos.CENTER);

        Button btnNewGame = createMenuButton("Nouvelle Partie", "#0f3460");
        btnNewGame.setOnAction(e -> controller.startNewGame());

        Button btnLoadGame = createMenuButton("Charger une Partie", "#16213e");
        btnLoadGame.setDisable(true); // À implémenter plus tard

        Button btnQuit = createMenuButton("Quitter", "#ef4444");
        btnQuit.setOnAction(e -> controller.quitGame());

        buttonsBox.getChildren().addAll(btnNewGame, btnLoadGame, btnQuit);

        // Assemblage
        root.getChildren().addAll(titleLabel, subtitleLabel, buttonsBox);

        // Scène
        Scene scene = new Scene(root, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        stage.setScene(scene);
        stage.setTitle("ÉnergiVille - Menu Principal");
        stage.show();
    }

    private Button createMenuButton(String text, String colorHex) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color: " + colorHex + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-min-width: 250px; " +
                        "-fx-padding: 12 20; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;");

        // Effet hover simple
        btn.setOnMouseEntered(e -> btn.setOpacity(0.9));
        btn.setOnMouseExited(e -> btn.setOpacity(1.0));

        return btn;
    }
}
