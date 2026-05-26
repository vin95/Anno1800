package com.anno1800.game.actions.actions;

import com.anno1800.data.gamedata.Goods;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.tiles.ShipCosts;

/**
 * Build a shipyard on a free coast tile.
 * 
 * PRECONDITION: ActionValidator has verified all requirements.
 * Goods are obtained and consumed during this action.
 */
public class BuildShipyard {
    public static void buildShipyard(Player player, int level, Game game) {
        PlayerBoard board = player.getPlayerBoard();
        
        // Get required goods
        Goods[] costs = ShipCosts.getShipyardCost(level);
        
        if (costs != null && costs.length > 0) {
            System.out.println("Building shipyard level " + level + " requires: " + java.util.Arrays.toString(costs));
            
            // PLANNING PHASE: Determine how to obtain goods
            if (!board.canObtainGoods(costs, game)) {
                throw new IllegalStateException("Cannot obtain required goods for shipyard level " + level);
            }
            
            // EXECUTION PHASE: Actually obtain and consume goods
            board.consumeGoods(costs, game);
        }
        
        // Take shipyard from board
        var shipyard = game.getBoard().takeShipyard(level);

        // Add shipyard to player's board
        board.buildShipyard(shipyard);

        System.out.println("Successfully built shipyard level " + level);
    }
}
