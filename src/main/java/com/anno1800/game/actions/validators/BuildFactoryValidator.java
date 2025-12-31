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
     * - Required goods must be producible (checked by agent/AI before action)
     * 
     * Note: This does NOT check if goods are available, because goods are produced
     * just-in-time and immediately consumed. The agent/AI must ensure that
     * ProduceGoods actions are executed before BuildFactory.
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

        // Note: We do NOT check for goods availability here because:
        // 1. Goods are produced just-in-time (ProduceGoods action)
        // 2. They are immediately consumed, not stored
        // 3. The agent/ActionGenerator must ensure ProduceGoods actions come first

        return true;
    }
}
