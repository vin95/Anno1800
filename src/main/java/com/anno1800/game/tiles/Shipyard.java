package com.anno1800.game.tiles;

public class Shipyard {
    private int level;
    private int tileIndex = -1;

    public Shipyard(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public void buildShip() {
        // Logic to build a ship based on the shipyard level
    }

    public int getTileIndex() {
        return tileIndex;
    }

    public void setTileIndex(int tileIndex) {
        this.tileIndex = tileIndex;
    }
}
