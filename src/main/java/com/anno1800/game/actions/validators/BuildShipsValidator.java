package com.anno1800.game.actions.validators;

import com.anno1800.game.actions.Action;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;

/**
 * Validates building-related actions (factories, shipyards, ships).
 */
public class BuildShipsValidator {

    /**
     * Validates BuildShips action.
     * Requirements:
     * - Must have free sea tiles
     * - Ship must be available on the board (deque not empty)
     * - Required chips must be available on the board
     * - Ship level must be valid (1-3)
     */
    public static boolean canBuildShips(Action.BuildShips action, Player player, Game game) {
        PlayerBoard playerBoard = player.getPlayerBoard();

        // Check if player has enough free sea tiles for the amount of ships
        if (playerBoard.getFreeSeaTiles() < action.amount()) {
            return false;
        }

        // Validate level
        if (action.level() < 1 || action.level() > 3) {
            return false;
        }

        // Check board-side constraints for each ship
        for (int i = 0; i < action.amount(); i++) {
            if (!game.getBoard().canTakeShip(action.shipType(), action.level())) {
                return false;
            }
        }

        return true;
    }
}
