package com.anno1800.Boardtiles;

import com.anno1800.data.Goods;

/**
 * Represents a starting factory.
 * StartFactory is identical to Factory but represents factories available at game start.
 * The distinction is made via the Producers type.
 */
public class StartFactory extends Factory {

    public StartFactory(Producers type, Goods[] costs, Goods produces, int populationLevel) {
        super(type, costs, produces, populationLevel);
    }
}
