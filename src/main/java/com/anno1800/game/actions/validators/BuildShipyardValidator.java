package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.tiles.ShipCosts;

/**
 * Validates building-related actions (factories, shipyards, ships).
 */
public class BuildShipyardValidator {

    /**
     * Validates BuildShipyard action.
     * Requirements:
     * - Must have free coast tiles (shipyards can only be built on coast)
     * - Shipyard of the specified level must be available on board
     * - Level must be valid (1-3)
     * - Required goods must be obtainable (production/trade/import)
     */
    public static boolean canBuildShipyard(Action.BuildShipyard action, Player player, Game game) {
        // Validate level
        if (action.level() < 1 || action.level() > 3) {
            return false;
        }

        // Check if player has free coast tiles
        if (player.getPlayerBoard().getFreeCoastTiles() <= 0) {
            return false;
        }

        // Check if shipyard is available on board
        if (!game.getBoard().hasShipyard(action.level())) {
            return false;
        }

        PlayerBoard board = player.getPlayerBoard();
        
        // PLANNING PHASE: Check if player can obtain required goods
        boolean canObtain = board.canObtainGoods(ShipCosts.getShipyardCost(action.level()), game);
        
        // Clear storedGoods after validation (rollback)
        board.clearStoredGoods();
        
        return canObtain;
    }
}
