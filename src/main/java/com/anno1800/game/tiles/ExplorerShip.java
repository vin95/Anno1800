package com.anno1800.game.tiles;

public class ExplorerShip implements shipType{
    private int level;
    private int activeExplorerChips;

    public ExplorerShip(int level) {
        this.level = level;
        this.activeExplorerChips = level;
    }

    public int getLevel() {
        return level;
    }

    public int getActiveExplorerChips() {
        return activeExplorerChips;
    }

    public void useExplorerChip() {
        if (activeExplorerChips > 0) {
            activeExplorerChips--;
        }
    }

    public void resetExplorerChips() {
        this.activeExplorerChips = level;
    }
}
