package com.anno1800.Boardtiles;

public class TradeShip implements shipType {
    private int level;
    private int activeTradeChips;

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
}
