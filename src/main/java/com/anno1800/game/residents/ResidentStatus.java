package com.anno1800.game.residents;

public enum ResidentStatus {
    ON_BOARD("on Board"),
    FIT("fit"),
    AT_WORK("at Work"),
    EXHAUSTED("Exhausted");

    private final String displayName;

    ResidentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}