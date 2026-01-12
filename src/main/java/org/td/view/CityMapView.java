package org.td.view;

import org.td.controller.*;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.td.model.entities.*;
import org.td.model.enums.*;
import org.td.utils.*;

/**
 * Vue de la carte de la ville
 * Affiche tous les bâtiments et gère les interactions
 */
public class CityMapView {
    private GameController controller;
    private Canvas canvas;
    private GraphicsContext gc;
    private ScrollPane scrollPane;
    private StackPane container;

    private Tooltip tooltip;
    private Building hoveredBuilding;
    private int mouseGridX, mouseGridY;

    public CityMapView(GameController controller) {
        this.controller = controller;
        createView();
        setupEventHandlers();
        startRenderLoop();
    }

    private void createView() {
        canvas = new Canvas(GameConfig.CANVAS_WIDTH, GameConfig.CANVAS_HEIGHT);
        gc = canvas.getGraphicsContext2D();

        container = new StackPane(canvas);
        container.setStyle("-fx-background-color: " + UIColors.toCss(UIColors.BACKGROUND_DARK) + ";");

        scrollPane = new ScrollPane(container);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle(UIStyles.SCROLLBAR);

        tooltip = new Tooltip();
        tooltip.setStyle(UIStyles.TOOLTIP);
    }

    private void setupEventHandlers() {
        canvas.setOnMouseClicked(this::handleMouseClick);
        canvas.setOnMouseMoved(this::handleMouseMove);
    }

    private void handleMouseClick(MouseEvent event) {
        int x = (int) (event.getX() / GameConfig.CELL_SIZE) * GameConfig.CELL_SIZE;
        int y = (int) (event.getY() / GameConfig.CELL_SIZE) * GameConfig.CELL_SIZE;

        if (event.getButton() == MouseButton.PRIMARY) {
            // Clic gauche - construire ou sélectionner
            BuildingController bc = controller.getBuildingController();

            if (bc.getBuildMode() != BuildingMode.NONE) {
                // Mode construction
                BuildResult result = bc.attemptBuild(x, y);
                showBuildResult(result);
            } else {
                // Sélectionner bâtiment
                Building building = bc.getBuildingAt((int) event.getX(), (int) event.getY());
                if (building != null) {
                    showBuildingInfo(building);
                }
            }
        } else if (event.getButton() == MouseButton.SECONDARY) {
            // Clic droit - annuler construction
            controller.getBuildingController().cancelBuildMode();
        }
    }

    private void handleMouseMove(MouseEvent event) {
        mouseGridX = (int) (event.getX() / GameConfig.CELL_SIZE) * GameConfig.CELL_SIZE;
        mouseGridY = (int) (event.getY() / GameConfig.CELL_SIZE) * GameConfig.CELL_SIZE;

        // Tooltip pour bâtiment sous la souris
        Building building = controller.getBuildingController()
                .getBuildingAt((int) event.getX(), (int) event.getY());

        if (building != null && building != hoveredBuilding) {
            hoveredBuilding = building;
            tooltip.setText(building.getDescription());
            Tooltip.install(canvas, tooltip);
        } else if (building == null && hoveredBuilding != null) {
            hoveredBuilding = null;
            Tooltip.uninstall(canvas, tooltip);
        }
    }

    private void startRenderLoop() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                render();
            }
        };
        timer.start();
    }

    /**
     * Rendu principal de la carte
     */
    private void render() {
        // Fond
        gc.setFill(UIColors.BACKGROUND_DARK);
        gc.fillRect(0, 0, GameConfig.CANVAS_WIDTH, GameConfig.CANVAS_HEIGHT);

        // Grille
        if (GameConfig.SHOW_GRID) {
            drawGrid();
        }

        // Bâtiments
        City city = controller.getCity();

        for (Residence residence : city.getResidences()) {
            drawResidence(residence);
        }

        for (PowerPlant plant : city.getPowerPlants()) {
            drawPowerPlant(plant);
        }

        for (Infrastructure infra : city.getInfrastructures()) {
            drawInfrastructure(infra);
        }

        // Aperçu construction
        if (controller.getBuildingController().getBuildMode() != BuildingMode.NONE) {
            drawBuildPreview();
        }

        // Debug info
        if (GameConfig.SHOW_DEBUG_INFO) {
            drawDebugInfo();
        }
    }

    private void drawGrid() {
        gc.setStroke(UIColors.GRID_LINE);
        gc.setLineWidth(1);

        for (int x = 0; x < GameConfig.CANVAS_WIDTH; x += GameConfig.CELL_SIZE) {
            gc.strokeLine(x, 0, x, GameConfig.CANVAS_HEIGHT);
        }

        for (int y = 0; y < GameConfig.CANVAS_HEIGHT; y += GameConfig.CELL_SIZE) {
            gc.strokeLine(0, y, GameConfig.CANVAS_WIDTH, y);
        }
    }

    private void drawResidence(Residence residence) {
        int x = residence.getX();
        int y = residence.getY();
        int size = GameConfig.CELL_SIZE;

        // Couleur selon niveau
        Color color = switch (residence.getResidenceLevel()) {
            case BASIC -> UIColors.RESIDENCE_BASIC;
            case MEDIUM -> UIColors.RESIDENCE_MEDIUM;
            case ADVANCED -> UIColors.RESIDENCE_ADVANCED;
        };

        // Bâtiment principal
        gc.setFill(color);
        gc.fillRoundRect(x + 2, y + 2, size - 4, size - 4, 4, 4);

        // Bordure
        gc.setStroke(color.brighter());
        gc.setLineWidth(2);
        gc.strokeRoundRect(x + 2, y + 2, size - 4, size - 4, 4, 4);

        // Indicateur électricité
        if (!residence.hasElectricity()) {
            gc.setFill(UIColors.ERROR);
            gc.fillOval(x + size - 12, y + 4, 8, 8);
        }

        // Fenêtres
        gc.setFill(Color.web("#fbbf24"));
        int windows = residence.getLevel();
        for (int i = 0; i < windows; i++) {
            gc.fillRect(x + 8 + i * 8, y + 8, 4, 4);
        }
    }

    private void drawPowerPlant(PowerPlant plant) {
        int x = plant.getX();
        int y = plant.getY();
        int size = GameConfig.CELL_SIZE * 2;

        // Couleur selon type
        Color color = getPlantColor(plant);

        // Bâtiment principal
        gc.setFill(color);
        gc.fillRoundRect(x + 2, y + 2, size - 4, size - 4, 8, 8);

        // Effet glow si actif
        if (plant.isActive()) {
            gc.setStroke(color.brighter());
            gc.setLineWidth(3);
            gc.strokeRoundRect(x + 2, y + 2, size - 4, size - 4, 8, 8);
        }

        // Niveau
        gc.setFill(UIColors.BACKGROUND_DARK);
        gc.fillRoundRect(x + 5, y + 5, 24, 24, 4, 4);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText("L" + plant.getLevel(), x + 10, y + 22);

        // Icône
        gc.setFont(Font.font("Arial", 30));
        String icon = getPlantIcon(plant);
        gc.fillText(icon, x + 25, y + 50);

        // Barre efficacité
        double efficiency = plant.getEfficiency();
        gc.setFill(UIColors.BACKGROUND_DARK);
        gc.fillRect(x + 5, y + size - 15, size - 10, 10);

        Color barColor = efficiency > 0.7 ? UIColors.SUCCESS : efficiency > 0.4 ? UIColors.WARNING : UIColors.ERROR;
        gc.setFill(barColor);
        gc.fillRect(x + 5, y + size - 15, (size - 10) * efficiency, 10);

        // Alerte maintenance
        if (plant.needsMaintenance()) {
            gc.setFill(UIColors.WARNING);
            gc.fillOval(x + size - 16, y + 4, 12, 12);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            gc.fillText("!", x + size - 12, y + 14);
        }
    }

    private void drawInfrastructure(Infrastructure infra) {
        int x = infra.getX();
        int y = infra.getY();
        int width = infra.getWidth() * GameConfig.CELL_SIZE;
        int height = infra.getHeight() * GameConfig.CELL_SIZE;

        Color color = getInfraColor(infra.getInfrastructureType());

        gc.setFill(color);
        gc.fillRoundRect(x + 2, y + 2, width - 4, height - 4, 6, 6);

        gc.setStroke(color.brighter());
        gc.setLineWidth(2);
        gc.strokeRoundRect(x + 2, y + 2, width - 4, height - 4, 6, 6);

        // Icône
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 24));
        gc.fillText(infra.getInfrastructureType().getIcon(), x + 10, y + 30);
    }

    private void drawBuildPreview() {
        BuildingController bc = controller.getBuildingController();

        // Calculer si placement valide
        boolean valid = !isOccupied(mouseGridX, mouseGridY);
        Color previewColor = valid ? UIColors.withOpacity(UIColors.SUCCESS, 0.5)
                : UIColors.withOpacity(UIColors.ERROR, 0.5);

        int size = bc.getBuildMode() == BuildingMode.POWER_PLANT ? GameConfig.CELL_SIZE * 2 : GameConfig.CELL_SIZE;

        gc.setFill(previewColor);
        gc.fillRoundRect(mouseGridX, mouseGridY, size, size, 8, 8);

        gc.setStroke(valid ? UIColors.SUCCESS : UIColors.ERROR);
        gc.setLineWidth(2);
        gc.strokeRoundRect(mouseGridX, mouseGridY, size, size, 8, 8);
    }

    private void drawDebugInfo() {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 12));
        gc.fillText("FPS: 60 | Buildings: " +
                controller.getBuildingController().getAllBuildings().size(), 10, 20);
    }

    private Color getPlantColor(PowerPlant plant) {
        if (plant instanceof CoalPlant)
            return UIColors.PLANT_COAL;
        if (plant instanceof SolarPlant)
            return UIColors.PLANT_SOLAR;
        if (plant instanceof WindTurbine)
            return UIColors.PLANT_WIND;
        if (plant instanceof NuclearPlant)
            return UIColors.PLANT_NUCLEAR;
        return UIColors.PRIMARY;
    }

    private String getPlantIcon(PowerPlant plant) {
        if (plant instanceof CoalPlant)
            return "🏭";
        if (plant instanceof SolarPlant)
            return "☀️";
        if (plant instanceof WindTurbine)
            return "💨";
        if (plant instanceof NuclearPlant)
            return "☢️";
        return "⚡";
    }

    private Color getInfraColor(BuildingType type) {
        return switch (type.getCategory().getDisplayName()) {
            case "Commercial" -> UIColors.INFRA_COMMERCIAL;
            case "Divertissement" -> UIColors.INFRA_ENTERTAINMENT;
            case "Espace Vert" -> UIColors.INFRA_PARK;
            case "Service Public" -> UIColors.INFRA_PUBLIC;
            case "Sécurité" -> UIColors.INFRA_SECURITY;
            default -> UIColors.PRIMARY;
        };
    }

    private boolean isOccupied(int x, int y) {
        return controller.getBuildingController().getBuildingAt(x + 10, y + 10) != null;
    }

    private void showBuildResult(BuildResult result) {
        if (result.success) {
            controller.sendNotification(result.message, EventType.SUCCESS);

        } else {
            controller.sendNotification(result.message, EventType.ERROR);
        }
    }

    private void showBuildingInfo(Building building) {
        // Show the full upgrade dialog instead of a simple alert
        BuildingUpgradeDialog.show(building, controller);
    }

    public ScrollPane getView() {
        return scrollPane;
    }
}
