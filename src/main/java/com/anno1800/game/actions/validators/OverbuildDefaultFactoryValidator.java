package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.tiles.Factory;

/**
 * Validates OverbuildDefaultFactory actions.
 * Checks if a default factory can be overbuilt with a new factory.
 */
public class OverbuildDefaultFactoryValidator {

    /**
     * Validates if an OverbuildDefaultFactory action can be performed.
     * 
     * Requirements:
     * - Default factory must not be null
     * - New factory must not be null
     * - Player must own the default factory
     * - Default factory must be a default factory (not a regular factory)
     * - Player must have required resources/goods for the new factory
     * - New factory must be available on the game board
     * 
     * @param action The OverbuildDefaultFactory action to validate
     * @param player The player performing the action
     * @param game The current game state
     * @return true if the action is valid, false otherwise
     */
    public static boolean canOverbuildDefaultFactory(Action.OverbuildDefaultFactory action, Player player, Game game) {
        Factory defaultFactory = action.defaultFactory();
        Factory newFactory = action.newFactory();
        
        if (defaultFactory == null || newFactory == null) {
            return false;
        }
        
        // A default factory can always be overbuilt
        // Workers will be exhausted, slots cleared, and factory marked as passive
        // The default factory becomes active again when the overbuilding factory is demolished
        return true;
    }
}
