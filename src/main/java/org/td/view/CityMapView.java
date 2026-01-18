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
        // Important: Mettre le fond du ScrollPane en sombre pour éviter le blanc lors
        // du redimensionnement
        scrollPane.setStyle(UIStyles.SCROLLBAR + "-fx-background: " + UIColors.toCss(UIColors.BACKGROUND_DARK) + ";" +
                "-fx-background-color: " + UIColors.toCss(UIColors.BACKGROUND_DARK) + ";");

        tooltip = new Tooltip();
        tooltip.setStyle(UIStyles.TOOLTIP);
    }

    private boolean isMouseOver = false; // Suivre si la souris est sur la carte

    private void setupEventHandlers() {
        canvas.setOnMouseClicked(this::handleMouseClick);
        canvas.setOnMouseMoved(this::handleMouseMove);

        // Fix: Ne pas afficher l'aperçu si la souris est hors de la zone
        canvas.setOnMouseEntered(e -> isMouseOver = true);
        canvas.setOnMouseExited(e -> isMouseOver = false);
    }

    // ... rest of event handlers ...

    private void drawBuildPreview() {
        // Fix: Si la souris n'est pas sur la carte, on ne dessine rien !
        if (!isMouseOver)
            return;

        BuildingController bc = controller.getBuildingController();

        // Calculer si placement valide
        boolean valid = !isOccupied(mouseGridX, mouseGridY);
        Color previewColor = valid ? UIColors.withOpacity(UIColors.SUCCESS, 0.5)
                : UIColors.withOpacity(UIColors.ERROR, 0.5);

        // Taille adaptative
        int size = GameConfig.CELL_SIZE;
        String name = "";

        if (bc.getBuildMode() == BuildingMode.POWER_PLANT) {
            size = GameConfig.CELL_SIZE * 2;
            if (bc.getSelectedPlantType() != null) {
                name = bc.getSelectedPlantType().getDisplayName();
            }
        } else if (bc.getBuildMode() == BuildingMode.INFRASTRUCTURE) {
            if (bc.getSelectedInfraType() != null) {
                name = bc.getSelectedInfraType().getDisplayName();
            }
        }

        gc.setFill(previewColor);
        gc.fillRoundRect(mouseGridX, mouseGridY, size, size, 8, 8);

        gc.setStroke(valid ? UIColors.SUCCESS : UIColors.ERROR);
        gc.setLineWidth(2);
        gc.strokeRoundRect(mouseGridX, mouseGridY, size, size, 8, 8);

        // Icône / Indicateur
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 20));
        String icon = bc.getBuildMode() == BuildingMode.POWER_PLANT ? "⚡" : "🏗";
        gc.fillText(icon, mouseGridX + size / 2 - 10, mouseGridY + size / 2 + 10);

        // Afficher le nom du bâtiment au-dessus
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(3);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        // Effet de contour pour lisibilité
        gc.strokeText(name, mouseGridX, mouseGridY - 10);
        gc.fillText(name, mouseGridX, mouseGridY - 10);
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
        // Fix: Force status à true car si la souris bouge, elle est dessus !
        isMouseOver = true;

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
        // Fix: On retire le binding qui cassait le rendu
        // canvas.widthProperty().bind(container.widthProperty());
        // canvas.heightProperty().bind(container.heightProperty());

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
        try {
            // Fond
            gc.setFill(UIColors.BACKGROUND_DARK);
            // On dessine le fond sur toute la taille du canvas fixe
            gc.fillRect(0, 0, GameConfig.CANVAS_WIDTH, GameConfig.CANVAS_HEIGHT);

            // Grille
            if (GameConfig.SHOW_GRID) {
                drawGrid();
            }

            // Bâtiments
            City city = controller.getCity();
            if (city != null) {
                if (city.getResidences() != null)
                    for (Residence residence : city.getResidences()) {
                        if (residence != null)
                            drawResidence(residence);
                    }

                if (city.getPowerPlants() != null)
                    for (PowerPlant plant : city.getPowerPlants()) {
                        if (plant != null)
                            drawPowerPlant(plant);
                    }

                if (city.getInfrastructures() != null)
                    for (Infrastructure infra : city.getInfrastructures()) {
                        if (infra != null)
                            drawInfrastructure(infra);
                    }
            }

            // Aperçu construction
            if (controller.getBuildingController().getBuildMode() != BuildingMode.NONE) {
                drawBuildPreview();
            }

            // Debug info
            if (GameConfig.SHOW_DEBUG_INFO) {
                drawDebugInfo();
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Fallback pour ne pas écran noir total
            gc.setFill(Color.RED);
            gc.fillText("ERREUR RENDU: " + e.getMessage(), 10, 50);
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
        int size = GameConfig.CELL_SIZE * 2; // 80x80 pixels typically

        // Couleur de base
        Color baseColor = getPlantColor(plant);

        // --- RENDU SELON TYPE ET NIVEAU ---
        if (plant instanceof CoalPlant) {
            drawCoalPlant(x, y, size, plant.getLevel(), baseColor);
        } else if (plant instanceof NuclearPlant) {
            drawNuclearPlant(x, y, size, plant.getLevel(), baseColor);
        } else if (plant instanceof SolarPlant) {
            drawSolarPlant(x, y, size, plant.getLevel(), baseColor);
        } else if (plant instanceof WindTurbine) {
            drawWindTurbine(x, y, size, plant.getLevel(), baseColor);
        } else {
            // Fallback générique
            gc.setFill(baseColor);
            gc.fillRoundRect(x + 5, y + 5, size - 10, size - 10, 10, 10);
        }

        // --- INDICATEURS COMMUNS (Overlay) ---

        // Effet "Actif" (Bordure brillante)
        if (plant.isActive()) {
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1);
            gc.strokeRoundRect(x + 4, y + 4, size - 8, size - 8, 10, 10);
        }

        // Badge Niveau
        gc.setFill(Color.BLACK); // Fond noir
        gc.fillOval(x + size - 25, y + size - 25, 20, 20);
        gc.setStroke(Color.WHITE);
        gc.strokeOval(x + size - 25, y + size - 25, 20, 20);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.fillText(String.valueOf(plant.getLevel()), x + size - 18, y + size - 10);

        // Barre d'Efficacité (fine en bas)
        double efficiency = plant.getEfficiency();
        Color barColor = efficiency > 0.8 ? UIColors.SUCCESS : efficiency > 0.4 ? UIColors.WARNING : UIColors.ERROR;
        gc.setFill(Color.rgb(50, 50, 50));
        gc.fillRect(x + 10, y + size - 8, size - 20, 4); // Fond barre
        gc.setFill(barColor);
        gc.fillRect(x + 10, y + size - 8, (size - 20) * efficiency, 4);

        // Alerte Maintenance
        if (plant.needsMaintenance()) {
            gc.setFill(UIColors.ERROR); // Rouge clignotant idéalement
            gc.fillOval(x + size - 20, y + 4, 16, 16);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            gc.fillText("!", x + size - 15, y + 16);
        }
    }

    // --- DESSIN CHARBON (Usine) ---
    private void drawCoalPlant(int x, int y, int size, int level, Color color) {
        // Corps principal
        gc.setFill(color.darker());
        gc.fillRect(x + 10, y + size / 2, size - 20, size / 2 - 10);

        // Toit en dents de scie (Factory roof)
        gc.setFill(color.brighter());
        double[] xPoints = { x + 10, x + 10, x + 30, x + 30, x + 50, x + 50, x + size - 10, x + size - 10 };
        double[] yPoints = { y + size / 2, y + size / 2 - 10, y + size / 2, y + size / 2 - 10, y + size / 2,
                y + size / 2 - 10, y + size / 2, y + size / 2 };
        gc.fillPolygon(xPoints, yPoints, 8);

        // Cheminées
        gc.setFill(Color.rgb(60, 60, 60)); // Gris foncé
        // Cheminée 1 (toujours là)
        gc.fillRect(x + 15, y + 15, 10, size / 2);
        // Fumée 1
        if (level >= 1)
            drawSmoke(x + 20, y + 10);

        if (level >= 2) {
            // Cheminée 2
            gc.fillRect(x + 35, y + 10, 12, size / 2 + 5);
            drawSmoke(x + 41, y + 5);
        }
        if (level >= 3) {
            // Gros réservoir ou 3ème cheminée
            gc.setFill(Color.rgb(40, 40, 40));
            gc.fillRect(x + 55, y + 25, 15, size / 2 - 10);
        }
    }

    private void drawSmoke(int hx, int hy) {
        gc.setFill(Color.rgb(200, 200, 200, 0.6));
        gc.fillOval(hx - 5, hy - 10, 10, 10);
        gc.fillOval(hx + 2, hy - 15, 8, 8);
    }

    // --- DESSIN NUCLEAIRE (Réacteur + Tours) ---
    private void drawNuclearPlant(int x, int y, int size, int level, Color color) {
        // Sol bétonné
        gc.setFill(Color.GRAY);
        gc.fillRoundRect(x + 5, y + 5, size - 10, size - 10, 10, 10);

        // Réacteur (Dôme)
        gc.setFill(Color.WHITE);
        gc.fillOval(x + size / 2 - 15, y + size / 2, 30, 25);

        // Tour de refroidissement 1 (Forme hyperbolique simplifiée par trapeze)
        gc.setFill(Color.web("#e5e7eb")); // Gris très clair
        double[] xTower1 = { x + 10, x + 20, x + 30, x + 40 };
        double[] yTower1 = { y + size - 10, y + 20, y + 20, y + size - 10 };
        gc.fillPolygon(xTower1, yTower1, 4);

        // Cercle rouge sur la tour (classique)
        gc.setStroke(UIColors.ERROR);
        gc.strokeOval(x + 15, y + 30, 10, 5);

        if (level >= 2) {
            // Tour 2
            double[] xTower2 = { x + 50, x + 60, x + 70, x + 80 };
            double[] yTower2 = { y + size - 10, y + 30, y + 30, y + size - 10 };
            gc.fillPolygon(xTower2, yTower2, 4);
        }

        if (level >= 3) {
            // Glow bleu (Radiation/Energie)
            gc.setEffect(new javafx.scene.effect.DropShadow(20, Color.CYAN));
            gc.setFill(Color.CYAN);
            gc.fillOval(x + size / 2 - 5, y + size / 2 + 10, 10, 10);
            gc.setEffect(null); // Reset shadow
        }
    }

    // --- DESSIN SOLAIRE (Panneaux) ---
    private void drawSolarPlant(int x, int y, int size, int level, Color color) {
        // Sol terre/sable
        gc.setFill(Color.web("#d97706")); // Orange/Marron
        gc.fillRect(x + 5, y + 5, size - 10, size - 10);

        gc.setFill(Color.web("#0ea5e9")); // Bleu panneau
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);

        int rows = (level == 1) ? 2 : (level == 2) ? 3 : 4;
        int cols = (level == 1) ? 2 : 3;

        double panelW = (double) (size - 20) / cols;
        double panelH = (double) (size - 20) / rows;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double px = x + 10 + c * panelW;
                double py = y + 10 + r * panelH;
                // Panneau légèrement incliné (trapèze ?) ou rect simple
                gc.fillRect(px + 2, py + 2, panelW - 4, panelH - 4);
                gc.strokeRect(px + 2, py + 2, panelW - 4, panelH - 4);
            }
        }
    }

    // --- DESSIN EOLIEN (Turbines) ---
    private void drawWindTurbine(int x, int y, int size, int level, Color color) {
        // Sol herbe
        gc.setFill(Color.web("#10b981"));
        gc.fillRect(x + 5, y + 5, size - 10, size - 10);

        int numTurbines = level; // 1, 2 ou 3 éoliennes

        for (int i = 0; i < numTurbines; i++) {
            // Position décalée
            int tx = x + size / 2 + (i == 1 ? -15 : (i == 2 ? 15 : 0));
            int ty = y + size - 10 - (i == 2 ? -5 : 0);
            drawSingleTurbine(tx, ty, 40);
        }
    }

    private void drawSingleTurbine(int x, int y, int height) {
        // Mât
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(3);
        gc.strokeLine(x, y, x, y - height);

        // Rotor (Centre)
        gc.setFill(Color.RED);
        gc.fillOval(x - 2, y - height - 2, 4, 4);

        // Pales (Simple croix pour l'instant - animation possible plus tard)
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeLine(x, y - height, x - 10, y - height - 10);
        gc.strokeLine(x, y - height, x + 10, y - height - 10);
    }

    private Color getPlantColor(PowerPlant plant) {
        return switch (plant.getPlantType()) {
            case COAL -> UIColors.PLANT_COAL;
            case SOLAR -> UIColors.PLANT_SOLAR;
            case WIND -> UIColors.PLANT_WIND;
            case NUCLEAR -> UIColors.PLANT_NUCLEAR;
            case HYDRO -> Color.BLUE;
            case GEOTHERMAL -> Color.RED;
        };
    }

    // Supprimé getPlantIcon car on dessine nous-même maintenant

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

    private void drawDebugInfo() {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 12));
        gc.fillText("FPS: 60 | Buildings: " +
                controller.getBuildingController().getAllBuildings().size(), 10, 20);
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
