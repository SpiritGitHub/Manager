package org.td.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.td.controller.GameController;
import org.td.model.entities.City;
import org.td.model.enums.GameSpeed;
import org.td.utils.UIColors;
import org.td.utils.UIStyles;

public class ControlPanel {
    private GameController gameController;
    private CityMapView cityMapView;
    private HBox view;

    // Contrôles
    private ComboBox<GameSpeed> speedCombo;
    private Button pauseButton;

    public ControlPanel(GameController gameController, CityMapView cityMapView) {
        this.gameController = gameController;
        this.cityMapView = cityMapView;
        createView();
    }

    private void createView() {
        view = new HBox(15);
        view.setPadding(new Insets(15, 20, 15, 20));
        view.setAlignment(Pos.CENTER_LEFT);
        view.setStyle(
                "-fx-background-color: " + UIColors.toCss(UIColors.BACKGROUND_LIGHT) + "; " +
                        "-fx-border-color: " + UIColors.toCss(UIColors.PRIMARY) + "; " +
                        "-fx-border-width: 2 0 0 0;");

        // Section Temps
        VBox timeSection = createTimeSection();

        // Section Actions
        VBox actionsSection = createActionsSection();

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        view.getChildren().addAll(
                timeSection,
                spacer,
                actionsSection);
    }

    private VBox createTimeSection() {
        VBox section = new VBox(8);
        section.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("⏱️ Temps");
        title.setStyle(UIStyles.LABEL_SUBTITLE);

        HBox controlsBox = new HBox(8);
        controlsBox.setAlignment(Pos.CENTER_LEFT);

        // Bouton Pause
        pauseButton = new Button("⏸ Pause");
        pauseButton.setStyle(UIStyles.BUTTON_WARNING);
        pauseButton.setOnAction(e -> togglePause());

        // ComboBox vitesse
        speedCombo = new ComboBox<>();
        speedCombo.getItems().addAll(GameSpeed.values());
        speedCombo.setValue(GameSpeed.NORMAL);
        speedCombo.setStyle(UIStyles.COMBO_BOX);
        speedCombo.setOnAction(e -> gameController.getTimeController().setSpeed(speedCombo.getValue()));

        // Boutons vitesse rapide
        Button slowerBtn = new Button("◀");
        slowerBtn.setStyle(UIStyles.BUTTON_PRIMARY);
        slowerBtn.setOnAction(e -> {
            gameController.getTimeController().decreaseSpeed();
            speedCombo.setValue(gameController.getTimeController().getCurrentSpeed());
        });

        Button fasterBtn = new Button("▶");
        fasterBtn.setStyle(UIStyles.BUTTON_PRIMARY);
        fasterBtn.setOnAction(e -> {
            gameController.getTimeController().increaseSpeed();
            speedCombo.setValue(gameController.getTimeController().getCurrentSpeed());
        });

        controlsBox.getChildren().addAll(pauseButton, slowerBtn, speedCombo, fasterBtn);

        section.getChildren().addAll(title, controlsBox);
        return section;
    }

    private VBox createActionsSection() {
        VBox section = new VBox(8);
        section.setAlignment(Pos.CENTER_RIGHT);

        HBox buttonsBox = new HBox(8);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);

        // Bouton Maintenance globale
        Button maintenanceBtn = new Button("🔧 Maintenance");
        maintenanceBtn.setStyle(UIStyles.BUTTON_SUCCESS);
        maintenanceBtn.setOnAction(e -> gameController.performGlobalMaintenance());

        // Bouton Statistiques
        Button statsBtn = new Button("📊 Stats");
        statsBtn.setStyle(UIStyles.BUTTON_PRIMARY);
        statsBtn.setOnAction(e -> showStatsDialog());

        // Bouton Sauvegarder
        Button saveBtn = new Button("💾 Sauvegarder");
        saveBtn.setStyle(UIStyles.BUTTON_PRIMARY);
        saveBtn.setOnAction(e -> saveGame());

        // Bouton Menu
        Button menuBtn = new Button("☰ Menu");
        menuBtn.setStyle(UIStyles.BUTTON_PRIMARY);
        menuBtn.setOnAction(e -> showMenu());

        buttonsBox.getChildren().addAll(maintenanceBtn, statsBtn, saveBtn, menuBtn);

        section.getChildren().add(buttonsBox);
        return section;
    }

    private void togglePause() {
        gameController.getTimeController().togglePause();
        boolean paused = gameController.getTimeController().isPaused();
        pauseButton.setText(paused ? "▶ Reprendre" : "⏸ Pause");
    }

    private void showStatsDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Rapport de Ville Complet");
        dialog.setHeaderText(null);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        dialogPane.setContent(createReportView());

        // Style du dialog
        dialogPane.setStyle("-fx-background-color: " + UIColors.toCss(UIColors.BACKGROUND_DARK) + ";");

        dialog.showAndWait();
    }

    private GridPane createReportView() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: " + UIColors.toCss(UIColors.BACKGROUND_DARK) + ";");

        // Colonne Gauche
        VBox leftColumn = new VBox(15);
        leftColumn.getChildren().addAll(
                createCitySummarySection(),
                createDemographicsSection(),
                createMonthlyRevenueSection());

        // Colonne Droite
        VBox rightColumn = new VBox(15);
        rightColumn.getChildren().addAll(
                createEnergySection(),
                createBuildingsSection(),
                createGameStatsSection());

        grid.add(leftColumn, 0, 0);
        grid.add(rightColumn, 1, 0);

        return grid;
    }

    private VBox createCitySummarySection() {
        return createSection("Résumé de la Ville",
                String.format("Nom: %s\nNiveau: %d\nPopulation: %d\nBudget: %.0f coins\nBonheur: %.1f%%",
                        gameController.getCity().getName(),
                        gameController.getCity().getLevel(),
                        gameController.getCity().getPopulation(),
                        gameController.getCity().getMoney(),
                        gameController.getCity().getHappiness()));
    }

    private VBox createDemographicsSection() {
        City city = gameController.getCity();
        long residences = city.getResidences().size();
        return createSection("Démographie",
                String.format("Résidences: %d\nCroissance: %s\nDensité moyenne: %.1f hab/résidence",
                        residences,
                        city.getHappiness() > 70 ? "Positive 📈" : "Stagnante 😐",
                        residences > 0 ? (double) city.getPopulation() / residences : 0));
    }

    private VBox createMonthlyRevenueSection() {
        City city = gameController.getCity();
        // Calculate monthly bill revenue (residents pay bills)
        double monthlyRevenue = city.getPopulation() * 50; // 50 coins per resident per month
        double estimatedMaintenance = (city.getPowerPlants().size() * 100 + city.getInfrastructures().size() * 50)
                * 720;
        return createSection("Revenus Mensuels",
                String.format(
                        "Factures résidents: %.0f coins/mois\nCoût maintenance estimé: %.0f coins/mois\nBilan mensuel estimé: %.0f coins",
                        monthlyRevenue,
                        estimatedMaintenance,
                        monthlyRevenue - estimatedMaintenance));
    }

    private VBox createEnergySection() {
        City city = gameController.getCity();
        double ratio = city.getTotalEnergyDemand() > 0 ? city.getTotalEnergyProduction() / city.getTotalEnergyDemand()
                : 1.0;
        return createSection("Énergie",
                String.format(
                        "Production: %.0f kWh\nDemande: %.0f kWh\nBilan: %.0f kWh\nCentrales: %d\nRendement: %.1f%%",
                        city.getTotalEnergyProduction(),
                        city.getTotalEnergyDemand(),
                        city.getEnergyBalance(),
                        city.getPowerPlants().size(),
                        ratio * 100));
    }

    private VBox createBuildingsSection() {
        City city = gameController.getCity();
        return createSection("Bâtiments",
                String.format("Résidences: %d\nCentrales: %d\nInfrastructures: %d\nTotal: %d",
                        city.getResidences().size(),
                        city.getPowerPlants().size(),
                        city.getInfrastructures().size(),
                        city.getResidences().size() + city.getPowerPlants().size() + city.getInfrastructures().size()));
    }

    private VBox createGameStatsSection() {
        return createSection("Statistiques de Jeu",
                String.format("Temps joué: %d h\nDate en jeu: %s\nNiveau max atteint: %d",
                        gameController.getCity().getCurrentTime().getHour(),
                        gameController.getCity().getCurrentTime().toString(),
                        gameController.getCity().getLevel()));
    }

    private VBox createSection(String title, String content) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: " + UIColors.toCss(UIColors.BACKGROUND_LIGHT) + ";" +
                "-fx-background-radius: 5;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: " + UIColors.toCss(UIColors.PRIMARY) + ";");

        Label contentLabel = new Label(content);
        contentLabel.setStyle("-fx-text-fill: " + UIColors.toCss(UIColors.TEXT_PRIMARY) + ";");

        box.getChildren().addAll(titleLabel, contentLabel);
        return box;
    }

    private void saveGame() {
        System.out.println("Sauvegarde...");
    }

    private void showMenu() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Menu");
        alert.setHeaderText("Options du jeu");
        alert.setContentText(
                "• Utilisez le menu à droite pour construire\n" +
                        "• Chaque bouton construit automatiquement\n" +
                        "• Contrôlez le temps avec les boutons");
        alert.showAndWait();
    }

    public HBox getView() {
        return view;
    }
}