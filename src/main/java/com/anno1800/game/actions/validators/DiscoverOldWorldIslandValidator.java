package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;

/**
 * Validates exploration-related actions (discovering islands, expeditions).
 */
public class DiscoverOldWorldIslandValidator {

    /**
     * Maximum number of Old World islands a player can discover.
     * Rule: "Kein Spieler kann mehr als 4 Alte-Welt-Inseln erschließen."
     */
    public static final int MAX_OLD_WORLD_ISLANDS = 4;

    /**
     * Validates DiscoverOldWorldIsland action.
     * Requirements:
     * - Player must not have already discovered 4 Old World islands (maximum limit)
     * - Must have at least numOldWorldIslands + 1 available explorer chips
     * - Old World islands must be available on the board
     */
    public static boolean canDiscoverOldWorldIsland(Action.DiscoverOldWorldIsland action, Player player, Game game) {
        // Check if player has already reached the maximum of 4 Old World islands
        if (player.getPlayerBoard().getNumOldWorldIslands() >= MAX_OLD_WORLD_ISLANDS) {
            return false;
        }

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
