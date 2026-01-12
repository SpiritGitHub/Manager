package org.td.view;

import org.td.controller.GameController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import org.td.model.entities.*;
import org.td.utils.*;

/**
 * Panneau simple pour lister et gérer tous les bâtiments construits
 */
public class BuildingListPanel {
    private GameController controller;
    private ScrollPane view;
    private VBox contentBox;

    public BuildingListPanel(GameController controller) {
        this.controller = controller;
        createView();
        startUpdateTimer();
    }

    private void createView() {
        contentBox = new VBox(10);
        contentBox.setPadding(new Insets(15));

        view = new ScrollPane(contentBox);
        view.setFitToWidth(true);
        view.setPrefWidth(GameConfig.SIDEBAR_WIDTH);
        view.setStyle(UIStyles.PANEL_DARK);

        refreshBuildingList();
    }

    private void refreshBuildingList() {
        contentBox.getChildren().clear();

        // Titre
        Label title = new Label("🔧 Gestion des Bâtiments");
        title.setStyle(UIStyles.LABEL_TITLE);
        contentBox.getChildren().add(title);

        var buildings = controller.getBuildingController().getAllBuildings();

        if (buildings.isEmpty()) {
            Label empty = new Label("Aucun bâtiment construit");
            empty.setStyle(UIStyles.LABEL_NORMAL);
            contentBox.getChildren().add(empty);
            return;
        }

        // Séparer par type
        var powerPlants = buildings.stream()
                .filter(b -> b instanceof PowerPlant)
                .map(b -> (PowerPlant) b)
                .toList();

        var infrastructures = buildings.stream()
                .filter(b -> b instanceof Infrastructure)
                .map(b -> (Infrastructure) b)
                .toList();

        // Section Centrales
        if (!powerPlants.isEmpty()) {
            Label powerHeader = new Label("⚡ Centrales (" + powerPlants.size() + ")");
            powerHeader.setStyle(UIStyles.LABEL_SUBTITLE);
            contentBox.getChildren().add(powerHeader);

            for (PowerPlant plant : powerPlants) {
                contentBox.getChildren().add(createBuildingCard(plant));
            }
        }

        // Section Infrastructures
        if (!infrastructures.isEmpty()) {
            Label infraHeader = new Label("🏢 Infrastructures (" + infrastructures.size() + ")");
            infraHeader.setStyle(UIStyles.LABEL_SUBTITLE);
            contentBox.getChildren().add(infraHeader);

            for (Infrastructure infra : infrastructures) {
                contentBox.getChildren().add(createBuildingCard(infra));
            }
        }
    }

    private VBox createBuildingCard(Building building) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(10));
        card.setStyle(UIStyles.CARD);

        // Header: Icon + Name + Level
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        String icon = "🏗";
        String name = building.getType();

        if (building instanceof PowerPlant plant) {
            icon = plant.getPlantType().getIcon();
            name = plant.getPlantType().getDisplayName();
        } else if (building instanceof Infrastructure infra) {
            icon = infra.getInfrastructureType().getIcon();
            name = infra.getInfrastructureType().getDisplayName();
        }

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(18));

        VBox nameBox = new VBox(2);
        Label nameLabel = new Label(name);
        nameLabel.setStyle(UIStyles.LABEL_SUBTITLE);
        Label levelLabel = new Label("Niveau " + building.getLevel());
        levelLabel.setStyle(UIStyles.LABEL_SMALL);
        nameBox.getChildren().addAll(nameLabel, levelLabel);

        header.getChildren().addAll(iconLabel, nameBox);

        // Bouton d'amélioration
        Button upgradeBtn = new Button();
        upgradeBtn.setMaxWidth(Double.MAX_VALUE);

        boolean canUpgrade = building.canUpgrade();
        double cost = canUpgrade ? building.getUpgradeCost() : 0;
        boolean canAfford = controller.getCity().canAfford(cost);

        if (!canUpgrade) {
            upgradeBtn.setText("✓ Niveau MAX");
            upgradeBtn.setDisable(true);
            upgradeBtn.setStyle(UIStyles.BUTTON_PRIMARY);
        } else if (!canAfford) {
            upgradeBtn.setText(String.format("⬆️ Améliorer (%.0f coins)", cost));
            upgradeBtn.setDisable(true);
            upgradeBtn.setStyle(UIStyles.BUTTON_PRIMARY);
        } else {
            upgradeBtn.setText(String.format("⬆️ Améliorer (%.0f coins)", cost));
            upgradeBtn.setDisable(false);
            upgradeBtn.setStyle(UIStyles.BUTTON_SUCCESS);
        }

        upgradeBtn.setOnAction(e -> {
            BuildingUpgradeDialog.show(building, controller);
            javafx.application.Platform.runLater(this::refreshBuildingList);
        });

        card.getChildren().addAll(header, upgradeBtn);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(UIStyles.CARD_HOVER));
        card.setOnMouseExited(e -> card.setStyle(UIStyles.CARD));

        return card;
    }

    private void startUpdateTimer() {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        javafx.util.Duration.seconds(5),
                        e -> refreshBuildingList()));
        timeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        timeline.play();
    }

    public ScrollPane getView() {
        return view;
    }
}
