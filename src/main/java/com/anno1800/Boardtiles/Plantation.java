package com.anno1800.Boardtiles;

import com.anno1800.data.Goods;
import com.anno1800.residents.Resident;

/**
 * Represents a plantation in the New World.
 * All plantations have exactly 2 worker slots.
 */
public class Plantation extends Producer {
    private final Goods produces;

    public Plantation(Producers type, Goods[] costs, Goods produces, int populationLevel) {
        super(type);
        this.produces = produces;
    }

    public Goods produces() {
        return produces;
    }
}
