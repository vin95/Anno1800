package com.anno1800.Boardtiles;

import com.anno1800.data.Goods;
import com.anno1800.residents.Resident;

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

    public Factory(Producers type, Goods[] costs, Goods produces, int populationLevel) {
        super(type);
        this.costs = costs;
        this.produces = produces;
        this.populationLevel = populationLevel;
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

    /**
     * @return Number of worker slots (always 2 for all factories)
     */
    public int workspaces() {
        return 2;
    }
}

