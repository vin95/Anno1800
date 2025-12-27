package com.anno1800.data;

public enum Plantations {
    
    // New World Materials
    CACAO_PLANTATION("Cacao Plantation"),
    SUGAR_PLANTATION("Sugar Plantation"),
    TOBACCO_PLANTATION("Tobacco Plantation"),
    COFFEE_PLANTATION("Coffee Plantation"),
    COTTON_PLANTATION("Cotton Plantation"),
    RUBBER_PLANTATION("Rubber Plantation");

    private final String displayName;

    Plantations(String displayName) {
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
