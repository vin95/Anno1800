package com.anno1800.game.tiles;

import com.anno1800.data.gamedata.Goods;

/**
 * Represents a starting factory.
 * StartFactory is identical to Factory but represents factories available at game start.
 * The distinction is made via the Producers type.
 */
public class StartFactory extends Factory {

    public StartFactory(Producers type, Goods[] costs, Goods produces, int populationLevel, int tradeCosts) {
        super(type, costs, produces, populationLevel, tradeCosts);
    }
}
