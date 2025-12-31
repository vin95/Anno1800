package com.anno1800.game.actions.actions;

import com.anno1800.data.gamedata.Goods;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.tiles.Factory;

/**
 * Build a factory on a free land tile.
     * 
     * PRECONDITION: ActionValidator has verified all requirements.
     * All required goods must have been produced in previous actions.
 */
public class BuildFactory {
    public static void buildFactory(Player player, Factory factory, Game game) {
        // Goods verification (informational only)
        Goods[] costs = factory.costs();
        if (costs != null && costs.length > 0) {
            System.out.println("Building factory requires: " + java.util.Arrays.toString(costs));
            System.out.println("Assuming all goods were produced in previous ProduceGoods actions.");
        }

        // Take factory from board
        Factory factoryFromBoard = game.getBoard().takeFactory(factory.getType());

        // Add factory to player's board
        player.getPlayerBoard().buildFactory(factoryFromBoard);

        System.out.println("Successfully built factory: " + factory.getType());
    }
}
