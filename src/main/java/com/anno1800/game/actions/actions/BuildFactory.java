package com.anno1800.game.actions.actions;

import com.anno1800.data.gamedata.Goods;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.tiles.Factory;

/**
 * Build a factory on a free land tile.
 * 
 * PRECONDITION: ActionValidator has verified all requirements.
 * Goods are obtained and consumed during this action.
 */
public class BuildFactory {
    public static void buildFactory(Player player, Factory factory, Game game) {
        PlayerBoard board = player.getPlayerBoard();
        
        // Get required goods
        Goods[] costs = factory.costs();
        
        if (costs != null && costs.length > 0) {
            System.out.println("Building factory requires: " + java.util.Arrays.toString(costs));
            
            // PLANNING PHASE: Determine how to obtain goods
            if (!board.canObtainGoods(costs, game)) {
                throw new IllegalStateException("Cannot obtain required goods for " + factory.getType());
            }
            
            // EXECUTION PHASE: Actually obtain and consume goods
            board.consumeGoods(costs, game);
        }

        // Take factory from board
        Factory factoryFromBoard = game.getBoard().takeFactory(factory.getType());

        // Add factory to player's board
        board.buildFactory(factoryFromBoard);

        System.out.println("Successfully built factory: " + factory.getType());
    }
}
