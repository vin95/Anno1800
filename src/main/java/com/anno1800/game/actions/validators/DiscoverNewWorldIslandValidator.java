package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;

/**
 * Validates exploration-related actions (discovering islands, expeditions).
 */
public class DiscoverNewWorldIslandValidator {

    /**
     * Maximum number of New World islands a player can discover.
     * Rule: "Kein Spieler kann mehr als 4 Neue-Welt-Inseln erkunden."
     */
    public static final int MAX_NEW_WORLD_ISLANDS = 4;

    /**
     * Validates DiscoverNewWorldIsland action.
     * Requirements:
     * - Player must not have already discovered 4 New World islands (maximum limit)
     * - Must have at least numNewWorldIslands + 1 available explorer chips
     * - New World islands must be available on the board
     */
    public static boolean canDiscoverNewWorldIsland(Action.DiscoverNewWorldIsland action, Player player, Game game) {
        // Check if player has already reached the maximum of 4 New World islands
        if (player.getPlayerBoard().getNumNewWorldIslands() >= MAX_NEW_WORLD_ISLANDS) {
            return false;
        }

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
