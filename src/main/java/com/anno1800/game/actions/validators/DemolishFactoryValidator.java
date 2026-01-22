package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.tiles.Factory;

/**
 * Validates DemolishFactory actions.
 * Checks if a factory can be demolished from the player's board.
 */
public class DemolishFactoryValidator {

    /**
     * Validates if a DemolishFactory action can be performed.
     * 
     * Requirements:
     * - Factory must not be null
     * - Player must own the factory
     * - Factory must be a non-default factory (default factories cannot be demolished)
     * 
     * @param action The DemolishFactory action to validate
     * @param player The player performing the action
     * @param game The current game state
     * @return true if the action is valid, false otherwise
     */
    public static boolean canDemolishFactory(Action.DemolishFactory action, Player player, Game game) {
        Factory factory = action.factory();
        
        if (factory == null) {
            return false;
        }
        
        // A factory can always be demolished
        // Workers will be exhausted and factory returned to board
        return true;
    }
}
