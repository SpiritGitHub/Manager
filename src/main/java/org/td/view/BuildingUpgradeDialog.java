package org.td.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.td.controller.BuildingController;
import org.td.controller.GameController;
import org.td.model.entities.*;
import org.td.controller.BuildResult;
import org.td.model.enums.EventType;
import org.td.utils.UIColors;

/**
 * Dialogue d'information et d'amélioration des bâtiments
 */
public class BuildingUpgradeDialog {

        public static void show(Building building, GameController controller) {
                // Create custom dialog
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Détails du Bâtiment");
                dialog.setHeaderText(null);

                // Content
                VBox content = new VBox(15);
                content.setPadding(new Insets(20));
                content.setStyle("-fx-background-color: " + UIColors.toCss(UIColors.BACKGROUND_DARK) + ";");

                // Header with icon and name
                HBox header = new HBox(10);
                header.setAlignment(Pos.CENTER_LEFT);

                String icon = building instanceof PowerPlant ? ((PowerPlant) building).getPlantType().getIcon()
                                : ((Infrastructure) building).getInfrastructureType().getIcon();

                Label iconLabel = new Label(icon);
                iconLabel.setStyle("-fx-font-size: 32px;");

                Label nameLabel = new Label(building.getType());
                nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " +
                                UIColors.toCss(UIColors.TEXT_PRIMARY) + ";");

                header.getChildren().addAll(iconLabel, nameLabel);

                // Stats grid
                GridPane statsGrid = new GridPane();
                statsGrid.setHgap(15);
                statsGrid.setVgap(10);

                int row = 0;

                // Common stats
                addStatRow(statsGrid, row++, "📍 Position",
                                String.format("(%d, %d)", building.getX(), building.getY()));
                addStatRow(statsGrid, row++, "🏆 Niveau", String.valueOf(building.getLevel()));
                addStatRow(statsGrid, row++, "💰 Coût construction",
                                String.format("%.0f coins", building.getConstructionCost()));

                // Specific stats for PowerPlant
                if (building instanceof PowerPlant plant) {
                        addStatRow(statsGrid, row++, "⚡ Production",
                                        String.format("%.0f / %.0f kWh", plant.getCurrentProduction(),
                                                        plant.getMaxProduction()));
                        addStatRow(statsGrid, row++, "🔧 Maintenance",
                                        String.format("%.0f coins/h", plant.getPlantType().getMaintenanceCost()));
                        addStatRow(statsGrid, row++, "🏭 Pollution",
                                        String.format("%.1f/10", plant.getPlantType().getPollutionLevel()));
                        addStatRow(statsGrid, row++, "⚙️ Efficacité",
                                        String.format("%.0f%%", plant.getEfficiency()));
                        addStatRow(statsGrid, row++, "📊 État",
                                        plant.isActive() ? "✅ Actif" : "❌ Inactif");
                }

                // Specific stats for Infrastructure
                if (building instanceof Infrastructure infra) {
                        addStatRow(statsGrid, row++, "😊 Bonus bonheur",
                                        String.format("+%.1f", infra.getInfrastructureType().getHappinessBonus()));
                        addStatRow(statsGrid, row++, "⚡ Consommation",
                                        String.format("%.0f kWh",
                                                        infra.getInfrastructureType().getEnergyConsumption()));
                        addStatRow(statsGrid, row++, "🔧 Entretien",
                                        String.format("%.0f coins/h",
                                                        infra.getInfrastructureType().getMaintenanceCost()));
                }

                // Upgrade info
                VBox upgradeInfo = new VBox(8);
                upgradeInfo.setPadding(new Insets(10));
                upgradeInfo.setStyle("-fx-background-color: " + UIColors.toCss(UIColors.BACKGROUND_MEDIUM) +
                                "; -fx-background-radius: 5;");

                if (building.canUpgrade()) {
                        double upgradeCost = building.getUpgradeCost();
                        Label upgradeTitle = new Label("⬆️ Amélioration disponible");
                        upgradeTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: " +
                                        UIColors.toCss(UIColors.SUCCESS) + ";");

                        Label upgradeCostLabel = new Label(String.format("Coût: %.0f coins", upgradeCost));
                        upgradeCostLabel.setStyle("-fx-text-fill: " + UIColors.toCss(UIColors.TEXT_PRIMARY) + ";");

                        Label upgradeNextLevel = new Label("Niveau suivant: " + (building.getLevel() + 1));
                        upgradeNextLevel.setStyle("-fx-text-fill: " + UIColors.toCss(UIColors.TEXT_SECONDARY) + ";");

                        if (building instanceof PowerPlant plant) {
                                double nextProduction = plant.getPlantType().getProduction(building.getLevel() + 1);
                                Label productionBoost = new Label(String.format("Production: %.0f kWh → %.0f kWh",
                                                plant.getMaxProduction(), nextProduction));
                                productionBoost.setStyle("-fx-text-fill: " + UIColors.toCss(UIColors.SUCCESS) + ";");
                                upgradeInfo.getChildren().addAll(upgradeTitle, upgradeCostLabel, upgradeNextLevel,
                                                productionBoost);
                        } else {
                                upgradeInfo.getChildren().addAll(upgradeTitle, upgradeCostLabel, upgradeNextLevel);
                        }
                } else {
                        Label maxLevel = new Label("🏆 Niveau maximum atteint!");
                        maxLevel.setStyle("-fx-font-weight: bold; -fx-text-fill: " +
                                        UIColors.toCss(UIColors.WARNING) + ";");
                        upgradeInfo.getChildren().add(maxLevel);
                }

                content.getChildren().addAll(header, new Separator(), statsGrid, upgradeInfo);
                dialog.getDialogPane().setContent(content);

                // Buttons
                ButtonType closeBtn = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
                ButtonType upgradeBtn = new ButtonType("⬆️ Améliorer", ButtonBar.ButtonData.OK_DONE);

                if (building.canUpgrade()) {
                        dialog.getDialogPane().getButtonTypes().addAll(upgradeBtn, closeBtn);
                } else {
                        dialog.getDialogPane().getButtonTypes().add(closeBtn);
                }

                // Handle upgrade
                dialog.showAndWait().ifPresent(response -> {
                        if (response == upgradeBtn && building.canUpgrade()) {
                                BuildResult result = controller.getBuildingController().upgradeBuilding(building);
                                if (result.success) {
                                        controller.sendNotification(result.message, EventType.SUCCESS);
                                        // Show updated info
                                        show(building, controller);
                                } else {
                                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                                        errorAlert.setTitle("Échec de l'amélioration");
                                        errorAlert.setHeaderText(null);
                                        errorAlert.setContentText(result.message);
                                        errorAlert.showAndWait();
                                }
                        }
                });
        }

        private static void addStatRow(GridPane grid, int row, String label, String value) {
                Label labelText = new Label(label);
                labelText.setStyle("-fx-text-fill: " + UIColors.toCss(UIColors.TEXT_SECONDARY) + ";");

                Label valueText = new Label(value);
                valueText.setStyle("-fx-font-weight: bold; -fx-text-fill: " +
                                UIColors.toCss(UIColors.TEXT_PRIMARY) + ";");

                grid.add(labelText, 0, row);
                grid.add(valueText, 1, row);
        }
}
