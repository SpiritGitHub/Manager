package org.td.controller;

public class BuildResult {
    public final boolean success;
    public final String message;

    public BuildResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
