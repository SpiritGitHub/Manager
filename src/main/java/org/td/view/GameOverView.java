package org.td.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.td.controller.GameController;
import org.td.utils.UIColors;

/**
 * Overlay moderne de Game Over
 */
public class GameOverView extends StackPane {

    private VBox contentBox;
    private Label reasonLabel;
    private Label scoreLabel;
    private GameController controller;

    public GameOverView(GameController controller) {
        this.controller = controller;
        createView();
        setVisible(false); // Caché par défaut
    }

    private void createView() {
        // Fond sombre semi-transparent avec flou
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");

        contentBox = new VBox(20);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setMaxWidth(600);
        contentBox.setPadding(new javafx.geometry.Insets(40));
        contentBox.setStyle("-fx-background-color: #1f2937; " +
                "-fx-background-radius: 20; " +
                "-fx-border-color: #ef4444; " +
                "-fx-border-width: 2; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(239, 68, 68, 0.4), 30, 0, 0, 0);");

        // Titre
        Label titleLabel = new Label("GAME OVER");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 48));
        titleLabel.setTextFill(Color.web("#ef4444"));
        titleLabel.setEffect(new DropShadow(10, Color.BLACK));

        // Icône "Sad Face" vectorielle (garantie visible)
        StackPane iconPane = new StackPane();
        javafx.scene.shape.Circle face = new javafx.scene.shape.Circle(50, Color.web("#fbbf24")); // Jaune foncé
        face.setStroke(Color.web("#d97706"));
        face.setStrokeWidth(4);

        javafx.scene.shape.Circle leftEye = new javafx.scene.shape.Circle(8, Color.web("#92400e"));
        leftEye.setTranslateX(-20);
        leftEye.setTranslateY(-10);

        javafx.scene.shape.Circle rightEye = new javafx.scene.shape.Circle(8, Color.web("#92400e"));
        rightEye.setTranslateX(20);
        rightEye.setTranslateY(-10);

        javafx.scene.shape.Arc mouth = new javafx.scene.shape.Arc(0, 0, 25, 15, 0, 180); // Arc inversé pour "triste"
        mouth.setType(javafx.scene.shape.ArcType.OPEN);
        mouth.setStroke(Color.web("#92400e"));
        mouth.setStrokeWidth(4);
        mouth.setFill(null);
        mouth.setTranslateY(20);

        iconPane.getChildren().addAll(face, leftEye, rightEye, mouth);
        iconPane.setEffect(new DropShadow(10, Color.BLACK));

        // Raison
        reasonLabel = new Label();
        reasonLabel.setFont(Font.font("Segoe UI", 18));
        reasonLabel.setTextFill(Color.WHITE);
        reasonLabel.setWrapText(true);
        reasonLabel.setTextAlignment(TextAlignment.CENTER);
        reasonLabel.setMaxWidth(500);

        // Score
        scoreLabel = new Label();
        scoreLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        scoreLabel.setTextFill(Color.web("#fef08a")); // Jaune

        // Boutons
        VBox buttonBox = new VBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button restartButton = createButton("🔄 Nouvelle Partie", "#2563eb");
        restartButton.setOnAction(e -> {
            setVisible(false); // Cacher l'overlay
            controller.restartGame();
        });

        Button quitButton = createButton("❌ Quitter le jeu", "#ef4444");
        quitButton.setOnAction(e -> System.exit(0));

        buttonBox.getChildren().addAll(restartButton, quitButton);

        contentBox.getChildren().addAll(titleLabel, iconPane, reasonLabel, scoreLabel, buttonBox);
        this.getChildren().add(contentBox);
    }

    private Button createButton(String text, String colorHex) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        btn.setStyle("-fx-background-color: " + colorHex + "; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 10; " +
                "-fx-padding: 10 30; " +
                "-fx-cursor: hand;");

        // Effet hover
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: derive(" + colorHex + ", 20%); " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 10; " +
                "-fx-padding: 10 30;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + colorHex + "; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 10; " +
                "-fx-padding: 10 30;"));
        return btn;
    }

    public void show(String reason, int score) {
        reasonLabel.setText(reason);
        scoreLabel.setText("Score Final: " + score);
        setVisible(true);
        toFront();
    }
}
