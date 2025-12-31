package com.anno1800.game.tiles;

import com.anno1800.data.gamedata.Goods;
import com.anno1800.data.gamedata.Producers;
import com.anno1800.game.residents.Resident;

/**
 * Represents a production facility.
 * Each factory type can exist twice in the game.
 * All factories have exactly 2 worker slots.
 */
public class Factory extends Producer {
    private final Goods[] costs;
    private final Goods produces;
    private final int populationLevel;
    private Resident slot1;
    private Resident slot2;
    private int tradeCosts;

    public Factory(Producers type, Goods[] costs, Goods produces, int populationLevel, int tradeCosts) {
        super(type);
        this.costs = costs;
        this.produces = produces;
        this.populationLevel = populationLevel;
        this.tradeCosts = tradeCosts;
        this.slot1 = null;
        this.slot2 = null;
    }

    public Goods[] costs() {
        return costs;
    }

    public Goods produces() {
        return produces;
    }

    public int populationLevel() {
        return populationLevel;
    }

    public Resident getSlot1() {
        return slot1;
    }

    public void setSlot1(Resident slot1) {
        this.slot1 = slot1;
    }

    public Resident getSlot2() {
        return slot2;
    }

    public void setSlot2(Resident slot2) {
        this.slot2 = slot2;
    }

    public int getTradeCosts() {
        return tradeCosts;
    }

    public void freeSlots() {
        this.slot1 = null;
        this.slot2 = null;
    }

    /**
     * @return Number of worker slots (always 2 for all factories)
     */
    public int workspaces() {
        return 2;
    }
}

