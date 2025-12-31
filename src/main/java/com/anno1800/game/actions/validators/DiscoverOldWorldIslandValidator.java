package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;

/**
 * Validates exploration-related actions (discovering islands, expeditions).
 */
public class DiscoverOldWorldIslandValidator {

    /**
     * Validates DiscoverOldWorldIsland action.
     * Requirements:
     * - Must have at least one available explorer chip
     * - Old World islands must be available on the board
     */
    public static boolean canDiscoverOldWorldIsland(Action.DiscoverOldWorldIsland action, Player player, Game game) {
        // Check if player has at least numOldWorldIslands + 1 available explorer chips
        if (player.getPlayerBoard().getAvailableExplorerChips() < player.getPlayerBoard().getNumOldWorldIslands() + 1) {
            return false;
        }

        // Check if there are Old World islands available on the board
        if (game.getBoard().getOldWorldIslands().isEmpty()) {
            return false;
        }

        return true;
    }
}
