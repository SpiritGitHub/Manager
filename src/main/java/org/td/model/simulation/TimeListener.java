package org.td.model.simulation;

import org.td.model.enums.GameSpeed;

import java.time.LocalDateTime;

/**
 * Interface pour écouter les événements temporels
 */
public interface TimeListener {
    default void onTimeAdvanced(LocalDateTime currentTime) {
    }

    default void onNewDay(LocalDateTime currentTime) {
    }

    default void onNewMonth(LocalDateTime currentTime) {
    }

    default void onPaused() {
    }

    default void onResumed() {
    }

    default void onSpeedChanged(GameSpeed newSpeed) {
    }

    default void onGameOver(String reason) {
    }

    default void onWarning(String message) {
    }
}
