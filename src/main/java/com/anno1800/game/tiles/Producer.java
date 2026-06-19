package com.anno1800.game.tiles;

import com.anno1800.data.gamedata.Goods;
import com.anno1800.data.gamedata.Producers;

/**
 * Base class for all production buildings.
 * Contains the producer type from the Producers enum.
 */
public abstract class Producer {
    private final Producers type;

    protected Producer(Producers type) {
        this.type = type;
    }

    public Producers getType() {
        return type;
    }

    public Goods produces() {
        return type.getProduces();
    }

    /**
     * Convenience wrapper: returns whether this producer represents a Factory (not a Plantation).
     */
    public boolean isFactory() {
        return type.isFactory();
    }
}
