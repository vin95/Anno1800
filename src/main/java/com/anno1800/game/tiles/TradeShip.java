package com.anno1800.game.tiles;

public class TradeShip implements shipType {
    private int level;
    private int activeTradeChips;
    private int tileIndex = -1;

    public TradeShip(int level) {
        this.level = level;
        this.activeTradeChips = level;
    }

    public int getLevel() {
        return level;
    }

    public int getActiveTradeChips() {
        return activeTradeChips;
    }

    public void useTradeChip() {
        if (activeTradeChips > 0) {
            activeTradeChips--;
        }
    }

    public void resetTradeChips() {
        this.activeTradeChips = level;
    }

    public int getTileIndex() {
        return tileIndex;
    }

    public void setTileIndex(int tileIndex) {
        this.tileIndex = tileIndex;
    }
}
