package org.td.controller;

import org.td.model.enums.EventType;
public interface GameEventListener {
    void onGameEvent(String message, EventType type);
}
