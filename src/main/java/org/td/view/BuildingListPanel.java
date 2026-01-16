package org.td.view;

import org.td.controller.GameController;
import org.td.controller.BuildingController; // Import manquant
import org.td.model.entities.Building; // Import manquant
import org.td.model.entities.Infrastructure;
import org.td.model.entities.PowerPlant;
import org.td.model.enums.BuildingType;
import org.td.model.enums.PowerPlantType;
import org.td.utils.GameConfig;
import org.td.utils.UIStyles;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

/**
 * Panneau latéral principal : Construction et Gestion
 */
public class BuildingListPanel {
    private GameController controller;
    private VBox mainLayout;
    private TabPane tabPane;
    
    // Contenu des onglets
    private VBox constructionContent;
    private VBox managementContent;

    public BuildingListPanel(GameController controller) {
        this.controller = controller;
        createView();
        startUpdateTimer();
    }

    private void createView() {
        mainLayout = new VBox();
        mainLayout.setPrefWidth(GameConfig.SIDEBAR_WIDTH);
        mainLayout.setStyle(UIStyles.PANEL_DARK);

        // Création du TabPane
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // --- ONGLET 1 : CONSTRUCTION 🏗️ ---
        Tab constructionTab = new Tab("Construction");
        constructionTab.setClosable(false);
        
        ScrollPane scrollConstruct = new ScrollPane();
        scrollConstruct.setFitToWidth(true);
        constructionContent = new VBox(10);
        constructionContent.setPadding(new Insets(10));
        constructionContent.setStyle(UIStyles.PANEL_DARK);
        scrollConstruct.setContent(constructionContent);
        
        constructionTab.setContent(scrollConstruct);

        // --- ONGLET 2 : GESTION 🔧 ---
        Tab managementTab = new Tab("Gestion");
        managementTab.setClosable(false);
        
        ScrollPane scrollManage = new ScrollPane();
        scrollManage.setFitToWidth(true);
        managementContent = new VBox(10);
        managementContent.setPadding(new Insets(10));
        managementContent.setStyle(UIStyles.PANEL_DARK);
        scrollManage.setContent(managementContent);
        
        managementTab.setContent(scrollManage);

        // Ajout des onglets
        tabPane.getTabs().addAll(constructionTab, managementTab);
        
        // CSS pour les onglets (simple)
        tabPane.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");

        VBox.setVgrow(tabPane, Priority.ALWAYS);
        mainLayout.getChildren().add(tabPane);

        // Initialisation
        refreshConstructionList();
        refreshBuildingList();
    }

    // ==========================================
    //           ONGLET CONSTRUCTION
    // ==========================================
    private void refreshConstructionList() {
        constructionContent.getChildren().clear();
        
        BuildingController bc = controller.getBuildingController();

        // 1. Centrales
        Label lblPlants = new Label("⚡ Centrales Électriques");
        lblPlants.setStyle(UIStyles.LABEL_SUBTITLE);
        constructionContent.getChildren().add(lblPlants);

        for (PowerPlantType type : PowerPlantType.values()) {
            if (type.isUnlockedAt(controller.getCity().getLevel())) {
                constructionContent.getChildren().add(createConstructionCard(type));
            }
        }

        // Séparateur
        Separator sep = new Separator();
        sep.setPadding(new Insets(10, 0, 10, 0));
        constructionContent.getChildren().add(sep);

        // 2. Infrastructures
        Label lblInfra = new Label("🏢 Infrastructures");
        lblInfra.setStyle(UIStyles.LABEL_SUBTITLE);
        constructionContent.getChildren().add(lblInfra);

        for (BuildingType type : BuildingType.values()) {
             if (type.isUnlockedAt(controller.getCity().getLevel())) {
                constructionContent.getChildren().add(createConstructionCard(type));
            }
        }
    }

    // Carte pour une Centrale
    private VBox createConstructionCard(PowerPlantType type) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(8));
        card.setStyle("-fx-background-color: #374151; -fx-background-radius: 5; -fx-border-color: #4b5563; -fx-border-radius: 5;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label(type.getIcon());
        icon.setFont(Font.font(24));
        Label name = new Label(type.getDisplayName());
        name.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        header.getChildren().addAll(icon, name);

        double cost = type.getConstructionCost(1);
        Button btnBuild = new Button("Construire (" + (int)cost + " €)");
        btnBuild.setMaxWidth(Double.MAX_VALUE);
        btnBuild.setStyle(UIStyles.BUTTON_PRIMARY);
        
        // Action
        btnBuild.setOnAction(e -> {
            controller.getBuildingController().setBuildPowerPlantMode(type);
        });

        card.getChildren().addAll(header, btnBuild);
        return card;
    }

    // Carte pour une Infrastructure
    private VBox createConstructionCard(BuildingType type) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(8));
        card.setStyle("-fx-background-color: #374151; -fx-background-radius: 5; -fx-border-color: #4b5563; -fx-border-radius: 5;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label(type.getIcon());
        icon.setFont(Font.font(24));
        Label name = new Label(type.getDisplayName());
        name.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        header.getChildren().addAll(icon, name);

        double cost = type.getConstructionCost();
        Button btnBuild = new Button("Construire (" + (int)cost + " €)");
        btnBuild.setMaxWidth(Double.MAX_VALUE);
        btnBuild.setStyle(UIStyles.BUTTON_PRIMARY);

        // Action
        btnBuild.setOnAction(e -> {
            controller.getBuildingController().setBuildInfrastructureMode(type);
        });

        card.getChildren().addAll(header, btnBuild);
        return card;
    }

    // ==========================================
    //             ONGLET GESTION
    // ==========================================
    private void refreshBuildingList() {
        managementContent.getChildren().clear();

        var buildings = controller.getBuildingController().getAllBuildings();

        if (buildings.isEmpty()) {
            Label empty = new Label("Aucun bâtiment en ville.");
            empty.setStyle("-fx-text-fill: #9ca3af; -fx-font-style: italic;");
            managementContent.getChildren().add(empty);
            return;
        }

        // Filtrer et trier
        var powerPlants = buildings.stream()
                .filter(b -> b instanceof PowerPlant)
                .map(b -> (PowerPlant) b)
                .toList();

        var infrastructures = buildings.stream()
                .filter(b -> b instanceof Infrastructure)
                .map(b -> (Infrastructure) b)
                .toList();

        if (!powerPlants.isEmpty()) {
            Label h = new Label("⚡ Centrales (" + powerPlants.size() + ")");
            h.setStyle(UIStyles.LABEL_SUBTITLE);
            managementContent.getChildren().add(h);
            for (PowerPlant p : powerPlants) managementContent.getChildren().add(createManageCard(p));
        }

        if (!infrastructures.isEmpty()) {
            Separator sep = new Separator();
            sep.setPadding(new Insets(10, 0, 10, 0));
            managementContent.getChildren().add(sep);
            
            Label h = new Label("🏢 Infrastructures (" + infrastructures.size() + ")");
            h.setStyle(UIStyles.LABEL_SUBTITLE);
            managementContent.getChildren().add(h);
            for (Infrastructure i : infrastructures) managementContent.getChildren().add(createManageCard(i));
        }
    }

    private VBox createManageCard(Building building) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(10));
        card.setStyle(UIStyles.CARD);

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        String iconStr = "❓";
        String nameStr = building.getType();
        
        if (building instanceof PowerPlant pp) {
            iconStr = pp.getPlantType().getIcon();
            nameStr = pp.getPlantType().getDisplayName();
        } else if (building instanceof Infrastructure inf) {
            iconStr = inf.getInfrastructureType().getIcon();
            nameStr = inf.getInfrastructureType().getDisplayName();
        }

        Label icon = new Label(iconStr);
        icon.setFont(Font.font(20));
        
        VBox texts = new VBox(2);
        Label name = new Label(nameStr);
        name.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        Label lvl = new Label("Niveau " + building.getLevel());
        lvl.setStyle("-fx-text-fill: #d1d5db; -fx-font-size: 10px;");
        texts.getChildren().addAll(name, lvl);
        
        header.getChildren().addAll(icon, texts);

        // Bouton Upgrade
        Button upgradeBtn = new Button();
        upgradeBtn.setMaxWidth(Double.MAX_VALUE);

        boolean canUpgrade = building.canUpgrade();
        double cost = canUpgrade ? building.getUpgradeCost() : 0;
        boolean canAfford = controller.getCity().canAfford(cost);

        if (!canUpgrade) {
            upgradeBtn.setText("MAX");
            upgradeBtn.setDisable(true);
            upgradeBtn.setStyle("-fx-background-color: #374151; -fx-text-fill: #9ca3af;");
        } else {
             upgradeBtn.setText("⬆ " + (int)cost + " €");
             if (canAfford) {
                 upgradeBtn.setStyle(UIStyles.BUTTON_SUCCESS);
                 upgradeBtn.setDisable(false);
             } else {
                 upgradeBtn.setStyle(UIStyles.BUTTON_PRIMARY); // Ou rouge/gris pour indiquer pas assez d'argent
                 upgradeBtn.setDisable(true);
             }
        }
        
        upgradeBtn.setOnAction(e -> {
            BuildingUpgradeDialog.show(building, controller);
            javafx.application.Platform.runLater(this::refreshBuildingList);
        });

        card.getChildren().addAll(header, upgradeBtn);
        return card;
    }

    private void startUpdateTimer() {
        // Rafraîchir l'interface toutes les quelques secondes pour mettre à jour les disponibilités
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        javafx.util.Duration.seconds(2),
                        e -> {
                            // On rafraîchit la construction aussi car de nouveaux bâtiments
                            // peuvent se débloquer avec le niveau
                            refreshConstructionList();
                            refreshBuildingList();
                        }));
        timeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        timeline.play();
    }

    public VBox getView() {
        return mainLayout;
    }
}
