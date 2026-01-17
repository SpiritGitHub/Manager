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

        // Zone des bâtiments animés (NOUVEAU)
        javafx.scene.layout.HBox buildingsContainer = createAnimatedBuildings();

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
        root.getChildren().addAll(titleLabel, subtitleLabel, buildingsContainer, buttonsBox);

        // Scène
        Scene scene = new Scene(root, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        stage.setScene(scene);
        stage.setTitle("ÉnergiVille - Menu Principal");
        stage.show();
    }

    private javafx.scene.layout.HBox createAnimatedBuildings() {
        javafx.scene.layout.HBox container = new javafx.scene.layout.HBox(10);
        container.setAlignment(Pos.BOTTOM_CENTER);
        container.setMinHeight(120);

        // Création de quelques "bâtiments" simples
        javafx.scene.Node b1 = createBuildingShape(40, 80, Color.web("#3b82f6"));
        javafx.scene.Node b2 = createBuildingShape(50, 100, Color.web("#10b981"));
        javafx.scene.Node b3 = createBuildingShape(35, 60, Color.web("#8b5cf6"));
        javafx.scene.Node b4 = createBuildingShape(45, 90, Color.web("#f59e0b"));

        container.getChildren().addAll(b1, b2, b3, b4);

        // Ajouter une instruction visuelle
        javafx.scene.control.Tooltip.install(container,
                new javafx.scene.control.Tooltip("Cliquez pour voir le thème !"));

        return container;
    }

    private javafx.scene.Node createBuildingShape(double width, double height, Color color) {
        javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(width, height, color);
        rect.setArcWidth(5);
        rect.setArcHeight(5);
        rect.setStroke(Color.WHITE);
        rect.setStrokeWidth(2);

        // Animation "Respiration" (Translation haut/bas)
        javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(
                javafx.util.Duration.seconds(1.5), rect);
        tt.setByY(-10); // Monte de 10 pixels
        tt.setCycleCount(javafx.animation.Animation.INDEFINITE);
        tt.setAutoReverse(true);
        // Décalage aléatoire pour que tous ne bougent pas en même temps
        tt.setDelay(javafx.util.Duration.seconds(Math.random()));
        tt.play();

        // Interaction
        rect.setCursor(javafx.scene.Cursor.HAND);
        rect.setOnMouseClicked(e -> showThemePresentation());

        return rect;
    }

    private void showThemePresentation() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Présentation du Thème");
        alert.setHeaderText("ÉnergiVille : Gérez l'énergie de demain !");
        alert.setContentText(
                "Bienvenue dans ÉnergiVille !\n\n" +
                        "Dans ce jeu de gestion, vous incarnez le maire d'une ville en pleine expansion.\n" +
                        "Votre mission : Construire une ville durable en gérant l'approvisionnement énergétique.\n\n" +
                        "⚡ Construisez des centrales (Solaire, Eolien, Hydro, etc.)\n" +
                        "🏠 Alimentez les bâtiments résidentiels et industriels\n" +
                        "💰 Gérez votre budget et l'impact écologique\n\n" +
                        "Saurez-vous trouver l'équilibre entre croissance et écologie ?");
        alert.showAndWait();
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
