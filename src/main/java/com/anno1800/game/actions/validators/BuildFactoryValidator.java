package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.tiles.Factory;

/**
 * Validates building-related actions (factories, shipyards, ships).
 */
public class BuildFactoryValidator {

    /**
     * Validates BuildFactory action.
     * Requirements:
     * - Must have free land or coast tile (depending on factory type)
     * - Factory type must be available on board
     * - Required goods must be obtainable (production/trade/import)
     * 
     * Note: This uses the planning phase - it checks if goods CAN be obtained
     * without actually producing them. The actual production happens during execution.
     */
    public static boolean canBuildFactory(Action.BuildFactory action, Player player, Game game) {
        Factory factory = action.factory();

        // Check if factory is available on the board
        if (!game.getBoard().hasFactory(factory.getType())) {
            return false;
        }

        // Check if player has free tiles (land or coast)
        PlayerBoard board = player.getPlayerBoard();
        if (board.getFreeLandTiles() <= 0 && board.getFreeCoastTiles() <= 0) {
            return false;
        }

        // PLANNING PHASE: Check if player can obtain required goods
        // This will simulate production/trade and add to storedGoods
        boolean canObtain = board.canObtainGoods(factory.costs(), game);
        
        // Clear storedGoods after validation (rollback)
        // The actual production will happen during action execution
        board.clearStoredGoods();
        
        return canObtain;
    }
}
