package org.td.model.entities;

import java.io.Serializable;
import java.util.UUID;

/**
 * Classe abstraite représentant un bâtiment dans la ville
 * Tous les types de bâtiments héritent de cette classe
 */
public abstract class Building implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String id; // Identifiant unique
    protected int x, y; // Position sur la carte (en pixels)
    protected int level; // Niveau du bâtiment (1-5)
    protected boolean isActive; // Est-ce que le bâtiment fonctionne
    protected double constructionCost; // Coût de construction
    protected long constructionTime; // Timestamp de construction
    protected boolean isUnderConstruction; // En cours de construction
    protected int constructionProgress; // Progression (0-100)

    /**
     * Constructeur de base
     * @param level Niveau initial du bâtiment
     * @param x Position X sur la carte
     * @param y Position Y sur la carte
     */
    public Building(int level, int x, int y) {
        this.id = UUID.randomUUID().toString();
        this.level = level;
        this.x = x;
        this.y = y;
        this.isActive = true;
        this.constructionTime = System.currentTimeMillis();
        this.isUnderConstruction = false;
        this.constructionProgress = 100;
    }

    /**
     * Retourne le type de bâtiment (à implémenter par les sous-classes)
     */
    public abstract String getType();

    /**
     * Retourne la largeur du bâtiment en cellules
     */
    public abstract int getWidth();

    /**
     * Retourne la hauteur du bâtiment en cellules
     */
    public abstract int getHeight();

    /**
     * Mise à jour du bâtiment (appelée chaque cycle de jeu)
     */
    public void update() {
        if (isUnderConstruction && constructionProgress < 100) {
            constructionProgress += 5; // Progression de 5% par cycle
            if (constructionProgress >= 100) {
                isUnderConstruction = false;
                constructionProgress = 100;
                onConstructionComplete();
            }
        }
    }

    /**
     * Appelé quand la construction est terminée
     */
    protected void onConstructionComplete() {
        // Peut être surchargé par les sous-classes
    }

    /**
     * Améliore le bâtiment au niveau suivant
     * @return true si l'amélioration a réussi
     */
    public boolean upgrade() {
        if (level < 5 && !isUnderConstruction) {
            level++;
            isUnderConstruction = true;
            constructionProgress = 0;
            return true;
        }
        return false;
    }

    /**
     * Calcule le coût d'amélioration
     */
    public double getUpgradeCost() {
        if (level >= 5) return 0;
        return constructionCost * level * 0.6;
    }

    /**
     * Vérifie si le bâtiment peut être amélioré
     */
    public boolean canUpgrade() {
        return level < 5 && !isUnderConstruction;
    }

    /**
     * Active ou désactive le bâtiment
     */
    public void toggleActive() {
        this.isActive = !this.isActive;
    }

    /**
     * Vérifie si ce bâtiment chevauche un autre
     */
    public boolean overlaps(Building other) {
        return !(this.x + this.getWidth() * 40 <= other.x ||
                other.x + other.getWidth() * 40 <= this.x ||
                this.y + this.getHeight() * 40 <= other.y ||
                other.y + other.getHeight() * 40 <= this.y);
    }

    /**
     * Retourne une description du bâtiment
     */
    public String getDescription() {
        return String.format("%s (Niveau %d)", getType(), level);
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, Math.min(5, level));
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public double getConstructionCost() {
        return constructionCost;
    }

    public long getConstructionTime() {
        return constructionTime;
    }

    public boolean isUnderConstruction() {
        return isUnderConstruction;
    }

    public int getConstructionProgress() {
        return constructionProgress;
    }

    @Override
    public String toString() {
        return String.format("%s[id=%s, level=%d, pos=(%d,%d)]",
                getType(), id.substring(0, 8), level, x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Building building = (Building) obj;
        return id.equals(building.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
