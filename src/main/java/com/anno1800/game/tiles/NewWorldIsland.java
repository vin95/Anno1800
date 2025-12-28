package com.anno1800.game.tiles;

public class NewWorldIsland implements Island {
    Plantation[] plantations;
    String type = "New World Island";

    public NewWorldIsland(Plantation[] plantations) {
        this.plantations = plantations;
    }

    public Plantation[] getPlantations() {
        return plantations;
    }
}
