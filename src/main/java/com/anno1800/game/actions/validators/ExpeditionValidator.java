package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;

/**
 * Validates exploration-related actions (discovering islands, expeditions).
 */
public class ExpeditionValidator {

    public static boolean canExpedition(Action.Expedition action, Player player, Game game) {
        // Check if player has at least 2 available explorer chips
        if (player.getPlayerBoard().getAvailableExplorerChips() < 2) {
            return false;
        }

        // Check if there are expedition cards available on the board
        if (game.getBoard().getExpeditionStack().isEmpty()) {
            return false;
        }

        return true;
    }
}
