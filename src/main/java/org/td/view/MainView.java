package org.td.view;

import org.td.controller.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.td.model.enums.EventType;
import org.td.utils.*;

/**
 * Vue principale de l'application
 * Assemble tous les composants de l'interface
 */
public class MainView extends Application {

    private GameController gameController;
    private Stage primaryStage;

    // Composants de l'interface
    private CityMapView cityMapView;
    private StatsPanel statsPanel;
    private ControlPanel controlPanel;
    private BuildingListPanel buildingListPanel;
    private NotificationPanel notificationPanel;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        // Créer nouveau jeu
        gameController = new GameController("ÉnergiVille", "Joueur", 2);

        // Initialiser l'interface
        BorderPane root = createMainLayout();

        // Créer la scène
        Scene scene = new Scene(root, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);

        stage.setScene(scene);
        stage.setTitle("ÉnergiVille - Tycoon Énergétique");
        stage.show();

        gameController.addEventListener((message, type) -> {
            notificationPanel.showNotification(message, type);
        });
        gameController.startGame();
    }

    /**
     * Crée le layout principal
     */
    private BorderPane createMainLayout() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + UIColors.toCss(UIColors.BACKGROUND_DARK) + ";");

        statsPanel = new StatsPanel(gameController);
        root.setTop(statsPanel.getView());

        cityMapView = new CityMapView(gameController);
        notificationPanel = new NotificationPanel();

        StackPane centerStack = new StackPane(
                cityMapView.getView(),
                notificationPanel.getView());

        root.setCenter(centerStack);

        controlPanel = new ControlPanel(gameController, cityMapView);
        root.setBottom(controlPanel.getView());

        buildingListPanel = new BuildingListPanel(gameController);
        root.setRight(buildingListPanel.getView());

        return root;
    }

    /**
     * Gère la fermeture de l'application
     */
    private void handleClose() {
        // Demander confirmation si partie en cours
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Quitter");
        alert.setHeaderText("Voulez-vous sauvegarder avant de quitter ?");
        alert.setContentText("Votre progression sera perdue si vous ne sauvegardez pas.");

        javafx.scene.control.ButtonType btnSave = new javafx.scene.control.ButtonType("Sauvegarder");
        javafx.scene.control.ButtonType btnQuit = new javafx.scene.control.ButtonType("Quitter");
        javafx.scene.control.ButtonType btnCancel = new javafx.scene.control.ButtonType("Annuler");

        alert.getButtonTypes().setAll(btnSave, btnQuit, btnCancel);

        alert.showAndWait().ifPresent(response -> {
            if (response == btnSave) {
                saveAndQuit();
            } else if (response == btnQuit) {
                System.exit(0);
            }
            // Si Cancel, ne fait rien (reste ouvert)
        });
    }

    /**
     * Sauvegarde et quitte
     */
    private void saveAndQuit() {
        String filename = GameConfig.SAVE_DIRECTORY + "autosave" + GameConfig.SAVE_EXTENSION;
        if (gameController.saveGame(filename)) {
            System.out.println("Partie sauvegardée avec succès");
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

/**
 * Panneau de notifications (overlay)
 */
class NotificationPanel {
    private VBox container;

    public NotificationPanel() {
        container = new VBox(10);
        container.setStyle("-fx-background-color: transparent;");
        container.setPadding(new Insets(20));
        container.setPickOnBounds(false);
        container.setMouseTransparent(true);
        StackPane.setAlignment(container, javafx.geometry.Pos.TOP_RIGHT);
    }

    public void showNotification(String message, EventType type) {
        javafx.scene.control.Label notification = new javafx.scene.control.Label(message);
        notification.setWrapText(true);
        notification.setMaxWidth(350);
        notification.setPadding(new Insets(12, 16, 12, 16));
        notification.setStyle(getNotificationStyle(type));

        // Effet d'apparition
        notification.setOpacity(0);
        javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(300), notification);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        container.getChildren().add(0, notification);

        // Auto-suppression après 3 secondes
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                javafx.util.Duration.millis(GameConfig.NOTIFICATION_DURATION));
        pause.setOnFinished(e -> {
            javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(300), notification);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> container.getChildren().remove(notification));
            fadeOut.play();
        });
        pause.play();

        // Limiter le nombre de notifications
        if (container.getChildren().size() > 5) {
            container.getChildren().remove(container.getChildren().size() - 1);
        }
    }

    private String getNotificationStyle(EventType type) {
        String baseStyle = "-fx-background-color: %s; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 4); " +
                "-fx-font-size: 13px; " +
                "-fx-font-weight: bold;";

        String color = switch (type) {
            case INFO -> "#3b82f6";
            case SUCCESS -> "#10b981";
            case WARNING -> "#f59e0b";
            case ERROR -> "#ef4444";
        };

        return String.format(baseStyle, color);
    }

    public VBox getView() {
        return container;
    }
}
