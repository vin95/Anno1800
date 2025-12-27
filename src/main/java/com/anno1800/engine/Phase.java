package com.anno1800.engine;

/**
 * Represents the different phases of a game turn.
 * Each player goes through these phases in order during their turn.
 */
public enum Phase {
    PRODUCTION("Production Phase"),
    ACTIONS("Actions Phase"),
    TRADING("Trading Phase"),
    END_OF_TURN("End of Turn");
    
    private final String displayName;
    
    Phase(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
