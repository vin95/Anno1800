package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;

/**
 * Validates exploration-related actions (discovering islands, expeditions).
 */
public class DiscoverNewWorldIslandValidator {

    /**
     * Validates DiscoverNewWorldIsland action.
     * Requirements:
     * - Must have at least numNewWorldIslands + 1 available explorer chips
     * - New World islands must be available on the board
     */
    public static boolean canDiscoverNewWorldIsland(Action.DiscoverNewWorldIsland action, Player player, Game game) {
        // Check if player has at least numNewWorldIslands + 1 available explorer chips
        if (player.getPlayerBoard().getAvailableExplorerChips() < player.getPlayerBoard().getNumNewWorldIslands() + 1) {
            return false;
        }

        // Check if there are New World islands available on the board
        if (game.getBoard().getNewWorldIslands().isEmpty()) {
            return false;
        }

        return true;
    }
}
