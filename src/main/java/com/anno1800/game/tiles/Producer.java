package com.anno1800.game.tiles;

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
}
