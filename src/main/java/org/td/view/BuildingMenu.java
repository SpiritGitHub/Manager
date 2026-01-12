package org.td.view;

import org.td.controller.GameController;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.td.model.enums.*;
import org.td.utils.*;

// FICHIER TEMPORAIRE MINIMAL POUR COMPILATION
// TODO: Restaurer le contenu complet
public class BuildingMenu {
    private GameController controller;
    private VBox view;

    public BuildingMenu(GameController controller) {
        this.controller = controller;
        this.view = new VBox();
        view.setPrefWidth(GameConfig.SIDEBAR_WIDTH);
        view.setPadding(new Insets(20));
        view.setStyle(UIStyles.PANEL_DARK);

        Label title = new Label("📋 Menu - En reconstruction");
        title.setStyle(UIStyles.LABEL_TITLE);
        view.getChildren().add(title);
    }

    public void toggle() {
        view.setVisible(!view.isVisible());
        view.setManaged(!view.isManaged());
    }

    public VBox getView() {
        return view;
    }
}
