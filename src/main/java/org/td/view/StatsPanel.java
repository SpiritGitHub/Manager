package org.td.view;

import org.td.controller.GameController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.td.utils.*;

/**
 * Panneau de statistiques en haut de l'écran
 * Affiche argent, bonheur, population, énergie, temps
 */
public class StatsPanel {
    private GameController controller;
    private HBox view;

    // Boxes pour les valeurs
    private VBox levelBox;
    private VBox moneyBox;
    private VBox popBox;
    private VBox happinessBox;
    private VBox energyBox;
    private VBox timeBox;

    public StatsPanel(GameController controller) {
        this.controller = controller;
        createView();
        bindProperties();
    }

    private void createView() {
        view = new HBox(20);
        view.setPadding(new Insets(15, 20, 15, 20));
        view.setAlignment(Pos.CENTER_LEFT);
        view.setStyle(
                "-fx-background-color: " + UIColors.toCss(UIColors.BACKGROUND_LIGHT) + "; " +
                        "-fx-border-color: " + UIColors.toCss(UIColors.PRIMARY) + "; " +
                        "-fx-border-width: 0 0 2 0;");

        // Level
        levelBox = createStatBox("🏙️", "Niveau", "1");

        // Money
        moneyBox = createStatBox("💰", "Budget", "0 €");

        // Population
        popBox = createStatBox("👥", "Population", "0");

        // Happiness
        happinessBox = createStatBox("😊", "Satisfaction", "0%");

        // Energy
        energyBox = createStatBox("⚡", "Énergie", "0/0 kWh");

        // Time
        timeBox = createStatBox("📅", "Date", "01/01");

        // Spacer pour pousser timeBox à droite
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        view.getChildren().addAll(
                levelBox,
                createSeparator(),
                moneyBox,
                createSeparator(),
                popBox,
                createSeparator(),
                happinessBox,
                createSeparator(),
                energyBox,
                spacer,
                timeBox);
    }

    private VBox createStatBox(String icon, String title, String initialValue) {
        VBox box = new VBox(3);
        box.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(icon + " " + title);
        titleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        titleLabel.setTextFill(UIColors.TEXT_SECONDARY);

        Label valueLabel = new Label(initialValue);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        valueLabel.setTextFill(UIColors.TEXT_PRIMARY);

        box.getChildren().addAll(titleLabel, valueLabel);
        box.setUserData(valueLabel); // Store label for updates
        return box;
    }

    private Region createSeparator() {
        Region separator = new Region();
        separator.setPrefWidth(1);
        separator.setPrefHeight(40);
        separator.setStyle("-fx-background-color: " + UIColors.toCss(UIColors.GRID_LINE));
        return separator;
    }

    private void bindProperties() {
        // Level
        controller.cityLevelProperty().addListener((obs, oldVal, newVal) -> {
            Label label = (Label) levelBox.getUserData();
            label.setText(String.valueOf(newVal.intValue()));
        });

        // Money
        controller.moneyProperty().addListener((obs, oldVal, newVal) -> {
            Label label = (Label) moneyBox.getUserData();
            label.setText(String.format("%.0f €", newVal.doubleValue()));

            // Color based on value
            if (newVal.doubleValue() < 0) {
                label.setTextFill(UIColors.ERROR);
            } else if (newVal.doubleValue() < 10000) {
                label.setTextFill(UIColors.WARNING);
            } else {
                label.setTextFill(UIColors.SUCCESS);
            }
        });

        // Population
        controller.populationProperty().addListener((obs, oldVal, newVal) -> {
            Label label = (Label) popBox.getUserData();
            label.setText(String.format("%,d", newVal.intValue()));
        });

        // Happiness
        controller.happinessProperty().addListener((obs, oldVal, newVal) -> {
            Label label = (Label) happinessBox.getUserData();
            double happiness = newVal.doubleValue();
            label.setText(String.format("%.0f%%", happiness));
            label.setTextFill(UIColors.getHappinessColor(happiness));
        });

        // Energy
        controller.energyProductionProperty().addListener((obs, oldVal, newVal) -> updateEnergy());
        controller.energyDemandProperty().addListener((obs, oldVal, newVal) -> updateEnergy());

        // Time
        controller.timeProperty().addListener((obs, oldVal, newVal) -> {
            Label label = (Label) timeBox.getUserData();
            label.setText(newVal);
        });
    }

    private void updateEnergy() {
        Label label = (Label) energyBox.getUserData();
        double production = controller.energyProductionProperty().get();
        double demand = controller.energyDemandProperty().get();

        label.setText(String.format("%.0f/%.0f kWh", production, demand));

        double ratio = demand > 0 ? production / demand : 1.0;
        label.setTextFill(UIColors.getEnergyColor(ratio));
    }

    public HBox getView() {
        return view;
    }
}